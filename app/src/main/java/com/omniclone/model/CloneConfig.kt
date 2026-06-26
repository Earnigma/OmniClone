package com.omniclone.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Represents the configuration for a single cloned application.
 *
 * Stored in Room as the `clones` table and also serializable so it can be passed to WorkManager.
 */
@Serializable
@Entity(tableName = "clones")
data class CloneConfig(
    @PrimaryKey val cloneId: String,
    val originalPackage: String,
    val cloneName: String,
    val clonePackage: String,
    val cloneIndex: Int,
    val iconPath: String? = null,
    val badgeText: String? = null,
    val badgeColor: Int? = null,
    val installedAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val apkPath: String = "",
    val features: Map<FeatureKey, String> = emptyMap(),
    val identity: IdentityConfig = IdentityConfig(),
    val proxyHost: String? = null,
    val proxyPort: Int? = null,
    val gpxTrackPath: String? = null,
    val automations: List<AutomationSequence> = emptyList()
) {
    /**
     * Serialize the configuration to a JSON string suitable for WorkManager Data.
     */
    fun toJson(): String = Json.encodeToString(this)

    /**
     * Return the effective custom package name, resolving `{pkg}` and `{index}` placeholders.
     */
    fun resolvePackageName(customTemplate: String?): String {
        if (customTemplate.isNullOrBlank()) return clonePackage
        return customTemplate
            .replace("{pkg}", originalPackage)
            .replace("{index}", cloneIndex.toString())
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        }

        fun fromJson(jsonString: String): CloneConfig = json.decodeFromString(jsonString)
    }
}

/**
 * Strongly typed feature identifiers. Each key maps to a string value in the [CloneConfig.features]
 * map. For boolean toggles the value is `"true"` / `"false"`; other features may store a custom
 * string such as a MAC address or latitude.
 */
@Serializable
enum class FeatureKey {
    // Module A — Identity & Anti-Tracking
    NEW_IDENTITY,
    QR_IDENTITY_TRANSFER,
    SHOW_CURRENT_IDENTITY,
    CHANGE_ANDROID_ID,
    CHANGE_IMEI_IMSI,
    CHANGE_WIFI_MAC,
    CHANGE_BLUETOOTH_MAC,
    ACCESS_BLUETOOTH,
    CHANGE_ETHERNET_MAC,
    HIDE_WIFI_INFO,
    HIDE_SIM_OPERATOR_INFO,
    HIDE_DNS_SERVERS,
    HIDE_CPU_GPU_INFO,
    CHANGE_GSF_ID,
    CHANGE_GOOGLE_AD_ID,
    CHANGE_AMAZON_AD_ID,
    CHANGE_FACEBOOK_ATTRIBUTION_ID,
    CHANGE_APP_SET_ID,
    CHANGE_OAID,
    CHANGE_WEBVIEW_USER_AGENT,
    CHANGE_SYSTEM_USER_AGENT,
    RANDOMIZE_USER_CREATION_TIME,
    RANDOMIZE_DEVICE_UPTIME,
    RANDOMIZE_FILE_TIMESTAMPS,
    RANDOMIZE_BOOT_ID,
    RANDOMIZE_BLUETOOTH_DISCOVERED,
    RANDOMIZE_DRM_IDS,
    RANDOMIZE_MEDIA_STORE_VERSION,
    RANDOMIZE_MEMORY_STORAGE,
    RANDOMIZE_BATTERY_LEVEL,
    RANDOMIZE_BUILD_PROPS,
    CUSTOMIZE_BUILD_PROPS,
    SPOOF_ANDROID_VERSION,
    HIDE_EMULATOR_DETECTION,
    PREVENT_DEVICE_FINGERPRINTING,
    SHOW_DEVICE_TAG,

