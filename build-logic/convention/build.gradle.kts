import org.gradle.kotlin.dsl.compileOnly
import org.gradle.kotlin.dsl.gradlePlugin
import org.gradle.kotlin.dsl.libs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "dev.kevin.build.logic"

val javaVersion = libs.versions.java.get().toInt()

java {
    sourceCompatibility = JavaVersion.values()[javaVersion - 1]
    targetCompatibility = JavaVersion.values()[javaVersion - 1]
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.valueOf("JVM_$javaVersion"))
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.dokka.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("library") {
            id = "dev.kevin.library"
            implementationClass = "LibraryConventionPlugin"
        }
        register("uiLibrary") {
            id = "dev.kevin.ui.library"
            implementationClass = "UILibraryConventionPlugin"
        }
        register("application") {
            id = "dev.kevin.application"
            implementationClass = "ApplicationConventionPlugin"
        }
        register("daggerHilt") {
            id = "dev.kevin.dagger.hilt"
            implementationClass = "DaggerHiltConventionPlugin"
        }
        register("firebase") {
            id = "dev.kevin.firebase"
            implementationClass = "FirebaseConventionPlugin"
        }
        register("dokka") {
            id = "dev.kevin.dokka"
            implementationClass = "DokkaConventionPlugin"
        }
    }
}