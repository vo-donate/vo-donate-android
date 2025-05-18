// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath(libs.google.oss.licenses.plugin)
    }
}

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

dependencies {
    dokka(project(":app"))

    // ... Core
    dokka(project(":core:android"))
    dokka(project(":core:network"))
    dokka(project(":core:web3"))

    dokkaPlugin(libs.dokka.android.plugin)
    dokkaPlugin(libs.dokka.mermaid.plugin)
}

dokka {
    pluginsConfiguration.html {
        footerMessage.set("Copyright © 2025 Kevin, All rights reserved.")
    }
}