    // Module B — Privacy & Sandbox
    PASSWORD_PROTECT,
    PATTERN_LOCK,
    OTP_PROTECT,
    STEALTH_MODE,
    FAKE_CALCULATOR,
    PANIC_MODE,
    INCOGNITO_MODE,
    INCOGNITO_KEYBOARD,
    FLOATING_KEYBOARD,
    IN_APP_FLOATING_KEYBOARD,
    DISABLE_ACCOUNTS_ACCESS,
    DISABLE_CONTACTS_ACCESS,
    DISABLE_CALENDAR_ACCESS,
    DISABLE_CALL_LOG_SMS_ACCESS,
    DISABLE_CLIPBOARD_ACCESS,
    EXCLUDE_FROM_RECENTS,
    REMOVE_PERMISSIONS,
    DISABLE_PERMISSION_PROMPTS,
    AUTO_GRANT_PERMISSIONS,
    SPOOF_GPS_LOCATION,
    GPS_RANDOMIZE,
    GPS_UNCERTAINTY,
    GPS_BEARING,
    GPS_API_ACCESS,
    ACCESS_FINE_LOCATION,
    ACCESS_MOCK_LOCATION,
    GPS_FAVORITES,
    GPS_GPX_TRACK,
    HIDE_MOCK_LOCATION,
    FAKE_TIME_ZONE,
    FAKE_DATE,
    FAKE_TIME,
    FREEZE_TIME,
    FAKE_BRIGHTNESS,
    FAKE_VOLUME,
    DISABLE_SENSOR_ACCESS,
    FAKE_ENVIRONMENT_SENSORS,
    DISABLE_ACCESSIBILITY_DETECTION,
    BYPASS_ACCESSIBILITY_CHECK,
    PREVENT_ACCESSIBILITY_DISCOVERY,
    PREVENT_SCREENSHOTS,
    ALLOW_SCREENSHOTS,
    PREVENT_SCREENSHOT_DETECTION,
    PREVENT_SCREEN_RECORDING_DETECT,
    DISABLE_AUTO_FILL,
    EXIT_ON_SCREEN_OFF,
    SNEEZE_SHAKE_TO_EXIT,
    HIDE_ROOT_PRESENCE,
    HIDE_SPECIFIC_APPS,
    HIDE_ALL_APPS,
    DISABLE_LOGCAT,
    DISABLE_SHARE_ACTIONS,
    DISABLE_DEVICE_ADMIN_DETECTION,
    CHANGE_KNOX_WARRANTY,
    NOTIFICATION_SECRET_MODE,
    WEBRTC_LEAK_GUARD,
    IP_FOOTPRINT_PROTECTION,
    BLOCK_NON_VPN_TRAFFIC,
    DISABLE_UPNP,
    DISABLE_BIOMETRIC,
    DISABLE_FONT_ACCESS,
    INSTALLER_SOURCE_SPOOFING,
    FIREBASE_AUTH_INDICATOR,
    UNRESTRICTED_BATTERY,
    ADVANCED_PROTECTION,
    DATA_DIRECTORY_WORKAROUND,
    HIDE_APP_SUGGESTIONS,
    DEVICE_SECURE_ENFORCEMENT,
    OVERRIDE_LINK_HANDLING,
    CHANGE_DEFAULT_BROWSER,
    AUDIT_LINK_HANDLING,

    // Module C — Display & UI
    RENAME_APP_LABEL,
    CUSTOM_LAUNCHER_ICON,
    ICON_BADGE_OVERLAY,
    CHANGE_STATUS_BAR_COLOR,
    CHANGE_NAV_BAR_COLOR,
    CHANGE_TOOLBAR_COLOR,
    INVERT_COLORS,
    FORCE_DARK_MODE,
    ALLOW_DARK_MODE,
    COLOR_FILTER_OVERLAY,
    ROTATION_LOCK,
    MODIFY_VIEWS,
    CHANGE_DISPLAY_DENSITY,
    CHANGE_FONT_SIZE,
    CHANGE_APP_LANGUAGE,
    KEEP_SCREEN_ON,
    IMMERSIVE_FULLSCREEN,
    FLOATING_WINDOW,
    FLOATING_APP_WINDOW,
    FREEFORM_WINDOWING,
    MULTI_WINDOW,
    PICTURE_IN_PICTURE,
    FLIP_SCREEN,
    HIDE_NOTCH,
    LARGE_ASPECT_RATIO,
    LIVE_CHAT_OVERLAY,
    WEBVIEW_TEXT_ZOOM,
    TWEAK_WEBVIEW_SETTINGS,
    ZOOMABLE_IMAGE_VIEWS,
    BLUR_IMAGE_VIEWS,
    ALLOW_TEXT_SELECTION,
    ALLOW_SHARE_ON_IMAGES,
    LONG_PRESS_COPY_TEXT,
    LONG_PRESS_ENABLE_DISABLED,
    REVEAL_PASSWORD_FIELDS,
    AUTO_DISMISS_DIALOGS,
    CUSTOM_SPLASH_SCREEN,
    WELCOME_MESSAGE,
    ALWAYS_ALLOW_COPY_PASTE,
    SCREEN_SAVER,
    RTL_LAYOUT,
    DISABLE_TRANSITIONS,
    ALLOW_SCREEN_RECORDING,

