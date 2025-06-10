plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetpack.dagger.hilt)
    // alias(libs.plugins.jetpack.firebase)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    namespace = "dev.kevin.vo_donate"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.kevin.vo_donate"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // ... Core
    implementation(project(":core:ui"))
    implementation(project(":core:network"))

    // ... Data
    implementation(project(":data"))

    // ... Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // ... OSS Licenses
    implementation(libs.google.oss.licenses)

    // ... LeakCanary
    debugImplementation(libs.leakcanary.android)
}