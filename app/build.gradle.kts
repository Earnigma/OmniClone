plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.omniclone"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.omniclone"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += setOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.5"
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/INDEX.LIST",
                "/META-INF/DEPENDENCIES"
            )
        }
        jniLibs {
            pickFirsts += setOf(
                "lib/arm64-v8a/libzipalign.so",
                "lib/armeabi-v7a/libzipalign.so",
                "lib/x86_64/libzipalign.so",
                "lib/x86/libzipalign.so"
            )
        }
    }
}

dependencies {
    // APK Manipulation
    implementation(files("libs/apktool.jar"))
    implementation(files("libs/baksmali.jar"))
    implementation(files("libs/smali.jar"))
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("com.google.guava:guava:33.2.1-android")

    // APK Signing
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("com.android.tools.build:apksig:8.7.0")

    // UI — Compose + Material 3
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Architecture
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-android-compiler:2.52")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Background Work
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // Networking / Proxy
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // GPS / Maps
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    // QR Codes
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // GPX Parsing
    implementation("io.jenetics:jpx:3.1.0")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Icon Badges
    implementation("me.leolin:ShortcutBadger:1.1.22")

    // Tasker Integration
    implementation("com.joaomgcd:taskerpluginlibrary:0.4.10")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Copy embedded ApkTool and helper JARs into the APK assets/libs area at build time.
abstract class CopyJarsToAssetsTask : DefaultTask() {
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun copyJars() {
        val source = inputDir.get().asFile
        val outDir = outputDir.get().asFile.apply { mkdirs() }
        listOf("apktool.jar", "baksmali.jar", "smali.jar").forEach { name ->
            val jar = File(source, name)
            if (jar.exists()) {
                jar.copyTo(File(outDir, name), overwrite = true)
            } else {
                logger.warn("Missing embedded JAR: ${jar.absolutePath}")
            }
        }
    }
}

// Copy the zipalign binary from the Android SDK build-tools into assets/bin so it can be
// executed at runtime. Falls back gracefully if the binary is missing.
abstract class CopyZipalignTask : DefaultTask() {
    @get:InputDirectory
    abstract val buildToolsDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun copyZipalign() {
        val source = buildToolsDir.get().asFile
        val abiDirs = mapOf(
            "arm64-v8a" to "lib/arm64-v8a",
            "armeabi-v7a" to "lib/armeabi-v7a",
            "x86_64" to "lib/x86_64",
            "x86" to "lib/x86"
        )

        abiDirs.forEach { (abi, dest) ->
            val zipalignFile = File(source, "$abi/zipalign").takeIf { it.exists() }
                ?: File(source, "zipalign").takeIf { it.exists() }
            val outDir = outputDir.get().asFile.resolve(dest).apply { mkdirs() }
            if (zipalignFile != null) {
                zipalignFile.copyTo(outDir.resolve("libzipalign.so"), overwrite = true)
                outDir.resolve("libzipalign.so").setExecutable(true, false)
            } else {
                logger.warn("zipalign binary not found for ABI $abi; runtime zipalign step will fail unless provided manually.")
            }
        }
    }
}

androidComponents {
    onVariants { variant ->
        val copyJars = tasks.register<CopyJarsToAssetsTask>("copy${variant.name.replaceFirstChar { it.uppercase() }}JarsToAssets") {
            inputDir.set(file("libs"))
            outputDir.set(layout.buildDirectory.dir("generated/assets/${variant.name}/libs"))
        }
        variant.sources.assets?.addGeneratedSourceDirectory(copyJars, CopyJarsToAssetsTask::outputDir)

        val sdkDir = providers.gradleProperty("sdk.dir")
            .orElse(System.getenv("ANDROID_SDK_ROOT"))
            .orElse(System.getenv("ANDROID_HOME"))
            .getOrElse(throw GradleException("ANDROID_SDK_ROOT or sdk.dir must be set"))

        val copyZipalign = tasks.register<CopyZipalignTask>("copy${variant.name.replaceFirstChar { it.uppercase() }}ZipalignToAssets") {
            buildToolsDir.set(file("$sdkDir/build-tools/${android.buildToolsVersion}"))
            outputDir.set(layout.buildDirectory.dir("generated/assets/${variant.name}/bin"))
        }
        variant.sources.assets?.addGeneratedSourceDirectory(copyZipalign, CopyZipalignTask::outputDir)
    }
}

kapt {
    correctErrorTypes = true
}
