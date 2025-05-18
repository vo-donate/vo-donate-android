plugins {
    alias(libs.plugins.jetpack.library)
    alias(libs.plugins.jetpack.dagger.hilt)
    alias(libs.plugins.dokka)
}

android {
    namespace = "dev.kevin.core.android"
}

dependencies {
    // ... Core Android
    api(libs.androidx.core.ktx)

    // ... Kotlin Reflect
    api(libs.kotlin.reflect)

    // ... Coroutines
    api(libs.kotlinx.coroutines.android)

    // ... Serialization
    api(libs.kotlinx.serialization.json)

    // ... Date-Time
    api(libs.kotlinx.datetime)

    // ... Dagger-Hilt
    api(libs.dagger.hilt.android)

    // ... Logger
    api(libs.timber.logging)
}
