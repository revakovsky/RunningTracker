import com.android.build.api.dsl.ApplicationExtension
import com.revakovskyi.convention.application.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationWearComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.run {
            pluginManager.run {
                apply("runningtracker.android.application.compose")
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    minSdk = libs.findVersion("wearMinSdk").get().toString().toInt()
                }
            }

            dependencies {
                "implementation"(libs.findLibrary("androidx.wear.compose.material").get())
                "implementation"(libs.findLibrary("androidx.wear.compose.foundation").get())
                "implementation"(libs.findLibrary("androidx.wear.compose.ui.tooling").get())
                "implementation"(libs.findLibrary("play.services.wearable").get())
            }
        }
    }

}