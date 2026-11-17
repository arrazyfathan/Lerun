import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.arrazyfathan.lerun.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.findByType<ApplicationExtension>()?.apply {
                buildFeatures {
                    compose = true
                }
            }

            extensions.findByType<LibraryExtension>()?.apply {
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                add("debugImplementation", libs.findLibrary("androidx-ui-tooling").get())
            }
        }
    }
}
