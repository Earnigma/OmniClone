# OmniClone ProGuard rules

# Keep OmniClone runtime classes injected into clones
-keep class com.omniclone.runtime.** { *; }

# Keep serialized models
-keepclassmembers class com.omniclone.model.** { *; }
-keepclassmembers class com.omniclone.runtime.** { *; }

# Keep WorkManager and Hilt
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.hilt.work.HiltWorker { *; }

# Keep ApkTool / BouncyCastle / apksig signatures
-keep class brut.androlib.** { *; }
-keep class org.bouncycastle.** { *; }
-keep class com.android.apksig.** { *; }

# Keep smali runtime hooks
-keep class com.omniclone.hooks.** { *; }
