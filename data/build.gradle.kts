plugins {
    alias(libs.plugins.jetpack.library)
    alias(libs.plugins.jetpack.dagger.hilt)
    alias(libs.plugins.dokka)
}

android {
    namespace = "dev.kevin.data"
}

dependencies {
    // ... Core
    implementation(project(":core:android"))
    implementation(project(":core:preferences"))
    implementation(project(":core:network"))
}