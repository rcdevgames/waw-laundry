import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    kotlin("kapt")
    alias(libs.plugins.kotlin.serialization)
}

// === AUTO INCREMENT VERSION CODE ===
// Read/write build count from file for automatic versionCode
val versionPropsFile = file("version.properties")
var versionCode = 1
var versionName = "1.0.0"

if (versionPropsFile.canRead()) {
    val versionProps = Properties()
    versionProps.load(FileInputStream(versionPropsFile))
    versionCode = versionProps["VERSION_CODE"]?.toString()?.toInt() ?: 1
    versionName = versionProps["VERSION_NAME"]?.toString() ?: "1.0.0"
} else {
    versionPropsFile.parentFile.mkdirs()
    versionPropsFile.createNewFile()
    val versionProps = Properties()
    versionProps["VERSION_CODE"] = "1"
    versionProps["VERSION_NAME"] = "1.0.0"
    versionProps.store(FileOutputStream(versionPropsFile), "Version Properties")
}

// Increment versionCode for release builds
tasks.register("incrementVersionCode") {
    doLast {
        val props = Properties()
        props.load(FileInputStream(versionPropsFile))
        val currentCode = props["VERSION_CODE"]?.toString()?.toInt() ?: 1
        val currentName = props["VERSION_NAME"]?.toString() ?: "1.0.0"

        // Parse version name and increment patch version
        val parts = currentName.split(".")
        val major = parts.getOrNull(0)?.toInt() ?: 1
        val minor = parts.getOrNull(1)?.toInt() ?: 0
        val patch = parts.getOrNull(2)?.toInt() ?: 0

        val newPatch = patch + 1
        val newName = "$major.$minor.$newPatch"
        val newCode = currentCode + 1

        props["VERSION_CODE"] = newCode.toString()
        props["VERSION_NAME"] = newName
        props.store(FileOutputStream(versionPropsFile), "Version Properties")

        println("Version incremented: $currentName ($currentCode) -> $newName ($newCode)")
    }
}

// Hook version increment to release build
afterEvaluate {
    tasks.named("assembleRelease") {
        dependsOn("incrementVersionCode")
    }
}

android {
    namespace = "net.rcdevgames.wawlaundry"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.rcdevgames.wawlaundry"
        minSdk = 28
        targetSdk = 36
        versionCode = versionCode
        versionName = versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // === SIGNING CONFIG ===
    // Read signing config from keystore.properties file (not committed to git)
    val keystorePropsFile = file("keystore.properties")
    val hasKeystore = keystorePropsFile.exists()

    // Get debug keystore from Android SDK
    val debugKeystorePath = System.getProperty("user.home") + "/.android/debug.keystore"

    signingConfigs {
        create("release") {
            if (hasKeystore) {
                val keystoreProps = Properties()
                keystoreProps.load(FileInputStream(keystorePropsFile))
                storeFile = file(keystoreProps["STORE_FILE"] as String)
                storePassword = keystoreProps["STORE_PASSWORD"] as String
                keyAlias = keystoreProps["KEY_ALIAS"] as String
                keyPassword = keystoreProps["KEY_PASSWORD"] as String
            } else {
                // Use debug signing as fallback (for development only)
                // WARNING: Don't use this for production!
                storeFile = file(debugKeystorePath)
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")

            // ProGuard/R8 configuration
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    // Split APKs by ABI for smaller downloads
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = false  // No universal APK to save space
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Keep extended icons - R8/ProGuard will strip unused ones in release builds
    implementation(libs.androidx.material.icons.extended)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Room & SQLCipher
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.sqlcipher)
    implementation(libs.androidx.sqlite)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // NOTE: Supabase removed - using local backup only with master password verification
    // Cloud sync features (CloudSyncSetupScreen, SyncWorker) are disabled

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)

    // DataStore (Preferences) & Security Crypto
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    // ErrorProne annotations (required for R8/ProGuard with some libraries)
    compileOnly("com.google.errorprone:error_prone_annotations:2.23.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
