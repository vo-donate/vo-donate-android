import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Convention plugin for configuring web3 dependencies and settings
 */
class Web3ConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", libs.findLibrary("web3j-core").get())
                add("implementation", libs.findLibrary("web3j-android").get())
                add("implementation", libs.findLibrary("web3j-klaytn").get())
                add("implementation", libs.findLibrary("web3j-klaytn-rpc").get())
            }
        }
    }

}