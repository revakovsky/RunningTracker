plugins {
    alias(libs.plugins.runningtracker.android.library.compose)
}

android {
    namespace = "com.revakovskyi.core.presentation.design_system_wear"

    defaultConfig {
        minSdk = libs.versions.wearMinSdk.get().toInt()
    }
}

dependencies {

    api(projects.core.presentation.designSystem)

    implementation(libs.androidx.wear.compose.material)

}