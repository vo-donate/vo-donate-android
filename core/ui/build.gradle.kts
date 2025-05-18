plugins {
    alias(libs.plugins.jetpack.ui.library)
    alias(libs.plugins.dokka)
}

android {
    namespace = "dev.kevin.core.ui"
}

dependencies {
    api(project(":core:android"))

    // ... AppCompat
    api(libs.androidx.appcompat)

    // ... Fragment
    api(libs.androidx.fragment.ktx)

    // ... Activity
    api(libs.androidx.activity.ktx)
    api(libs.androidx.activity.compose)

    // ... Lifecycle
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.livedata.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.runtimeCompose)
    api(libs.androidx.lifecycle.viewModelCompose)

    // ... Jetpack Compose
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.ui.util)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.navigationSuite)
    api(libs.androidx.compose.material3.adaptive)
    api(libs.androidx.compose.material3.adaptive.layout)
    api(libs.androidx.compose.material3.adaptive.navigation)
    api(libs.androidx.compose.material3.windowSizeClass)
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.ui.tooling.preview)
    debugApi(libs.androidx.compose.ui.tooling)

    // ... Navigation
    api(libs.androidx.navigation.fragment)
    api(libs.androidx.navigation.compose)
    api(libs.androidx.hilt.navigation.compose)

    // ... Coil
    api(libs.coil.kt)
    api(libs.coil.kt.compose)

    // ... Lottie
    api(libs.lottie.compose)
}