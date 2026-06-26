package com.omniclone.engine

import android.content.Context
import com.omniclone.model.CloneConfig
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injects OmniClone runtime hooks into the decoded smali source of a cloned APK.
 *
 * Responsibilities:
 * 1. Copy bundled smali hook templates into decoded/smali/omniclone/hooks/.
 * 2. Locate the original Application class or create a new one if absent.
 * 3. Ensure the original Application extends [com.omniclone.runtime.OmniCloneApplication].
 * 4. Write a [HookConfig] JSON asset used by OmniCloneApplication to decide which hooks to load.
 *
 * This is the Phase 1 implementation. Phase 2 expands the template library to cover all
 * feature-specific hooks listed in Section 7.
 */
@Singleton
class SmaliInjector @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val OMNICLONE_SMALI_DIR = "smali/omniclone"
        private const val OMNICLONE_HOOKS_DIR = "$OMNICLONE_SMALI_DIR/hooks"
        private const val OMNICLONE_RUNTIME_DIR = "$OMNICLONE_SMALI_DIR/runtime"
        private const val OMNICLONE_ASSETS_DIR = "smali_assets"
    }


    /**
     * Inject hooks into the decoded APK directory.
     *
     * @param decodedApkDir The directory produced by ApkTool decode.
     * @param config The clone configuration.
     * @return A [PatchResult] describing success or failure.
     */
    fun inject(decodedApkDir: File, config: CloneConfig): PatchResult {
        return try {
            val hooksDir = File(decodedApkDir, OMNICLONE_HOOKS_DIR).apply { mkdirs() }
            val runtimeDir = File(decodedApkDir, OMNICLONE_RUNTIME_DIR).apply { mkdirs() }

            copyTemplateFiles(hooksDir, runtimeDir)
            patchApplicationClass(decodedApkDir, config)
            writeHookConfigAsset(decodedApkDir, config)

            PatchResult.Success("Smali hooks injected")
        } catch (e: Exception) {
            PatchResult.Error("Smali injection failed", e.stackTraceToString())
        }
    }

    /**
     * Copy every .smali file from the bundled assets into the decoded smali tree.
     */
    private fun copyTemplateFiles(hooksDir: File, runtimeDir: File) {
        val templates = context.assets.list("smali_templates") ?: emptyArray()

        templates.filter { it.endsWith(".smali") }.forEach { templateName ->
            val targetDir = when {
                templateName in listOf(
                    "OmniCloneApplication.smali",
                    "OmniRuntime.smali",
                    "OmniAccessibilityService.smali",
                    "OmniNotificationListener.smali",
                    "OmniVpnService.smali",
                    "CalculatorActivity.smali"
                ) -> runtimeDir
                templateName.endsWith("Application.smali") -> runtimeDir
                else -> hooksDir
            }
            val targetFile = File(targetDir, templateName)
            context.assets.open("smali_templates/$templateName").use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    /**
     * Locate the Application class declared in AndroidManifest.xml and ensure it extends
     * OmniCloneApplication. If no application class exists, create OmniCloneApplication.smali
     * and point the manifest to it (manifest update is already done by [ManifestPatcher]).
     */
    private fun patchApplicationClass(decodedApkDir: File, config: CloneConfig) {
        val manifestFile = File(decodedApkDir, "AndroidManifest.xml")
        val manifestText = manifestFile.readText()

        val packageName = Regex("package=\"([^\"]+)\"").find(manifestText)?.groupValues?.get(1) ?: ""
        val appNameRegex = Regex("android:name=\"([^\"]+)\"")
        val appMatch = appNameRegex.find(manifestText)
        val declaredApp = appMatch?.groupValues?.get(1)?.let { resolveClassName(it, packageName) }?.replace(".", "/")

        val smaliDirs = decodedApkDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("smali")
        } ?: emptyArray()

        if (declaredApp != null && !declaredApp.startsWith("com/omniclone/runtime/OmniCloneApplication")) {
            // Try to find and patch the existing Application class.
            val existingAppFile = smaliDirs.firstNotNullOfOrNull { dir ->
                File(dir, "$declaredApp.smali").takeIf { it.exists() }
            }

            if (existingAppFile != null) {
                patchExistingApplication(existingAppFile)
                return
            }
        }

        // No existing Application class: create OmniCloneApplication directly.
        createOmniCloneApplication(File(decodedApkDir, OMNICLONE_RUNTIME_DIR), config)
    }

    private fun resolveClassName(name: String, packageName: String): String {
        return when {
            name.startsWith(".") -> packageName + name
            name.contains(".") -> name
            else -> "$packageName.$name"
        }
    }

    /**
     * Rewrite an existing Application class so it extends com/omniclone/runtime/OmniCloneApplication.
     * Preserves the original superclass relationship by remembering it in a synthetic field and
     * delegating constructor calls.
     */
    private fun patchExistingApplication(appFile: File) {
        val lines = appFile.readLines().toMutableList()
        val superIndex = lines.indexOfFirst { it.trim().startsWith(".super ") }
        if (superIndex == -1) {
            throw IllegalStateException("Application class missing .super directive")
        }

        val originalSuper = lines[superIndex].substringAfter(".super ").trim()

        // Update .super to OmniCloneApplication.
        lines[superIndex] = ".super Lcom/omniclone/runtime/OmniCloneApplication;"

        // Remember original superclass.
        val classNameLine = lines.indexOfFirst { it.trim().startsWith(".class ") }
        if (classNameLine != -1) {
            lines.add(
                classNameLine + 1,
                ".field private originalSuperClass:Ljava/lang/String; = \"$originalSuper\""
            )
        }

        // Rewrite direct-super constructor invocations to point to OmniCloneApplication.
        for (i in lines.indices) {
            val line = lines[i]
            if (line.contains("invoke-direct") && line.contains("<init>") && line.contains(originalSuper)) {
                lines[i] = line.replace(originalSuper, "Lcom/omniclone/runtime/OmniCloneApplication;")
            }
        }

        appFile.writeText(lines.joinToString("\n"))
    }

    /**
     * Create a fresh OmniCloneApplication.smali file when the source APK had no Application class.
     */
    private fun createOmniCloneApplication(runtimeDir: File, config: CloneConfig) {
        val appFile = File(runtimeDir, "OmniCloneApplication.smali")
        if (appFile.exists()) return // Already copied from templates.

        appFile.writeText(buildOmniCloneApplicationSmali(config))
    }

    /**
     * Write the hook configuration JSON into the APK assets so OmniCloneApplication can read it
     * at runtime.
     */
    private fun writeHookConfigAsset(decodedApkDir: File, config: CloneConfig) {
        val assetsDir = File(decodedApkDir, "assets/$OMNICLONE_ASSETS_DIR").apply { mkdirs() }
        val configFile = File(assetsDir, "hook_config.json")

        val hookConfig = buildHookConfig(config)
        configFile.writeText(hookConfig)
    }

    private fun buildHookConfig(config: CloneConfig): String {
        val id = config.cloneId
        val pkg = config.clonePackage
        val identity = config.identity

        return buildString {
            appendLine("{")
            appendLine("  \"cloneId\": \"$id\",")
            appendLine("  \"clonePackage\": \"$pkg\",")
            appendStringField("androidId", identity.androidId)
            appendStringField("imei", identity.imei)
            appendStringField("imsi", identity.imsi)
            appendStringField("wifiMac", identity.wifiMac)
            appendStringField("bluetoothMac", identity.bluetoothMac)
            appendStringField("ethernetMac", identity.ethernetMac)
            appendStringField("gsfId", identity.gsfId)
            appendStringField("googleAdId", identity.googleAdvertisingId)
            appendStringField("webViewUserAgent", identity.webViewUserAgent)
            appendStringField("systemUserAgent", identity.systemUserAgent)
            appendStringField("proxyHost", config.proxyHost)
            appendLine("  \"proxyPort\": ${config.proxyPort ?: 0}")
            appendLine("}")
        }
    }

    private fun StringBuilder.appendStringField(name: String, value: String?) {
        if (value != null) {
            appendLine("  \"$name\": \"$value\",")
        }
    }

    /**
     * Generate the OmniCloneApplication smali source if the bundled template is not available.
     */
    private fun buildOmniCloneApplicationSmali(config: CloneConfig): String {
        return """
            .class public Lcom/omniclone/runtime/OmniCloneApplication;
            .super Landroid/app/Application;

            .field private static final TAG:Ljava/lang/String; = "OmniCloneApp"

            .method public constructor <init>()V
                .locals 0
                invoke-direct {p0}, Landroid/app/Application;-><init>()V
                return-void
            .end method

            .method public onCreate()V
                .locals 2
                invoke-super {p0}, Landroid/app/Application;->onCreate()V
                const-string v0, "OmniCloneApp"
                const-string v1, "OmniClone runtime initialized"
                invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
                invoke-static {p0}, Lcom/omniclone/runtime/OmniCloneApplication;->loadHooks(Landroid/content/Context;)V
                return-void
            .end method

            .method private static loadHooks(Landroid/content/Context;)V
                .locals 2
                .param p0, "context"    # Landroid/content/Context;

                :try_start_0
                invoke-virtual {p0}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
                move-result-object v0
                const-string v1, "smali_assets/hook_config.json"
                invoke-virtual {v0, v1}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;
                move-result-object v0
                invoke-virtual {v0}, Ljava/io/InputStream;->close()V
                :try_end_0
                .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

                :catch_0
                return-void
            .end method
        """.trimIndent()
    }
}
