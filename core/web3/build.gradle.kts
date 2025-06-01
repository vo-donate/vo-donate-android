@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.jetpack.library)
    alias(libs.plugins.jetpack.dagger.hilt)
    alias(libs.plugins.jetpack.test)
    alias(libs.plugins.jetpack.web3j)
    alias(libs.plugins.dokka)
    alias(libs.plugins.secrets)
}

android {
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
    packaging {
        resources {
            excludes += "/META-INF/DISCLAIMER"
        }
    }
    namespace = "dev.kevin.core.web3j"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.dagger.hilt.android)
    
    // Core dependencies
    implementation(project(":core:android"))
}
