import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters

class DokkaConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply("org.jetbrains.dokka")
            }

            extensions.configure<DokkaExtension> {
                moduleName.set(path)
                dokkaSourceSets.named("main") {
                    includes.from("README.md")
                    suppressGeneratedFiles.set(true)
                }
                pluginsConfiguration.withType<DokkaHtmlPluginParameters> {
                    footerMessage.set("Copyright © 2025 Kevin, All rights reserved.")
                }
            }

            dependencies {
                "dokkaPlugin"(libs.findLibrary("dokka.android.plugin").get())
                "dokkaPlugin"(libs.findLibrary("dokka.mermaid.plugin").get())
            }
        }
    }

}