    // Module D — Media
    MUTE_ON_START,
    CUSTOM_VOLUME_ON_START,
    MUTE_WHILE_FOREGROUND,
    MUTE_BASED_ON_TEXT,
    PREVENT_VOLUME_CHANGE,
    CUSTOM_START_SOUND,
    DISABLE_FRONT_CAMERA,
    DISABLE_BACK_CAMERA,
    DISABLE_MICROPHONE,
    FAKE_CAMERA_FEED,
    FIX_EXIF_ORIENTATION,
    RANDOMIZE_FAKE_CAMERA_IMAGES,
    DISABLE_AUDIO_FOCUS_STEALING,
    ALLOW_AUDIO_ALONGSIDE,
    DISABLE_CHROMECAST_BUTTON,
    SHOW_ON_SECONDARY_DISPLAY,
    LOCK_VOLUME_ROCKER,
    VOLUME_ROCKER_OVERLAY,
    DISABLE_HAPTIC_FEEDBACK,
    AUDIO_PLAYBACK_CAPTURE,
    PREFERRED_CAMERA_APP,

    // Module E — Navigation
    FLOATING_BACK_BUTTON,
    CONFIRM_EXIT_DIALOG,
    MINIMIZE_ON_BACK,
    SHAKE_TO_EXIT,
    SWIPE_EDGE_BACK,
    LONG_PRESS_BACK_MENU,
    LONG_PRESS_BACK_CUSTOM,
    FINGERPRINT_SENSOR_ACTION,
    VOLUME_KEY_ACTIONS,
    REPROGRAM_VOLUME_KEYS,
    KIOSK_MODE,
    POPUP_BLOCKER,
    ACTIVITY_MONITOR,
    BLOCK_SPECIFIC_ACTIVITIES,

    // Module F — Storage
    ISOLATED_STORAGE,
    INSTALL_TO_SD_CARD,
    DISABLE_MEDIA_ACCESS,
    REDIRECT_EXTERNAL_STORAGE,
    PREVENT_APP_BACKUP,
    KEEP_DATA_ON_UNINSTALL,
    BUNDLE_SD_CARD_DIRS,
    BUNDLE_ORIGINAL_APP,
    CLEAR_CACHE_ON_EXIT,
    SECURE_DELETE_ON_EXIT,

    // Module G — Launching & Behavior
    REMOVE_FROM_LAUNCHER,
    ADD_ACTIVITY_SHORTCUTS,
    DISABLE_AUTO_START,
    MAKE_PERSISTENT,
    DISABLE_BACKGROUND_SERVICES,
    CLEAR_APP_DEFAULTS,
    LAUNCH_SECRET_DIALER,
    LAUNCH_OUTGOING_CALL,
    QUICK_SETTINGS_TILE,
    DISABLE_WAKE_LOCKS,
    MODIFY_JOB_SCHEDULING,
    FAKE_BATTERY_LEVEL,
    REQUEST_IGNORE_BATTERY,
    MAKE_DEFAULT_HOME,
    MAKE_DEFAULT_CAMERA,
    MAKE_DEFAULT_ASSIST,
    START_APP_ON_LAUNCH,
    START_EXIT_SPEN,
    START_EXIT_HEADPHONE,
    START_EXIT_POWER_BUTTON,
    DISABLE_SCREEN_EVENTS,
    LAUNCH_SD_MOUNTED,
    LAUNCH_NFC_TAG,

    // Module H — Networking
    DISABLE_ALL_NETWORKING,
    TOGGLE_NETWORKING_NOTIFICATION,
    DISABLE_MOBILE_DATA,
    BLOCK_BACKGROUND_NETWORKING,
    BLOCK_NETWORKING_SCREEN_OFF,
    BLOCK_IF_NO_VPN,
    MOCK_NETWORK_TYPE,
    PROXY_PER_CLONE,
    SHOW_IP_INFO_OVERLAY,
    DISABLE_CLEARTEXT_HTTP,

    // Module I — Notifications
    FILTER_NOTIFICATIONS_KEYWORD,
    QUIET_TIME_SCHEDULE,
    SILENCE_ALL_NOTIFICATIONS,
    CHANGE_VIBRATION_PATTERN,
    CHANGE_NOTIFICATION_LED,
    SNOOZE_NOTIFICATIONS,
    NOTIFICATION_TIMEOUT,
    CHANGE_NOTIFICATION_VISIBILITY,
    CHANGE_NOTIFICATION_PRIORITY,
    REMOVE_NOTIFICATION_ACTIONS,
    REPLACE_NOTIFICATION_ICONS,
    SINGLE_NOTIFICATION_GROUP,
    CHANGE_NOTIFICATION_TEXT,
    NOTIFICATION_SECRET,
    ICON_BADGE,
    FILTER_TOASTS,
    CHANGE_TOAST_POSITION,
    CHANGE_TOAST_DURATION,
    CHANGE_TOAST_OPACITY,
    TOAST_AS_NOTIFICATION,
    INVERT_TOAST_APPEARANCE,

