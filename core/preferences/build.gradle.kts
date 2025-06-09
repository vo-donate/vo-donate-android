plugins {
    alias(libs.plugins.jetpack.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.dokka)
}

android {
    namespace = "dev.kevin.core.preferences"
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    // ... Core Android
    implementation(project(":core:android"))

    // ... DataStore
    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)
}