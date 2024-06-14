import com.android.build.api.dsl.ApplicationExtension
import com.revakovskyi.convention.compose.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.run {
            pluginManager.apply("runningtracker.android.application")

            val commonExtension = extensions.getByType<ApplicationExtension>()
            configureAndroidCompose(commonExtension)
        }
    }

}