import com.arrazyfathan.lerun.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("lerun.android.library")
                apply("lerun.android.compose")
                apply("lerun.android.hilt")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            dependencies {
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:common"))

                add("implementation", libs.findBundle("androidx-lifecycle").get())
                add("implementation", libs.findLibrary("androidx-navigation3-runtime").get())
                add("implementation", libs.findLibrary("androidx-hilt-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-core").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())
            }
        }
    }
}
