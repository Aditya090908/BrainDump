plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.braindump"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.braindump"

        // ML Kit GenAI requires Android 8.0+
        minSdk = 26

        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    // =========================================================
    // ROOM 3
    // =========================================================

    val room_version = "3.0.0"

    implementation(
        "androidx.room3:room3-runtime:$room_version"
    )

    ksp(
        "androidx.room3:room3-compiler:$room_version"
    )


    // =========================================================
    // SQLITE DRIVER FOR ROOM 3
    // =========================================================

    implementation(
        "androidx.sqlite:sqlite-framework:2.7.0"
    )


    // =========================================================
    // ON-DEVICE AI — GEMINI NANO / AICORE
    // =========================================================

    implementation(
        "com.google.mlkit:genai-prompt:1.0.0-beta2"
    )


    // =========================================================
    // COMPOSE
    // =========================================================

    implementation(
        platform(libs.androidx.compose.bom)
    )

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )


    // =========================================================
    // TESTING
    // =========================================================

    testImplementation(
        libs.junit
    )

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}