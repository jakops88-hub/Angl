plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

// Load version information from version.properties
val versionPropsFile = file("../version.properties")
val versionProps = java.util.Properties()

if (versionPropsFile.exists()) {
    versionProps.load(java.io.FileInputStream(versionPropsFile))
}

// Get version code from properties or default to 1
val versionCodeFromProps = versionProps.getProperty("VERSION_CODE", "1").toInt()

// Use GitHub Actions run number if available, otherwise use properties file value
val buildVersionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()?.let { runNumber ->
    // For CI builds, use a high base + run number to ensure uniqueness
    1000 + runNumber
} ?: versionCodeFromProps

// Build version name from properties
val versionMajor = versionProps.getProperty("VERSION_MAJOR", "1").toInt()
val versionMinor = versionProps.getProperty("VERSION_MINOR", "0").toInt()
val versionPatch = versionProps.getProperty("VERSION_PATCH", "0").toInt()
val buildVersionName = "$versionMajor.$versionMinor.$versionPatch"

// Log version information
println("===========================================")
println("Building Angl")
println("Version Code: $buildVersionCode")
println("Version Name: $buildVersionName")
println("===========================================")

android {
    namespace = "com.angl"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.angl"
        minSdk = 24
        targetSdk = 35
        versionCode = buildVersionCode
        versionName = buildVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("keystore.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                    ?: error("SIGNING_STORE_PASSWORD environment variable not set")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                    ?: error("SIGNING_KEY_ALIAS environment variable not set")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
                    ?: error("SIGNING_KEY_PASSWORD environment variable not set")
            } else {
                // Keystore not found - release build will be unsigned
                // See SIGNING.md for instructions on creating a production keystore
                logger.warn("⚠️  Keystore not found at ${keystoreFile.absolutePath}")
                logger.warn("⚠️  Release build will be UNSIGNED - not suitable for Play Store upload")
                logger.warn("⚠️  See SIGNING.md for setup instructions")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (file("keystore.jks").exists()) signingConfigs.getByName("release") else null
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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    
    // Note: Google Fonts dependency removed - using local TTF files in res/font/
    // Place cinzel_bold.ttf, montserrat_medium.ttf, montserrat_bold.ttf in res/font/

    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // ML Kit
    implementation("com.google.mlkit:pose-detection:18.0.0-beta4")
    implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta4")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-inline:5.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}

// Task to increment version code in version.properties
tasks.register("incrementVersionCode") {
    group = "versioning"
    description = "Increments the version code in version.properties"
    
    doLast {
        val versionPropsFile = file("../version.properties")
        val versionProps = java.util.Properties()
        
        if (versionPropsFile.exists()) {
            versionProps.load(java.io.FileInputStream(versionPropsFile))
            val currentVersionCode = versionProps.getProperty("VERSION_CODE", "1").toInt()
            val newVersionCode = currentVersionCode + 1
            versionProps.setProperty("VERSION_CODE", newVersionCode.toString())
            
            versionProps.store(java.io.FileOutputStream(versionPropsFile), 
                "Version configuration for Angl app\nUpdated by incrementVersionCode task")
            
            println("Version code incremented: $currentVersionCode -> $newVersionCode")
        } else {
            println("ERROR: version.properties file not found!")
        }
    }
}

// Task to display current version information
tasks.register("showVersion") {
    group = "versioning"
    description = "Displays current version information"
    
    doLast {
        val versionPropsFile = file("../version.properties")
        val versionProps = java.util.Properties()
        
        if (versionPropsFile.exists()) {
            versionProps.load(java.io.FileInputStream(versionPropsFile))
            val versionCode = versionProps.getProperty("VERSION_CODE", "1")
            val versionMajor = versionProps.getProperty("VERSION_MAJOR", "1")
            val versionMinor = versionProps.getProperty("VERSION_MINOR", "0")
            val versionPatch = versionProps.getProperty("VERSION_PATCH", "0")
            
            println("===========================================")
            println("Current Version Information:")
            println("  Version Code: $versionCode")
            println("  Version Name: $versionMajor.$versionMinor.$versionPatch")
            println("===========================================")
        } else {
            println("ERROR: version.properties file not found!")
        }
    }
}
