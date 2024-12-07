import com.android.build.api.dsl.LibraryExtension
import com.revakovskyi.convention.compose.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.run {
            pluginManager.run {
                apply("runningtracker.android.library")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.getByType<LibraryExtension>().also { extension ->
                configureAndroidCompose(extension)
            }
        }
    }

}