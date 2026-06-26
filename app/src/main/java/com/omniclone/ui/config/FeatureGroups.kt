package com.omniclone.ui.config

import com.omniclone.model.FeatureKey

/**
 * Grouped feature toggles displayed in the clone configuration screen.
 */
object FeatureGroups {

    data class FeatureItem(
        val key: FeatureKey,
        val label: String,
        val description: String,
        val isInput: Boolean = false
    )

    data class FeatureGroup(
        val title: String,
        val items: List<FeatureItem>
    )

    val groups = listOf(
        FeatureGroup(
            title = "A — Identity & Anti-Tracking",
            items = listOf(
                FeatureItem(FeatureKey.NEW_IDENTITY, "New Identity", "Randomize all identifiers"),
                FeatureItem(FeatureKey.CHANGE_ANDROID_ID, "Android ID", "Custom/random Android ID"),
                FeatureItem(FeatureKey.CHANGE_IMEI_IMSI, "IMEI / IMSI", "Spoof telephony IDs"),
                FeatureItem(FeatureKey.CHANGE_WIFI_MAC, "Wi-Fi MAC", "Spoof Wi-Fi MAC"),
                FeatureItem(FeatureKey.CHANGE_BLUETOOTH_MAC, "Bluetooth MAC", "Spoof Bluetooth MAC"),
                FeatureItem(FeatureKey.HIDE_WIFI_INFO, "Hide Wi-Fi Info", "Hide SSID/BSSID"),
                FeatureItem(FeatureKey.HIDE_SIM_OPERATOR_INFO, "Hide SIM Info", "Hide operator/number"),
                FeatureItem(FeatureKey.CHANGE_GOOGLE_AD_ID, "Google Ad ID", "Spoof advertising ID"),
                FeatureItem(FeatureKey.CHANGE_WEBVIEW_USER_AGENT, "WebView UA", "Override WebView user agent"),
                FeatureItem(FeatureKey.CHANGE_SYSTEM_USER_AGENT, "System UA", "Override HTTP user agent"),
                FeatureItem(FeatureKey.CUSTOMIZE_BUILD_PROPS, "Customize Build Props", "Spoof model/brand"),
                FeatureItem(FeatureKey.SPOOF_ANDROID_VERSION, "Spoof Android Version", "Fake SDK/release"),
                FeatureItem(FeatureKey.HIDE_EMULATOR_DETECTION, "Hide Emulator", "Block emulator checks"),
                FeatureItem(FeatureKey.PREVENT_DEVICE_FINGERPRINTING, "Anti-Fingerprint", "Enable all privacy toggles")
            )
        ),
        FeatureGroup(
            title = "B — Privacy & Sandbox",
            items = listOf(
                FeatureItem(FeatureKey.PASSWORD_PROTECT, "PIN Lock", "Require PIN on launch"),
                FeatureItem(FeatureKey.STEALTH_MODE, "Stealth Mode", "Disguise launch"),
                FeatureItem(FeatureKey.FAKE_CALCULATOR, "Calculator Disguise", "Launch as calculator"),
                FeatureItem(FeatureKey.SPOOF_GPS_LOCATION, "Spoof GPS", "Override location"),
                FeatureItem(FeatureKey.HIDE_MOCK_LOCATION, "Hide Mock Flag", "Return false for isFromMockProvider"),
                FeatureItem(FeatureKey.FAKE_TIME_ZONE, "Fake Time Zone", "Override default timezone"),
                FeatureItem(FeatureKey.FREEZE_TIME, "Freeze Time", "Stop time progression"),
                FeatureItem(FeatureKey.PREVENT_SCREENSHOTS, "Prevent Screenshots", "Set FLAG_SECURE"),
                FeatureItem(FeatureKey.ALLOW_SCREENSHOTS, "Allow Screenshots", "Clear FLAG_SECURE"),
                FeatureItem(FeatureKey.HIDE_ROOT_PRESENCE, "Hide Root", "Block root checks"),
                FeatureItem(FeatureKey.HIDE_SPECIFIC_APPS, "Hide Specific Apps", "Filter app list"),
                FeatureItem(FeatureKey.HIDE_ALL_APPS, "Hide All Apps", "Return empty app list"),
                FeatureItem(FeatureKey.DISABLE_LOGCAT, "Disable Logcat", "No-op all Log calls"),
                FeatureItem(FeatureKey.IP_FOOTPRINT_PROTECTION, "Proxy", "Route via SOCKS5/HTTP proxy"),
                FeatureItem(FeatureKey.ADVANCED_PROTECTION, "Advanced Protection", "Enable all privacy toggles")
            )
        ),
        FeatureGroup(
            title = "C — Display & UI",
            items = listOf(
                FeatureItem(FeatureKey.RENAME_APP_LABEL, "Rename App", "Change launcher label", isInput = true),
                FeatureItem(FeatureKey.CUSTOM_LAUNCHER_ICON, "Custom Icon", "Replace launcher icon"),
                FeatureItem(FeatureKey.FORCE_DARK_MODE, "Force Dark Mode", "Enable dark theme"),
                FeatureItem(FeatureKey.KEEP_SCREEN_ON, "Keep Screen On", "Add FLAG_KEEP_SCREEN_ON"),
                FeatureItem(FeatureKey.IMMERSIVE_FULLSCREEN, "Immersive Mode", "Hide system UI"),
                FeatureItem(FeatureKey.MULTI_WINDOW, "Multi-Window", "Allow multi-window"),
                FeatureItem(FeatureKey.PICTURE_IN_PICTURE, "Picture-in-Picture", "Enable PiP"),
                FeatureItem(FeatureKey.CUSTOM_SPLASH_SCREEN, "Custom Splash", "Inject splash screen"),
                FeatureItem(FeatureKey.WELCOME_MESSAGE, "Welcome Message", "Show first-launch dialog"),
                FeatureItem(FeatureKey.ALLOW_SCREEN_RECORDING, "Allow Screen Recording", "Clear FLAG_SECURE")
            )
        ),
        FeatureGroup(
            title = "D — Media",
            items = listOf(
                FeatureItem(FeatureKey.MUTE_ON_START, "Mute on Start", "Silence on launch"),
                FeatureItem(FeatureKey.DISABLE_FRONT_CAMERA, "Disable Front Camera", "Block front camera"),
                FeatureItem(FeatureKey.DISABLE_BACK_CAMERA, "Disable Back Camera", "Block back camera"),
                FeatureItem(FeatureKey.FAKE_CAMERA_FEED, "Fake Camera", "Replace camera feed"),
                FeatureItem(FeatureKey.DISABLE_MICROPHONE, "Disable Microphone", "Block mic access"),
                FeatureItem(FeatureKey.LOCK_VOLUME_ROCKER, "Lock Volume", "Block volume changes"),
                FeatureItem(FeatureKey.DISABLE_HAPTIC_FEEDBACK, "Disable Haptics", "No-op vibrator")
            )
        ),
        FeatureGroup(
            title = "E — Navigation",
            items = listOf(
                FeatureItem(FeatureKey.CONFIRM_EXIT_DIALOG, "Confirm Exit", "Show dialog on back"),
                FeatureItem(FeatureKey.MINIMIZE_ON_BACK, "Minimize on Back", "Move task to back"),
                FeatureItem(FeatureKey.SHAKE_TO_EXIT, "Shake to Exit", "Exit on shake"),
                FeatureItem(FeatureKey.KIOSK_MODE, "Kiosk Mode", "Lock to app"),
                FeatureItem(FeatureKey.POPUP_BLOCKER, "Popup Blocker", "Auto-dismiss popups"),
                FeatureItem(FeatureKey.ACTIVITY_MONITOR, "Activity Monitor", "Log Activity starts")
            )
        ),
        FeatureGroup(
            title = "F — Storage",
            items = listOf(
                FeatureItem(FeatureKey.ISOLATED_STORAGE, "Isolated Storage", "Redirect data paths"),
                FeatureItem(FeatureKey.INSTALL_TO_SD_CARD, "Install to SD", "preferExternal"),
                FeatureItem(FeatureKey.REDIRECT_EXTERNAL_STORAGE, "Redirect External", "Override external storage"),
                FeatureItem(FeatureKey.PREVENT_APP_BACKUP, "Prevent Backup", "allowBackup=false"),
                FeatureItem(FeatureKey.CLEAR_CACHE_ON_EXIT, "Clear Cache on Exit", "Wipe cache on destroy")
            )
        ),
        FeatureGroup(
            title = "G — Launching & Behavior",
            items = listOf(
                FeatureItem(FeatureKey.REMOVE_FROM_LAUNCHER, "Hide from Launcher", "Remove LAUNCHER filter"),
                FeatureItem(FeatureKey.DISABLE_AUTO_START, "Disable Auto-Start", "Remove BOOT receivers"),
                FeatureItem(FeatureKey.MAKE_PERSISTENT, "Persistent", "Keep service running"),
                FeatureItem(FeatureKey.REQUEST_IGNORE_BATTERY, "Ignore Battery Optimizations", "Request whitelist"),
                FeatureItem(FeatureKey.MAKE_DEFAULT_HOME, "Default Home", "Add HOME category"),
                FeatureItem(FeatureKey.LAUNCH_SECRET_DIALER, "Secret Dialer Code", "Launch via *#*#code#*#*")
            )
        ),
        FeatureGroup(
            title = "H — Networking",
            items = listOf(
                FeatureItem(FeatureKey.DISABLE_ALL_NETWORKING, "Disable Networking", "Block all network calls"),
                FeatureItem(FeatureKey.DISABLE_MOBILE_DATA, "Disable Mobile Data", "Block cellular"),
                FeatureItem(FeatureKey.BLOCK_IF_NO_VPN, "Kill Switch", "Block without VPN"),
                FeatureItem(FeatureKey.MOCK_NETWORK_TYPE, "Mock Network Type", "Fake Wi-Fi/Mobile/Ethernet"),
                FeatureItem(FeatureKey.DISABLE_CLEARTEXT_HTTP, "Disable Cleartext HTTP", "Force HTTPS")
            )
        ),
        FeatureGroup(
            title = "I — Notifications",
            items = listOf(
                FeatureItem(FeatureKey.SILENCE_ALL_NOTIFICATIONS, "Silence All", "Cancel clone notifications"),
                FeatureItem(FeatureKey.NOTIFICATION_SECRET_MODE, "Secret Mode", "Replace content with dots"),
                FeatureItem(FeatureKey.ICON_BADGE, "Icon Badge", "Show badge count"),
                FeatureItem(FeatureKey.FILTER_TOASTS, "Filter Toasts", "Block toast messages"),
                FeatureItem(FeatureKey.CHANGE_TOAST_DURATION, "Toast Duration", "Override toast length")
            )
        ),
        FeatureGroup(
            title = "J — Game",
            items = listOf(
                FeatureItem(FeatureKey.COPY_OBB_FILES, "Copy OBB", "Copy expansion files"),
                FeatureItem(FeatureKey.BUNDLE_OBB_IN_APK, "Bundle OBB", "Embed OBB in APK"),
                FeatureItem(FeatureKey.FPS_MONITOR, "FPS Monitor", "Show FPS overlay")
            )
        ),
        FeatureGroup(
            title = "K — Android TV & Wear",
            items = listOf(
                FeatureItem(FeatureKey.TV_LAUNCHER_SUPPORT, "TV Launcher", "Add LEANBACK_LAUNCHER"),
                FeatureItem(FeatureKey.REMOVE_WEAR_COMPONENTS, "Remove Wear", "Strip wearable declarations"),
                FeatureItem(FeatureKey.MAKE_WEAR_APP, "Make Wear App", "Adjust for Wear OS")
            )
        ),
        FeatureGroup(
            title = "L — Automation",
            items = listOf(
                FeatureItem(FeatureKey.TOGGLE_WIFI_ON_LAUNCH, "Toggle Wi-Fi", "On launch"),
                FeatureItem(FeatureKey.TOGGLE_BLUETOOTH_ON_LAUNCH, "Toggle Bluetooth", "On launch"),
                FeatureItem(FeatureKey.SET_CLIPBOARD_ON_LAUNCH, "Set Clipboard", "On launch"),
                FeatureItem(FeatureKey.EXECUTE_TASKER, "Tasker Tasks", "Run Tasker actions"),
                FeatureItem(FeatureKey.ACTION_SEQUENCER, "Action Sequencer", "Visual automation builder")
            )
        ),
        FeatureGroup(
            title = "M — Developer",
            items = listOf(
                FeatureItem(FeatureKey.CHANGE_VERSION_NAME, "Version Name", "Override versionName", isInput = true),
                FeatureItem(FeatureKey.CHANGE_VERSION_CODE, "Version Code", "Override versionCode", isInput = true),
                FeatureItem(FeatureKey.CHANGE_TARGET_SDK, "Target SDK", "Override targetSdkVersion", isInput = true),
                FeatureItem(FeatureKey.BUILT_IN_LOGCAT_VIEWER, "Logcat Viewer", "In-app log viewer"),
                FeatureItem(FeatureKey.LIVE_ACTIVITY_MONITOR, "Live Activity Monitor", "Real-time activity log"),
                FeatureItem(FeatureKey.CLONE_CONFIG_EXPORT_IMPORT, "Export/Import Config", "JSON backup")
            )
        )
    )
}
