import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.dokka.gradle.DokkaExtension

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
            }

            dependencies {
                "dokkaPlugin"(libs.findLibrary("dokka.android.plugin").get())
                "dokkaPlugin"(libs.findLibrary("dokka.mermaid.plugin").get())
            }
        }
    }

}