package com.omniclone.engine

import com.omniclone.model.CloneConfig
import com.omniclone.model.FeatureKey
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Patches the decoded AndroidManifest.xml of a cloned application.
 *
 * All modifications are performed on a DOM representation and written back to disk. The patcher
 * supports every manifest-level feature listed in the project specification.
 */
@Singleton
class ManifestPatcher @Inject constructor() {

    companion object {
        private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
        private const val TOOLS_NS = "http://schemas.android.com/tools"
        private const val OMNICLONE_PERMISSION_PREFIX = "com.omniclone.permission."
        private const val OMNICLONE_INSTRUMENTATION_CLASS = "com.omniclone.runtime.OmniCloneInstrumentation"
    }

    /**
     * Apply all manifest-level patches to the AndroidManifest.xml inside [decodedApkDir].
     *
     * @param decodedApkDir Directory containing the decoded APK (must contain AndroidManifest.xml).
     * @param config The clone configuration driving the patches.
     * @return A [PatchResult] describing success or failure.
     */
    fun patch(decodedApkDir: File, config: CloneConfig): PatchResult {
        val manifestFile = File(decodedApkDir, "AndroidManifest.xml")
        if (!manifestFile.exists()) {
            return PatchResult.Error("AndroidManifest.xml not found in ${decodedApkDir.absolutePath}")
        }

        return try {
            val doc = parseManifest(manifestFile)
            val root = doc.documentElement

            val originalPackage = root.getAttribute("package")
            if (originalPackage.isBlank()) {
                return PatchResult.Error("Original package attribute is missing")
            }

            val newPackage = config.resolvePackageName(config.clonePackage)

            // Core manifest changes
            renamePackage(root, newPackage)
            removeSharedUserId(root)
            renameAuthorities(root, originalPackage, newPackage)
            renamePermissions(root, originalPackage, newPackage)
            renameIntentDataSchemes(root, originalPackage, newPackage)
            updateApplicationName(root, newPackage)

            // Feature-driven manifest patches
            applyProviderFeatures(doc, root, config)
            applyActivityFeatures(doc, root, config)
            applyServiceFeatures(doc, root, config)
            applyReceiverFeatures(doc, root, config)
            applyPermissionFeatures(doc, root, config, originalPackage, newPackage)
            applyNetworkSecurityFeatures(doc, root, config)
            applyBackupFeatures(root, config)
            applyInstallLocation(root, config)
            applyLeanbackFeatures(root, config)
            applyWearableFeatures(root, config)
            applyLauncherFeatures(root, config)
            applyAutoStartFeatures(root, config)
            applyScreenOrientationFeatures(root, config)
            applyImmersiveFeatures(root, config)
            applyShortcutFeatures(root, config)
            applyHomeIntentFeatures(root, config)
            applyCameraIntentFeatures(root, config)
            applyAssistIntentFeatures(root, config)
            applyDialerLaunchFeatures(root, config)
            applyOutgoingCallFeatures(root, config)
            applyHeadsetLaunchFeatures(root, config)
            applyScreenOnOffFeatures(root, config)
            applySdcardMountFeatures(root, config)
            applyNfcFeatures(root, config)
            applyTileServiceFeatures(root, config)
            applyDreamServiceFeatures(root, config)
            applyNotificationListenerFeatures(root, config)
            applyAccessibilityServiceFeatures(root, config)
            applyVpnServiceFeatures(root, config)
            applyVersionFeatures(root, config)
            applyTargetSdkFeature(root, config)
            applyRemovePermissionsFeature(root, config)
            applyDisableShareActionsFeature(root, config)
            applyExcludeFromRecentsFeature(root, config)
            applyMultiWindowFeatures(root, config)
            applyNotchFeatures(root, config)
            applyAspectRatioFeatures(root, config)
            applyRtlFeatures(root, config)
            applySecretCodeFeatures(root, config)
            applyQuickSettingsTileFeatures(root, config)
            applyPersistentFeatures(root, config)
            applyDisableBackgroundServicesFeature(root, config)
            applyCustomPermissionsFeature(root, config)

            // OmniClone instrumentation permissions (always injected)
            injectInstrumentationPermissions(doc, root)

            // Label and icon replacement
            applyLabelAndIconFeatures(root, config)

            // Ensure namespace declarations exist
            ensureNamespaces(root)

            writeManifest(doc, manifestFile)
            PatchResult.Success("Manifest patched: $originalPackage -> $newPackage")
        } catch (e: Exception) {
            PatchResult.Error("Manifest patch failed", e.stackTraceToString())
        }
    }