    // Module J — Game
    COPY_OBB_FILES,
    BUNDLE_OBB_IN_APK,
    KEY_MAPPER,
    FPS_MONITOR,

    // Module K — Android TV & Wear
    TV_LAUNCHER_SUPPORT,
    CUSTOM_TV_BANNER,
    JOYSTICK_POINTER,
    TV_PICTURE_IN_PICTURE,
    TV_VERSION_ON_PHONE,
    REMOVE_WEAR_COMPONENTS,
    MAKE_WEAR_APP,

    // Module L — Automation
    SET_BRIGHTNESS_ON_LAUNCH,
    TOGGLE_DND,
    TOGGLE_WIFI_ON_LAUNCH,
    TOGGLE_BLUETOOTH_ON_LAUNCH,
    TOGGLE_AUTO_ROTATE,
    SET_CLIPBOARD_ON_LAUNCH,
    EXECUTE_TASKER,
    AUTO_PRESS_BUTTONS,
    AUTO_FILL_FORMS,
    AUTO_SCROLLER,
    FLASHLIGHT_WHILE_OPEN,
    ACTION_SEQUENCER,
    START_SHELL_HOOK,
    EXIT_SHELL_HOOK,
    EXTERNAL_API_ACCESS,
    TASKER_INTEGRATION,

    // Module M — Developer & Debugging
    CHANGE_VERSION_NAME,
    CHANGE_VERSION_CODE,
    HIDE_DEVELOPER_MODE,
    BUILT_IN_LOGCAT_VIEWER,
    DISABLE_CLONE_LOGCAT,
    CHANGE_TARGET_SDK,
    SPOOF_BUILD_VERSION,
    CUSTOMIZE_BUILD_PROPS_DEV,
    CUSTOM_PERMISSIONS,
    LIVE_ACTIVITY_MONITOR,
    LIVE_FILE_MONITOR,
    URL_MONITOR,
    HTTP_HEADER_INSPECTOR,
    WEBVIEW_DEV_TOOLKIT,
    TWEAK_WEBVIEW_SETTINGS_DEV,
    VIEW_HIERARCHY_INSPECTOR,
    SEARCH_VIEW_HIERARCHY,
    REPLACE_WIDGET_TEXT,
    RESTYLE_WIDGETS,
    PERSIST_UI_RULES,
    APK_SIGNATURE_INSPECTOR,
    CUSTOM_SMALI_PATCH,
    HEX_PATCHER,
    DEX_CLASS_INSPECTOR,
    BUILD_PROPS_INSPECTOR,
    CLONE_CONFIG_EXPORT_IMPORT
}

/**
 * Container for every spoofed identity value. Stored inside [CloneConfig] and mutated by the
 * Identity Manager UI.
 */
@Serializable
data class IdentityConfig(
    val androidId: String? = null,
    val imei: String? = null,
    val imsi: String? = null,
    val wifiMac: String? = null,
    val bluetoothMac: String? = null,
    val ethernetMac: String? = null,
    val gsfId: String? = null,
    val googleAdvertisingId: String? = null,
    val amazonAdvertisingId: String? = null,
    val facebookAttributionId: String? = null,
    val appSetId: String? = null,
    val oaid: String? = null,
    val webViewUserAgent: String? = null,
    val systemUserAgent: String? = null,
    val userCreationTime: Long? = null,
    val bootId: String? = null,
    val drmId: String? = null,
    val mediaStoreVersion: String? = null,
    val buildFingerprint: String? = null,
    val buildModel: String? = null,
    val buildBrand: String? = null,
    val buildManufacturer: String? = null,
    val buildProduct: String? = null,
    val buildDevice: String? = null,
    val buildBoard: String? = null,
    val buildHardware: String? = null,
    val sdkRelease: String? = null,
    val sdkInt: Int? = null,
    val deviceTag: String? = null
)

/**
 * A reusable automation sequence created in the Automation Builder.
 */
@Serializable
data class AutomationSequence(
    val id: String,
    val name: String,
    val steps: List<AutomationStep>
)

@Serializable
data class AutomationStep(
    val id: String,
    val action: String,
    val target: String? = null,
    val value: String? = null,
    val delayMs: Long = 0
)
