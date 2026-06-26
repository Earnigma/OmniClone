package com.omniclone.engine

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.omniclone.model.CloneConfig
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * WorkManager [CoroutineWorker] that runs the complete clone pipeline in the background with a
 * foreground notification. The worker accepts a JSON-serialized [CloneConfig] and reports progress
 * for each of the eight pipeline steps.
 *
 * Pipeline steps:
 * 1. EXTRACT — read base APK from the installed package or external source.
 * 2. DECODE — run embedded ApkTool to decode the APK.
 * 3. PATCH MANIFEST — apply package rename and feature-driven manifest patches.
 * 4. PATCH SMALI — inject OmniClone runtime hooks.
 * 5. REBUILD — run ApkTool to rebuild the patched APK.
 * 6. ZIPALIGN — align the rebuilt APK.
 * 7. SIGN — generate a unique key and sign the aligned APK.
 * 8. INSTALL — dispatch ACTION_INSTALL_PACKAGE and persist the clone config.
 */
@HiltWorker
class CloneEngine @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val manifestPatcher: ManifestPatcher,
    private val smaliInjector: SmaliInjector,
    private val apkSigner: ApkSigner,
    private val splitApkMerger: SplitApkMerger
) : CoroutineWorker(appContext, params) {

    companion object {
        const val CHANNEL_ID = "omniclone_clone_channel"
        const val NOTIFICATION_ID = 1001

        const val INPUT_CONFIG_JSON = "config_json"
        const val PROGRESS_STEP = "step"
        const val PROGRESS_MESSAGE = "message"
        const val PROGRESS_PERCENT = "percent"
        const val OUTPUT_RESULT_JSON = "result_json"

        private const val STEP_EXTRACT = "EXTRACT"
        private const val STEP_DECODE = "DECODE"
        private const val STEP_PATCH_MANIFEST = "PATCH MANIFEST"
        private const val STEP_PATCH_SMALI = "PATCH SMALI"
        private const val STEP_REBUILD = "REBUILD"
        private const val STEP_ZIPALIGN = "ZIPALIGN"
        private const val STEP_SIGN = "SIGN"
        private const val STEP_INSTALL = "INSTALL"

        private val STEP_PERCENTAGES = mapOf(
            STEP_EXTRACT to 10,
            STEP_DECODE to 25,
            STEP_PATCH_MANIFEST to 40,
            STEP_PATCH_SMALI to 55,
            STEP_REBUILD to 70,
            STEP_ZIPALIGN to 80,
            STEP_SIGN to 90,
            STEP_INSTALL to 100
        )

        /**
         * Build WorkManager input [Data] from a [CloneConfig].
         */
        fun inputData(config: CloneConfig): Data {
            return Data.Builder()
                .putString(INPUT_CONFIG_JSON, config.toJson())
                .build()
        }
    }

    private lateinit var config: CloneConfig
    private lateinit var workDir: File
    private lateinit var decodedDir: File
    private lateinit var unsignedApk: File
    private lateinit var alignedApk: File
    private lateinit var signedApk: File
    private lateinit var logs: StringBuilder

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        logs = StringBuilder()
        val configJson = inputData.getString(INPUT_CONFIG_JSON)
            ?: return@withContext fail(CloneResult.ExtractError("Missing clone config input"))

        config = try {
            CloneConfig.fromJson(configJson)
        } catch (e: Exception) {
            return@withContext fail(CloneResult.ExtractError("Invalid clone config JSON", e.message))
        }

        setForeground(createForegroundInfo(STEP_EXTRACT, "Preparing clone pipeline..."))

        workDir = File(
            applicationContext.getExternalFilesDir(null) ?: applicationContext.filesDir,
            "omniclone/work/${config.cloneId}"
        )
        decodedDir = File(workDir, "decoded")
        unsignedApk = File(workDir, "unsigned.apk")
        alignedApk = File(workDir, "aligned.apk")
        signedApk = File(workDir, "${sanitizeFileName(config.cloneName)}_${config.cloneIndex}_signed.apk")

        workDir.mkdirs()

        val result = runPipeline()

        if (result is CloneResult.Success) {
            Result.success(
                Data.Builder()
                    .putString(OUTPUT_RESULT_JSON, result.toJson())
                    .build()
            )
        } else {
            Result.failure(
                Data.Builder()
                    .putString(OUTPUT_RESULT_JSON, result.toJson())
                    .build()
            )
        }
    }

    private suspend fun runPipeline(): CloneResult {
        // Step 1 — EXTRACT
        val sourceApk = step(STEP_EXTRACT, "Extracting source APK...") {
            extractSourceApk()
        } ?: return currentFailure()

        // Step 2 — DECODE
        step(STEP_DECODE, "Decoding APK with ApkTool...") {
            decodeApk(sourceApk)
        } ?: return currentFailure()

        // Step 3 — PATCH MANIFEST
        step(STEP_PATCH_MANIFEST, "Patching AndroidManifest.xml...") {
            patchManifest()
        } ?: return currentFailure()

        // Step 4 — PATCH SMALI
        step(STEP_PATCH_SMALI, "Injecting Smali hooks...") {
            smaliInjector.inject(decodedDir, config)
        } ?: return currentFailure()

        // Step 5 — REBUILD
        step(STEP_REBUILD, "Rebuilding patched APK...") {
            rebuildApk()
        } ?: return currentFailure()

        // Step 6 — ZIPALIGN
        step(STEP_ZIPALIGN, "Aligning APK with zipalign...") {
            zipAlign()
        } ?: return currentFailure()

        // Step 7 — SIGN
        val signedPath = step(STEP_SIGN, "Signing APK...") {
            signApk()
        } ?: return currentFailure()

        // Step 8 — INSTALL
        return step(STEP_INSTALL, "Requesting installation...") {
            installApk(signedPath)
        } ?: currentFailure()
    }

    private var lastFailure: CloneResult? = null

    private fun currentFailure(): CloneResult {
        return lastFailure ?: CloneResult.ExtractError("Unknown pipeline failure")
    }

    private suspend fun <T> step(name: String, message: String, block: suspend () -> T): T? {
        if (isStopped) {
            lastFailure = CloneResult.Cancelled()
            return null
        }

        setProgress(name, message)
        setForeground(createForegroundInfo(name, message))

        return try {
            block()
        } catch (e: Exception) {
            log("Step $name failed: ${e.message}")
            lastFailure = when (name) {
                STEP_EXTRACT -> CloneResult.ExtractError(e.message ?: "Extraction failed", e.stackTraceToString())
                STEP_DECODE -> CloneResult.DecodeError(e.message ?: "Decode failed", e.stackTraceToString())
                STEP_PATCH_MANIFEST -> CloneResult.ManifestPatchError(e.message ?: "Manifest patch failed", e.stackTraceToString())
                STEP_PATCH_SMALI -> CloneResult.SmaliPatchError(e.message ?: "Smali injection failed", e.stackTraceToString())
                STEP_REBUILD -> CloneResult.RebuildError(e.message ?: "Rebuild failed", e.stackTraceToString())
                STEP_ZIPALIGN -> CloneResult.ZipAlignError(e.message ?: "Zipalign failed", e.stackTraceToString())
                STEP_SIGN -> CloneResult.SignError(e.message ?: "Signing failed", e.stackTraceToString())
                STEP_INSTALL -> CloneResult.InstallError(e.message ?: "Install failed", e.stackTraceToString())
                else -> CloneResult.ExtractError(e.message ?: "Pipeline failed", e.stackTraceToString())
            }
            null
        }
    }

    private suspend fun setProgress(step: String, message: String) {
        setProgress(
            Data.Builder()
                .putString(PROGRESS_STEP, step)
                .putString(PROGRESS_MESSAGE, message)
                .putInt(PROGRESS_PERCENT, STEP_PERCENTAGES[step] ?: 0)
                .build()
        )
    }

    private fun createForegroundInfo(step: String, message: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("OmniClone")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(100, STEP_PERCENTAGES[step] ?: 0, false)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cancel",
                WorkManager.getInstance(applicationContext)
                    .createCancelPendingIntent(id)
            )
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun fail(result: CloneResult): Result {
        lastFailure = result
        return Result.failure(
            Data.Builder()
                .putString(OUTPUT_RESULT_JSON, result.toJson())
                .build()
        )
    }

    private fun extractSourceApk(): File {
        log("Extracting source APK for ${config.originalPackage}")

        val externalSource = config.apkPath.takeIf { it.isNotBlank() && File(it).exists() }
        if (externalSource != null) {
            log("Using external APK source: $externalSource")
            return File(externalSource)
        }

        val pm = applicationContext.packageManager
        val appInfo = try {
            pm.getApplicationInfo(config.originalPackage, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            throw IllegalStateException("Original package not installed: ${config.originalPackage}")
        }

        return if (appInfo.splitSourceDirs.isNullOrEmpty()) {
            val source = File(appInfo.sourceDir)
            log("Using single APK: ${source.absolutePath}")
            source
        } else {
            log("Merging split APKs...")
            splitApkMerger.merge(appInfo, workDir)
        }
    }

    private fun decodeApk(sourceApk: File) {
        log("Decoding APK: ${sourceApk.absolutePath}")
        if (decodedDir.exists()) {
            decodedDir.deleteRecursively()
        }

        val apktoolJar = extractAssetIfNeeded("libs/apktool.jar")
        val args = arrayOf(
            "java", "-jar", apktoolJar.absolutePath,
            "d", sourceApk.absolutePath,
            "-o", decodedDir.absolutePath,
            "-f"
        )

        val result = runProcess(args, timeoutSeconds = 300)
        if (result.exitCode != 0) {
            throw IllegalStateException("ApkTool decode failed: ${result.output}\n${result.error}")
        }

        if (!File(decodedDir, "AndroidManifest.xml").exists()) {
            throw IllegalStateException("Decoded manifest missing; ApkTool may not have run")
        }
    }

    private fun patchManifest() {
        log("Patching manifest")
        val result = manifestPatcher.patch(decodedDir, config)
        if (result is PatchResult.Error) {
            throw IllegalStateException(result.message)
        }
    }

    private fun rebuildApk() {
        log("Rebuilding APK")
        if (unsignedApk.exists()) {
            unsignedApk.delete()
        }

        val apktoolJar = extractAssetIfNeeded("libs/apktool.jar")
        val args = arrayOf(
            "java", "-jar", apktoolJar.absolutePath,
            "b", decodedDir.absolutePath,
            "-o", unsignedApk.absolutePath
        )

        val result = runProcess(args, timeoutSeconds = 300)
        if (result.exitCode != 0) {
            throw IllegalStateException("ApkTool rebuild failed: ${result.output}\n${result.error}")
        }

        if (!unsignedApk.exists()) {
            throw IllegalStateException("Rebuilt APK not created")
        }
    }

    private fun zipAlign() {
        log("Aligning APK")
        if (alignedApk.exists()) {
            alignedApk.delete()
        }

        val zipalignBinary = extractZipalignBinary()
        val args = arrayOf(
            zipalignBinary.absolutePath,
            "-v", "4",
            unsignedApk.absolutePath,
            alignedApk.absolutePath
        )

        val result = runProcess(args, timeoutSeconds = 120)
        if (result.exitCode != 0) {
            throw IllegalStateException("zipalign failed: ${result.output}\n${result.error}")
        }

        if (!alignedApk.exists()) {
            throw IllegalStateException("Aligned APK not created")
        }
    }

    private fun signApk(): String {
        log("Signing APK")
        return apkSigner.sign(config.cloneId, alignedApk, signedApk)
    }

    private fun installApk(signedApkPath: String): CloneResult {
        log("Requesting install for $signedApkPath")
        val file = File(signedApkPath)
        if (!file.exists()) {
            throw IllegalStateException("Signed APK not found")
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }

        if (intent.resolveActivity(applicationContext.packageManager) == null) {
            throw IllegalStateException("No package installer found")
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            config.cloneId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            pendingIntent.send()
        } catch (e: Exception) {
            applicationContext.startActivity(intent)
        }

        return CloneResult.Success(
            signedApkPath = signedApkPath,
            installedPackage = config.clonePackage
        )
    }

    private fun extractAssetIfNeeded(assetPath: String): File {
        val dest = File(applicationContext.filesDir, "omniclone/$assetPath")
        if (dest.exists()) return dest

        dest.parentFile?.mkdirs()
        applicationContext.assets.open(assetPath).use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
        dest.setExecutable(true, false)
        return dest
    }

    private fun extractZipalignBinary(): File {
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        val assetPath = "bin/lib/$abi/libzipalign.so"
        val dest = File(applicationContext.filesDir, "omniclone/bin/zipalign_$abi")
        if (dest.exists()) return dest

        dest.parentFile?.mkdirs()
        applicationContext.assets.open(assetPath).use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
        dest.setExecutable(true, false)
        return dest
    }

    private fun runProcess(args: Array<String>, timeoutSeconds: Long): ProcessResult {
        log("Running: ${args.joinToString(" ")}")
        val pb = ProcessBuilder(*args)
            .directory(workDir)
            .redirectErrorStream(true)

        val process = pb.start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            throw IllegalStateException("Process timed out after ${timeoutSeconds}s: ${args.joinToString(" ")}")
        }
        return ProcessResult(process.exitValue(), output, "")
    }

    private fun log(message: String) {
        logs.appendLine("[${System.currentTimeMillis()}] $message")
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    }

    private data class ProcessResult(
        val exitCode: Int,
        val output: String,
        val error: String
    )
}
