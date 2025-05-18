@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.jetpack.library)
    alias(libs.plugins.jetpack.dagger.hilt)
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
}

dependencies {
    implementation(libs.web3j)
}