    private fun parseManifest(manifestFile: File): Document {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isValidating = false
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        }
        return factory.newDocumentBuilder().parse(FileInputStream(manifestFile))
    }

    private fun writeManifest(doc: Document, manifestFile: File) {
        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        }
        FileOutputStream(manifestFile).use { out ->
            transformer.transform(DOMSource(doc), StreamResult(out))
        }
    }

    private fun renamePackage(root: Element, newPackage: String) {
        root.setAttribute("package", newPackage)
    }

    private fun removeSharedUserId(root: Element) {
        root.removeAttributeNS(ANDROID_NS, "sharedUserId")
    }

    private fun renameAuthorities(root: Element, oldPackage: String, newPackage: String) {
        val providerNodes = root.getElementsByTagName("provider")
        for (i in 0 until providerNodes.length) {
            val provider = providerNodes.item(i) as? Element ?: continue
            val auth = provider.getAttributeNS(ANDROID_NS, "authorities")
            if (auth.isNotBlank()) {
                val newAuth = auth.replaceAuthority(oldPackage, newPackage)
                provider.setAttributeNS(ANDROID_NS, "android:authorities", newAuth)
            }
        }

        // Also rewrite FileProvider paths XML files if present.
        val metaDataNodes = root.getElementsByTagName("meta-data")
        for (i in 0 until metaDataNodes.length) {
            val meta = metaDataNodes.item(i) as? Element ?: continue
            val name = meta.getAttributeNS(ANDROID_NS, "name")
            if (name == "android.support.FILE_PROVIDER_PATHS" || name == "androidx.core.content.FILE_PROVIDER_PATHS") {
                // No manifest change needed, but the resource XML may be patched separately.
            }
        }
    }

    private fun renamePermissions(root: Element, oldPackage: String, newPackage: String) {
        val elements = listOf("permission", "uses-permission", "permission-tree", "permission-group")
        elements.forEach { tag ->
            root.getElementsByTagName(tag).asSequence().forEach { node ->
                val element = node as? Element ?: return@forEach
                val name = element.getAttributeNS(ANDROID_NS, "name")
                if (name.startsWith(oldPackage)) {
                    element.setAttributeNS(ANDROID_NS, "android:name", newPackage + name.removePrefix(oldPackage))
                }
            }
        }
    }

    private fun renameIntentDataSchemes(root: Element, oldPackage: String, newPackage: String) {
        root.getElementsByTagName("data").asSequence().forEach { node ->
            val data = node as? Element ?: return@forEach
            val scheme = data.getAttributeNS(ANDROID_NS, "scheme")
            if (scheme.startsWith(oldPackage)) {
                data.setAttributeNS(ANDROID_NS, "android:scheme", scheme.replace(oldPackage, newPackage))
            }
        }
    }

    private fun updateApplicationName(root: Element, newPackage: String) {
        val appNodes = root.getElementsByTagName("application")
        if (appNodes.length == 0) return
        val application = appNodes.item(0) as Element

        // If no application name is present, OmniCloneApplication will be injected by SmaliInjector.
        val existingName = application.getAttributeNS(ANDROID_NS, "name")
        if (existingName.isBlank()) {
            application.setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.runtime.OmniCloneApplication")
        }
    }

    private fun applyProviderFeatures(doc: Document, root: Element, config: CloneConfig) {
        // Add FileProvider if custom icon or content sharing is requested.
        if (isEnabled(config, FeatureKey.CUSTOM_LAUNCHER_ICON) ||
            isEnabled(config, FeatureKey.ALLOW_SHARE_ON_IMAGES) ||
            isEnabled(config, FeatureKey.BUNDLE_ORIGINAL_APP)
        ) {
            addFileProvider(doc, root, config.clonePackage)
        }
    }

    private fun addFileProvider(doc: Document, root: Element, packageName: String) {
        val provider = doc.createElement("provider").apply {
            setAttributeNS(ANDROID_NS, "android:name", "androidx.core.content.FileProvider")
            setAttributeNS(ANDROID_NS, "android:authorities", "$packageName.fileprovider")
            setAttributeNS(ANDROID_NS, "android:exported", "false")
            setAttributeNS(ANDROID_NS, "android:grantUriPermissions", "true")
        }
        val metaData = doc.createElement("meta-data").apply {
            setAttributeNS(ANDROID_NS, "android:name", "android.support.FILE_PROVIDER_PATHS")
            setAttributeNS(ANDROID_NS, "android:resource", "@xml/omniclone_file_paths")
        }
        provider.appendChild(metaData)

        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        app.appendChild(provider)
    }

    private fun applyActivityFeatures(doc: Document, root: Element, config: CloneConfig) {
        val activities = root.getElementsByTagName("activity")
        for (i in 0 until activities.length) {
            val activity = activities.item(i) as? Element ?: continue

            if (isEnabled(config, FeatureKey.EXCLUDE_FROM_RECENTS)) {
                activity.setAttributeNS(ANDROID_NS, "android:excludeFromRecents", "true")
            }

            if (isEnabled(config, FeatureKey.KEEP_SCREEN_ON)) {
                activity.setAttributeNS(ANDROID_NS, "android:keepScreenOn", "true")
            }

            if (isEnabled(config, FeatureKey.ROTATION_LOCK)) {
                activity.setAttributeNS(ANDROID_NS, "android:screenOrientation", "portrait")
            }

            if (isEnabled(config, FeatureKey.FORCE_DARK_MODE) || isEnabled(config, FeatureKey.ALLOW_DARK_MODE)) {
                activity.setAttributeNS(ANDROID_NS, "android:forceDarkAllowed", "true")
            }

            if (isEnabled(config, FeatureKey.MULTI_WINDOW) || isEnabled(config, FeatureKey.FREEFORM_WINDOWING)) {
                activity.setAttributeNS(ANDROID_NS, "android:resizeableActivity", "true")
            }

            if (isEnabled(config, FeatureKey.PICTURE_IN_PICTURE) || isEnabled(config, FeatureKey.TV_PICTURE_IN_PICTURE)) {
                activity.setAttributeNS(ANDROID_NS, "android:supportsPictureInPicture", "true")
            }

            if (isEnabled(config, FeatureKey.HIDE_NOTCH)) {
                activity.setAttributeNS(ANDROID_NS, "android:windowLayoutInDisplayCutoutMode", "shortEdges")
            }

            if (isEnabled(config, FeatureKey.RTL_LAYOUT)) {
                activity.setAttributeNS(ANDROID_NS, "android:layoutDirection", "rtl")
            }

            if (isEnabled(config, FeatureKey.IMMERSIVE_FULLSCREEN)) {
                activity.setAttributeNS(ANDROID_NS, "android:theme", "@style/OmniClone.Immersive")
            }

            if (isEnabled(config, FeatureKey.FAKE_CALCULATOR)) {
                injectCalculatorLauncher(doc, root, activity)
            }

            if (isEnabled(config, FeatureKey.REMOVE_FROM_LAUNCHER)) {
                removeLauncherIntentFilter(activity)
            }

            if (isEnabled(config, FeatureKey.MAKE_DEFAULT_HOME)) {
                addIntentFilter(doc, activity, "android.intent.action.MAIN", "android.intent.category.HOME")
            }

            if (isEnabled(config, FeatureKey.MAKE_DEFAULT_CAMERA)) {
                addIntentFilter(doc, activity, "android.media.action.IMAGE_CAPTURE", null)
            }
        }
    }

    private fun injectCalculatorLauncher(doc: Document, root: Element, mainActivity: Element) {
        // Keep the original activity, but add a CalculatorActivity as the default launcher.
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val calculatorActivity = doc.createElement("activity").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.runtime.CalculatorActivity")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
            setAttributeNS(ANDROID_NS, "android:theme", "@style/OmniClone.Calculator")
        }
        val filter = doc.createElement("intent-filter").apply {
            setAttributeNS(ANDROID_NS, "android:label", "@string/app_name")
        }
        filter.appendChild(doc.createElement("action").apply {
            setAttributeNS(ANDROID_NS, "android:name", "android.intent.action.MAIN")
        })
        filter.appendChild(doc.createElement("category").apply {
            setAttributeNS(ANDROID_NS, "android:name", "android.intent.category.LAUNCHER")
        })
        calculatorActivity.appendChild(filter)
        app.appendChild(calculatorActivity)

        // Remove LAUNCHER category from the original main activity.
        removeLauncherIntentFilter(mainActivity)
    }

    private fun removeLauncherIntentFilter(activity: Element) {
        val filters = activity.getElementsByTagName("intent-filter")
        for (i in 0 until filters.length) {
            val filter = filters.item(i) as? Element ?: continue
            val hasMain = filter.getElementsByTagName("action").asSequence().any {
                (it as? Element)?.getAttributeNS(ANDROID_NS, "name") == "android.intent.action.MAIN"
            }
            val hasLauncher = filter.getElementsByTagName("category").asSequence().any {
                (it as? Element)?.getAttributeNS(ANDROID_NS, "name") == "android.intent.category.LAUNCHER"
            }
            if (hasMain && hasLauncher) {
                filter.getElementsByTagName("category").asSequence().forEach { cat ->
                    if ((cat as? Element)?.getAttributeNS(ANDROID_NS, "name") == "android.intent.category.LAUNCHER") {
                        filter.removeChild(cat)
                    }
                }
            }
        }
    }

    private fun addIntentFilter(doc: Document, activity: Element, action: String, category: String?) {
        val existing = activity.getElementsByTagName("intent-filter").asSequence().any { filter ->
            (filter as? Element)?.getElementsByTagName("action")?.asSequence()?.any {
                (it as? Element)?.getAttributeNS(ANDROID_NS, "name") == action
            } == true
        }
        if (existing) return

        val filter = doc.createElement("intent-filter")
        filter.appendChild(doc.createElement("action").apply {
            setAttributeNS(ANDROID_NS, "android:name", action)
        })
        category?.let {
            filter.appendChild(doc.createElement("category").apply {
                setAttributeNS(ANDROID_NS, "android:name", it)
            })
        }
        activity.appendChild(filter)
    }

    private fun applyServiceFeatures(doc: Document, root: Element, config: CloneConfig) {
        val services = root.getElementsByTagName("service")
        for (i in 0 until services.length) {
            val service = services.item(i) as? Element ?: continue

            if (isEnabled(config, FeatureKey.DISABLE_BACKGROUND_SERVICES)) {
                service.setAttributeNS(ANDROID_NS, "android:enabled", "false")
            }

            if (isEnabled(config, FeatureKey.MAKE_PERSISTENT)) {
                service.setAttributeNS(ANDROID_NS, "android:persistent", "true")
                service.setAttributeNS(ANDROID_NS, "android:directBootAware", "true")
            }
        }

        if (isEnabled(config, FeatureKey.INCOGNITO_MODE)) {
            // Will be handled at runtime via Smali; no manifest change required.
        }
    }

    private fun applyReceiverFeatures(doc: Document, root: Element, config: CloneConfig) {
        val receivers = root.getElementsByTagName("receiver")
        for (i in 0 until receivers.length) {
            val receiver = receivers.item(i) as? Element ?: continue

            if (isEnabled(config, FeatureKey.DISABLE_AUTO_START)) {
                val filters = receiver.getElementsByTagName("intent-filter")
                for (j in 0 until filters.length) {
                    val filter = filters.item(j) as? Element ?: continue
                    val actions = filter.getElementsByTagName("action")
                    for (k in 0 until actions.length) {
                        val action = actions.item(k) as? Element ?: continue
                        if (action.getAttributeNS(ANDROID_NS, "name") == "android.intent.action.BOOT_COMPLETED") {
                            filter.removeChild(action)
                        }
                    }
                    if (filter.childNodes.length == 0) {
                        receiver.removeChild(filter)
                    }
                }
                if (receiver.childNodes.length == 0) {
                    receiver.parentNode.removeChild(receiver)
                }
            }

            if (isEnabled(config, FeatureKey.DISABLE_SCREEN_EVENTS)) {
                val filters = receiver.getElementsByTagName("intent-filter")
                val toRemove = mutableListOf<Element>()
                for (j in 0 until filters.length) {
                    val filter = filters.item(j) as? Element ?: continue
                    val hasScreenAction = filter.getElementsByTagName("action").asSequence().any {
                        val name = (it as? Element)?.getAttributeNS(ANDROID_NS, "name") ?: ""
                        name == "android.intent.action.SCREEN_ON" || name == "android.intent.action.SCREEN_OFF"
                    }
                    if (hasScreenAction) toRemove.add(filter)
                }
                toRemove.forEach { receiver.removeChild(it) }
            }
        }

        if (isEnabled(config, FeatureKey.EXIT_ON_SCREEN_OFF)) {
            val app = root.getElementsByTagName("application").item(0) as? Element ?: return
            val receiver = doc.createElement("receiver").apply {
                setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.runtime.ScreenOffReceiver")
                setAttributeNS(ANDROID_NS, "android:exported", "false")
            }
            val filter = doc.createElement("intent-filter").apply {
                appendChild(doc.createElement("action").apply {
                    setAttributeNS(ANDROID_NS, "android:name", "android.intent.action.SCREEN_OFF")
                })
            }
            receiver.appendChild(filter)
            app.appendChild(receiver)
        }
    }

    private fun applyPermissionFeatures(
        doc: Document,
        root: Element,
        config: CloneConfig,
        oldPackage: String,
        newPackage: String
    ) {
        if (isEnabled(config, FeatureKey.DISABLE_ACCOUNTS_ACCESS)) {
            removeUsesPermission(root, "android.permission.GET_ACCOUNTS")
            removeUsesPermission(root, "android.permission.GET_ACCOUNTS_PRIVILEGED")
        }

        if (isEnabled(config, FeatureKey.DISABLE_CONTACTS_ACCESS)) {
            removeUsesPermission(root, "android.permission.READ_CONTACTS")
            removeUsesPermission(root, "android.permission.WRITE_CONTACTS")
        }

        if (isEnabled(config, FeatureKey.DISABLE_CALENDAR_ACCESS)) {
            removeUsesPermission(root, "android.permission.READ_CALENDAR")
            removeUsesPermission(root, "android.permission.WRITE_CALENDAR")
        }

        if (isEnabled(config, FeatureKey.DISABLE_CALL_LOG_SMS_ACCESS)) {
            removeUsesPermission(root, "android.permission.READ_CALL_LOG")
            removeUsesPermission(root, "android.permission.WRITE_CALL_LOG")
            removeUsesPermission(root, "android.permission.READ_SMS")
            removeUsesPermission(root, "android.permission.SEND_SMS")
            removeUsesPermission(root, "android.permission.RECEIVE_SMS")
        }

        if (isEnabled(config, FeatureKey.DISABLE_MEDIA_ACCESS)) {
            removeUsesPermission(root, "android.permission.READ_MEDIA_IMAGES")
            removeUsesPermission(root, "android.permission.READ_MEDIA_VIDEO")
            removeUsesPermission(root, "android.permission.READ_MEDIA_AUDIO")
            removeUsesPermission(root, "android.permission.READ_EXTERNAL_STORAGE")
            removeUsesPermission(root, "android.permission.WRITE_EXTERNAL_STORAGE")
        }

        if (isEnabled(config, FeatureKey.ACCESS_FINE_LOCATION) ||
            isEnabled(config, FeatureKey.SPOOF_GPS_LOCATION)
        ) {
            addUsesPermission(doc, root, "android.permission.ACCESS_FINE_LOCATION")
            addUsesPermission(doc, root, "android.permission.ACCESS_COARSE_LOCATION")
            if (isEnabled(config, FeatureKey.ACCESS_MOCK_LOCATION)) {
                addUsesPermission(doc, root, "android.permission.ACCESS_MOCK_LOCATION")
            }
        }

        if (isEnabled(config, FeatureKey.FAKE_CAMERA_FEED) ||
            isEnabled(config, FeatureKey.DISABLE_FRONT_CAMERA) ||
            isEnabled(config, FeatureKey.DISABLE_BACK_CAMERA) ||
            isEnabled(config, FeatureKey.PREFERRED_CAMERA_APP)
        ) {
            addUsesPermission(doc, root, "android.permission.CAMERA")
        }

        if (isEnabled(config, FeatureKey.AUDIO_PLAYBACK_CAPTURE)) {
            addUsesPermission(doc, root, "android.permission.MODIFY_AUDIO_SETTINGS")
            addUsesPermission(doc, root, "android.permission.RECORD_AUDIO")
        }

        if (isEnabled(config, FeatureKey.LAUNCH_NFC_TAG)) {
            addUsesPermission(doc, root, "android.permission.NFC")
        }

        if (isEnabled(config, FeatureKey.CHANGE_BLUETOOTH_MAC) ||
            isEnabled(config, FeatureKey.TOGGLE_BLUETOOTH_ON_LAUNCH)
        ) {
            addUsesPermission(doc, root, "android.permission.BLUETOOTH")
            addUsesPermission(doc, root, "android.permission.BLUETOOTH_CONNECT")
        }

        if (isEnabled(config, FeatureKey.CHANGE_IMEI_IMSI)) {
            addUsesPermission(doc, root, "android.permission.READ_PHONE_STATE")
        }

        // Inject OmniClone runtime permissions.
        addUsesPermission(doc, root, OMNICLONE_PERMISSION_PREFIX + "RUNTIME_HOOKS")
    }

    private fun addUsesPermission(doc: Document, root: Element, permission: String) {
        val existing = root.getElementsByTagName("uses-permission").asSequence().any {
            (it as? Element)?.getAttributeNS(ANDROID_NS, "name") == permission
        }
        if (existing) return

        val element = doc.createElement("uses-permission").apply {
            setAttributeNS(ANDROID_NS, "android:name", permission)
        }
        root.insertBefore(element, root.firstChild)
    }

    private fun removeUsesPermission(root: Element, permission: String) {
        val toRemove = root.getElementsByTagName("uses-permission").asSequence().filter {
            (it as? Element)?.getAttributeNS(ANDROID_NS, "name") == permission
        }.toList()
        toRemove.forEach { node ->
            node.parentNode.removeChild(node)
        }
    }

    private fun applyNetworkSecurityFeatures(doc: Document, root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.DISABLE_CLEARTEXT_HTTP)) return

        val existing = root.getElementsByTagName("network-security-config").length > 0
        if (existing) return

        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        app.setAttributeNS(ANDROID_NS, "android:networkSecurityConfig", "@xml/omniclone_network_security")

        // Resource XML is written by a separate helper if it does not exist.
    }

    private fun applyBackupFeatures(root: Element, config: CloneConfig) {
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        if (isEnabled(config, FeatureKey.PREVENT_APP_BACKUP)) {
            app.setAttributeNS(ANDROID_NS, "android:allowBackup", "false")
        }
    }

    private fun applyInstallLocation(root: Element, config: CloneConfig) {
        if (isEnabled(config, FeatureKey.INSTALL_TO_SD_CARD)) {
            root.setAttributeNS(ANDROID_NS, "android:installLocation", "preferExternal")
        }
    }

    private fun applyLeanbackFeatures(root: Element, config: CloneConfig) {
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return

        if (isEnabled(config, FeatureKey.TV_LAUNCHER_SUPPORT) ||
            isEnabled(config, FeatureKey.TV_VERSION_ON_PHONE)
        ) {
            app.setAttributeNS(ANDROID_NS, "android:banner", "@drawable/omniclone_tv_banner")
        }

        if (isEnabled(config, FeatureKey.TV_VERSION_ON_PHONE)) {
            root.getElementsByTagName("uses-feature").asSequence().forEach { node ->
                val feature = node as? Element ?: return@forEach
                if (feature.getAttributeNS(ANDROID_NS, "name") == "android.software.leanback") {
                    feature.setAttributeNS(ANDROID_NS, "android:required", "false")
                }
            }
        }
    }

    private fun applyWearableFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.REMOVE_WEAR_COMPONENTS)) return

        val wearNodes = listOf(
            "uses-feature",
            "uses-library"
        )
        wearNodes.forEach { tag ->
            root.getElementsByTagName(tag).asSequence().filter { node ->
                val name = (node as? Element)?.getAttributeNS(ANDROID_NS, "name") ?: ""
                name.contains("wearable") || name.contains("watch")
            }.forEach { node ->
                node.parentNode.removeChild(node)
            }
        }
    }

    private fun applyLauncherFeatures(root: Element, config: CloneConfig) {
        // TV launcher support handled via intent-filter on main activity.
        if (!isEnabled(config, FeatureKey.TV_LAUNCHER_SUPPORT)) return

        val mainActivity = findMainActivity(root) ?: return
        val existing = mainActivity.getElementsByTagName("intent-filter").asSequence().any { filter ->
            (filter as? Element)?.getElementsByTagName("category")?.asSequence()?.any {
                (it as? Element)?.getAttributeNS(ANDROID_NS, "name") == "android.intent.category.LEANBACK_LAUNCHER"
            } == true
        }
        if (existing) return

        val doc = root.ownerDocument
        val filter = doc.createElement("intent-filter").apply {
            appendChild(doc.createElement("action").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.intent.action.MAIN")
            })
            appendChild(doc.createElement("category").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.intent.category.LEANBACK_LAUNCHER")
            })
        }
        mainActivity.appendChild(filter)
    }

    private fun findMainActivity(root: Element): Element? {
        val activities = root.getElementsByTagName("activity")
        for (i in 0 until activities.length) {
            val activity = activities.item(i) as? Element ?: continue
            val isMain = activity.getElementsByTagName("intent-filter").asSequence().any { filter ->
                val f = filter as? Element ?: return@any false
                val hasMain = f.getElementsByTagName("action").asSequence().any {
                    (it as? Element)?.getAttributeNS(ANDROID_NS, "name") == "android.intent.action.MAIN"
                }
                val hasLauncher = f.getElementsByTagName("category").asSequence().any {
                    (it as? Element)?.getAttributeNS(ANDROID_NS, "name") == "android.intent.category.LAUNCHER"
                }
                hasMain && hasLauncher
            }
            if (isMain) return activity
        }
        return activities.item(0) as? Element
    }

    private fun applyAutoStartFeatures(root: Element, config: CloneConfig) {
        // Handled by removing BOOT_COMPLETED receivers.
    }

    private fun applyScreenOrientationFeatures(root: Element, config: CloneConfig) {
        // Applied per activity in applyActivityFeatures.
    }

    private fun applyImmersiveFeatures(root: Element, config: CloneConfig) {
        // Applied per activity in applyActivityFeatures.
    }

    private fun applyShortcutFeatures(root: Element, config: CloneConfig) {
        // Runtime feature; manifest injection not required.
    }

    private fun applyHomeIntentFeatures(root: Element, config: CloneConfig) {
        // Applied per activity in applyActivityFeatures.
    }

    private fun applyCameraIntentFeatures(root: Element, config: CloneConfig) {
        // Applied per activity in applyActivityFeatures.
    }

    private fun applyAssistIntentFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.MAKE_DEFAULT_ASSIST)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        app.setAttributeNS(ANDROID_NS, "android:voiceInteractionService", "com.omniclone.runtime.OmniVoiceInteractionService")
    }

    private fun applyDialerLaunchFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.LAUNCH_SECRET_DIALER)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val doc = root.ownerDocument
        val receiver = doc.createElement("receiver").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.runtime.SecretCodeReceiver")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
        }
        val filter = doc.createElement("intent-filter").apply {
            appendChild(doc.createElement("action").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.provider.Telephony.SECRET_CODE")
            })
            appendChild(doc.createElement("data").apply {
                setAttributeNS(ANDROID_NS, "android:scheme", "android_secret_code")
                setAttributeNS(ANDROID_NS, "android:host", "6666")
            })
        }
        receiver.appendChild(filter)
        app.appendChild(receiver)
    }

    private fun applyOutgoingCallFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.LAUNCH_OUTGOING_CALL)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val doc = root.ownerDocument
        val receiver = doc.createElement("receiver").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.runtime.OutgoingCallReceiver")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
        }
        val filter = doc.createElement("intent-filter").apply {
            appendChild(doc.createElement("action").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.intent.action.NEW_OUTGOING_CALL")
            })
        }
        receiver.appendChild(filter)
        app.appendChild(receiver)
    }

    private fun applyHeadsetLaunchFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.START_EXIT_HEADPHONE)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val doc = root.ownerDocument
        val receiver = doc.createElement("receiver").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.runtime.HeadsetEventReceiver")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
        }
        val filter = doc.createElement("intent-filter").apply {
            appendChild(doc.createElement("action").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.intent.action.HEADSET_PLUG")
            })
        }
        receiver.appendChild(filter)
        app.appendChild(receiver)
    }

    private fun applyScreenOnOffFeatures(root: Element, config: CloneConfig) {
        // Applied by removing receivers when DISABLE_SCREEN_EVENTS is on.
    }

    private fun applySdcardMountFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.LAUNCH_SD_MOUNTED)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val doc = root.ownerDocument
        val receiver = doc.createElement("receiver").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.runtime.SdCardMountedReceiver")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
        }
        val filter = doc.createElement("intent-filter").apply {
            appendChild(doc.createElement("action").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.intent.action.MEDIA_MOUNTED")
            })
            appendChild(doc.createElement("data").apply {
                setAttributeNS(ANDROID_NS, "android:scheme", "file")
            })
        }
        receiver.appendChild(filter)
        app.appendChild(receiver)
    }

    private fun applyNfcFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.LAUNCH_NFC_TAG)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val doc = root.ownerDocument
        val activity = doc.createElement("activity").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.runtime.NfcDispatchActivity")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
            setAttributeNS(ANDROID_NS, "android:theme", "@android:style/Theme.NoDisplay")
        }
        val filter = doc.createElement("intent-filter").apply {
            appendChild(doc.createElement("action").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.nfc.action.NDEF_DISCOVERED")
            })
            appendChild(doc.createElement("category").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.intent.category.DEFAULT")
            })
            appendChild(doc.createElement("data").apply {
                setAttributeNS(ANDROID_NS, "android:mimeType", "*/*")
            })
        }
        activity.appendChild(filter)
        app.appendChild(activity)
    }

    private fun applyTileServiceFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.QUICK_SETTINGS_TILE)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val doc = root.ownerDocument
        val service = doc.createElement("service").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.runtime.OmniTileService")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
            setAttributeNS(ANDROID_NS, "android:permission", "android.permission.BIND_QUICK_SETTINGS_TILE")
        }
        val filter = doc.createElement("intent-filter").apply {
            appendChild(doc.createElement("action").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.service.quicksettings.action.QS_TILE")
            })
        }
        service.appendChild(filter)
        app.appendChild(service)
    }

    private fun applyDreamServiceFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.SCREEN_SAVER)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val doc = root.ownerDocument
        val service = doc.createElement("service").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.runtime.OmniDreamService")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
            setAttributeNS(ANDROID_NS, "android:permission", "android.permission.BIND_DREAM_SERVICE")
        }
        val filter = doc.createElement("intent-filter").apply {
            appendChild(doc.createElement("action").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.service.dreams.DreamService")
            })
        }
        service.appendChild(filter)
        app.appendChild(service)
    }

    private fun applyNotificationListenerFeatures(root: Element, config: CloneConfig) {
        if (!hasNotificationFeatures(config)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val doc = root.ownerDocument
        val service = doc.createElement("service").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.service.OmniNotificationListener")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
            setAttributeNS(ANDROID_NS, "android:permission", "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE")
        }
        val filter = doc.createElement("intent-filter").apply {
            appendChild(doc.createElement("action").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.service.notification.NotificationListenerService")
            })
        }
        service.appendChild(filter)
        app.appendChild(service)
    }

    private fun hasNotificationFeatures(config: CloneConfig): Boolean {
        return config.features.keys.any { key ->
            when (key) {
                FeatureKey.FILTER_NOTIFICATIONS_KEYWORD,
                FeatureKey.QUIET_TIME_SCHEDULE,
                FeatureKey.SILENCE_ALL_NOTIFICATIONS,
                FeatureKey.CHANGE_VIBRATION_PATTERN,
                FeatureKey.CHANGE_NOTIFICATION_LED,
                FeatureKey.SNOOZE_NOTIFICATIONS,
                FeatureKey.NOTIFICATION_TIMEOUT,
                FeatureKey.CHANGE_NOTIFICATION_VISIBILITY,
                FeatureKey.CHANGE_NOTIFICATION_PRIORITY,
                FeatureKey.REMOVE_NOTIFICATION_ACTIONS,
                FeatureKey.REPLACE_NOTIFICATION_ICONS,
                FeatureKey.SINGLE_NOTIFICATION_GROUP,
                FeatureKey.CHANGE_NOTIFICATION_TEXT,
                FeatureKey.NOTIFICATION_SECRET,
                FeatureKey.NOTIFICATION_SECRET_MODE,
                FeatureKey.ICON_BADGE -> true
                else -> false
            }
        }
    }

    private fun applyAccessibilityServiceFeatures(root: Element, config: CloneConfig) {
        if (!hasAccessibilityFeatures(config)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val doc = root.ownerDocument
        val service = doc.createElement("service").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.service.OmniAccessibilityService")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
            setAttributeNS(ANDROID_NS, "android:permission", "android.permission.BIND_ACCESSIBILITY_SERVICE")
        }
        val metaData = doc.createElement("meta-data").apply {
            setAttributeNS(ANDROID_NS, "android:name", "android.accessibilityservice")
            setAttributeNS(ANDROID_NS, "android:resource", "@xml/omniclone_accessibility_service")
        }
        service.appendChild(metaData)
        app.appendChild(service)
    }

    private fun hasAccessibilityFeatures(config: CloneConfig): Boolean {
        return config.features.keys.any { key ->
            when (key) {
                FeatureKey.IN_APP_FLOATING_KEYBOARD,
                FeatureKey.MODIFY_VIEWS,
                FeatureKey.COLOR_FILTER_OVERLAY,
                FeatureKey.LIVE_CHAT_OVERLAY,
                FeatureKey.FLOATING_BACK_BUTTON,
                FeatureKey.SWIPE_EDGE_BACK,
                FeatureKey.AUTO_PRESS_BUTTONS,
                FeatureKey.AUTO_FILL_FORMS,
                FeatureKey.AUTO_SCROLLER,
                FeatureKey.VIEW_HIERARCHY_INSPECTOR,
                FeatureKey.SEARCH_VIEW_HIERARCHY,
                FeatureKey.REPLACE_WIDGET_TEXT,
                FeatureKey.RESTYLE_WIDGETS,
                FeatureKey.POPUP_BLOCKER,
                FeatureKey.KIOSK_MODE,
                FeatureKey.FLOATING_APP_WINDOW,
                FeatureKey.MUTE_BASED_ON_TEXT,
                FeatureKey.VOLUME_ROCKER_OVERLAY,
                FeatureKey.FPS_MONITOR,
                FeatureKey.JOYSTICK_POINTER,
                FeatureKey.SHOW_IP_INFO_OVERLAY -> true
                else -> false
            }
        }
    }

    private fun applyVpnServiceFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.BLOCK_IF_NO_VPN) &&
            !isEnabled(config, FeatureKey.IP_FOOTPRINT_PROTECTION)
        ) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val doc = root.ownerDocument
        val service = doc.createElement("service").apply {
            setAttributeNS(ANDROID_NS, "android:name", "com.omniclone.service.OmniVpnService")
            setAttributeNS(ANDROID_NS, "android:exported", "true")
            setAttributeNS(ANDROID_NS, "android:permission", "android.permission.BIND_VPN_SERVICE")
        }
        val filter = doc.createElement("intent-filter").apply {
            appendChild(doc.createElement("action").apply {
                setAttributeNS(ANDROID_NS, "android:name", "android.net.VpnService")
            })
        }
        service.appendChild(filter)
        app.appendChild(service)
    }

    private fun applyVersionFeatures(root: Element, config: CloneConfig) {
        config.features[FeatureKey.CHANGE_VERSION_NAME]?.let { value ->
            if (value.isNotBlank()) {
                root.setAttributeNS(ANDROID_NS, "android:versionName", value)
            }
        }
        config.features[FeatureKey.CHANGE_VERSION_CODE]?.let { value ->
            if (value.isNotBlank()) {
                root.setAttributeNS(ANDROID_NS, "android:versionCode", value)
            }
        }
    }

    private fun applyTargetSdkFeature(root: Element, config: CloneConfig) {
        config.features[FeatureKey.CHANGE_TARGET_SDK]?.let { value ->
            if (value.isNotBlank()) {
                val usesSdk = root.getElementsByTagName("uses-sdk").item(0) as? Element
                if (usesSdk != null) {
                    usesSdk.setAttributeNS(ANDROID_NS, "android:targetSdkVersion", value)
                }
            }
        }
    }

    private fun applyRemovePermissionsFeature(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.REMOVE_PERMISSIONS)) return

        val permissionsToRemove = config.features[FeatureKey.REMOVE_PERMISSIONS]
            ?.split(",")
            ?.map { it.trim() }
            ?: return

        permissionsToRemove.forEach { permission ->
            removeUsesPermission(root, permission)
        }
    }

    private fun applyDisableShareActionsFeature(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.DISABLE_SHARE_ACTIONS)) return

        root.getElementsByTagName("activity").asSequence().forEach { node ->
            val activity = node as? Element ?: return@forEach
            val filters = activity.getElementsByTagName("intent-filter")
            val toRemove = mutableListOf<Element>()
            for (i in 0 until filters.length) {
                val filter = filters.item(i) as? Element ?: continue
                val hasSend = filter.getElementsByTagName("action").asSequence().any {
                    (it as? Element)?.getAttributeNS(ANDROID_NS, "name") == "android.intent.action.SEND"
                }
                if (hasSend) toRemove.add(filter)
            }
            toRemove.forEach { activity.removeChild(it) }
        }
    }

    private fun applyExcludeFromRecentsFeature(root: Element, config: CloneConfig) {
        // Applied per activity.
    }

    private fun applyMultiWindowFeatures(root: Element, config: CloneConfig) {
        // Applied per activity.
    }

    private fun applyNotchFeatures(root: Element, config: CloneConfig) {
        // Applied per activity.
    }

    private fun applyAspectRatioFeatures(root: Element, config: CloneConfig) {
        if (!isEnabled(config, FeatureKey.LARGE_ASPECT_RATIO)) return
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        app.removeAttributeNS(ANDROID_NS, "maxAspectRatio")
    }

    private fun applyRtlFeatures(root: Element, config: CloneConfig) {
        // Applied per activity.
    }

    private fun applySecretCodeFeatures(root: Element, config: CloneConfig) {
        // Handled by applyDialerLaunchFeatures.
    }

    private fun applyQuickSettingsTileFeatures(root: Element, config: CloneConfig) {
        // Handled by applyTileServiceFeatures.
    }

    private fun applyPersistentFeatures(root: Element, config: CloneConfig) {
        // Applied per service.
    }

    private fun applyDisableBackgroundServicesFeature(root: Element, config: CloneConfig) {
        // Applied per service.
    }

    private fun applyCustomPermissionsFeature(doc: Document, root: Element, config: CloneConfig) {
        val customPermissions = config.features[FeatureKey.CUSTOM_PERMISSIONS] ?: return
        if (customPermissions.isBlank()) return

        customPermissions.split("\n").forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank()) return@forEach
            val parts = trimmed.split("=")
            val name = parts.getOrNull(0)?.trim() ?: return@forEach
            val protectionLevel = parts.getOrNull(1)?.trim() ?: "normal"

            val existing = root.getElementsByTagName("permission").asSequence().any {
                (it as? Element)?.getAttributeNS(ANDROID_NS, "name") == name
            }
            if (existing) return@forEach

            val permission = doc.createElement("permission").apply {
                setAttributeNS(ANDROID_NS, "android:name", name)
                setAttributeNS(ANDROID_NS, "android:protectionLevel", protectionLevel)
            }
            root.insertBefore(permission, root.firstChild)
        }
    }

    private fun injectInstrumentationPermissions(doc: Document, root: Element) {
        val permissions = listOf(
            "RUNTIME_HOOKS",
            "ACCESS_ALL_APPS",
            "MODIFY_SYSTEM_STATE"
        )
        permissions.forEach { perm ->
            addUsesPermission(doc, root, OMNICLONE_PERMISSION_PREFIX + perm)
        }

        val app = root.getElementsByTagName("application").item(0) as? Element ?: return
        val instrumentation = doc.createElement("instrumentation").apply {
            setAttributeNS(ANDROID_NS, "android:name", OMNICLONE_INSTRUMENTATION_CLASS)
            setAttributeNS(ANDROID_NS, "android:targetPackage", root.getAttribute("package"))
            setAttributeNS(ANDROID_NS, "android:functionalTest", "false")
            setAttributeNS(ANDROID_NS, "android:handleProfiling", "true")
        }
        app.appendChild(instrumentation)
    }

    private fun applyLabelAndIconFeatures(root: Element, config: CloneConfig) {
        val app = root.getElementsByTagName("application").item(0) as? Element ?: return

        if (isEnabled(config, FeatureKey.RENAME_APP_LABEL)) {
            config.cloneName.takeIf { it.isNotBlank() }?.let { name ->
                app.setAttributeNS(ANDROID_NS, "android:label", name)
            }
        }

        if (isEnabled(config, FeatureKey.CUSTOM_LAUNCHER_ICON)) {
            config.iconPath?.takeIf { it.isNotBlank() }?.let {
                app.setAttributeNS(ANDROID_NS, "android:icon", "@mipmap/omniclone_custom_icon")
                app.setAttributeNS(ANDROID_NS, "android:roundIcon", "@mipmap/omniclone_custom_icon_round")
            }
        }
    }

    private fun ensureNamespaces(root: Element) {
        if (root.getAttribute("xmlns:android").isBlank()) {
            root.setAttribute("xmlns:android", ANDROID_NS)
        }
        if (root.getAttribute("xmlns:tools").isBlank()) {
            root.setAttribute("xmlns:tools", TOOLS_NS)
        }
    }

    private fun isEnabled(config: CloneConfig, key: FeatureKey): Boolean {
        return config.features[key]?.toBoolean() == true
    }

    private fun NodeList.asSequence(): Sequence<Node> = sequence {
        for (i in 0 until length) {
            yield(item(i))
        }
    }

    private fun String.replaceAuthority(oldPackage: String, newPackage: String): String {
        return split(";").joinToString(";") { authority ->
            when {
                authority == oldPackage -> newPackage
                authority.startsWith("$oldPackage.") -> newPackage + authority.removePrefix(oldPackage)
                authority.contains(oldPackage) -> authority.replace(oldPackage, newPackage)
                else -> authority
            }
        }
    }
}
