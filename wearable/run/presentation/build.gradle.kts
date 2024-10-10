plugins {
    alias(libs.plugins.runningtracker.android.library.compose)
}

android {
    namespace = "com.revakovskyi.wear.run.presentation"

    defaultConfig {
        minSdk = libs.versions.wearMinSdk.get().toInt()
    }
}

dependencies {

    // Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Koin
    api(libs.bundles.koin.compose)

    // Wearable
    implementation(libs.androidx.wear.compose.ui.tooling)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.play.services.wearable)

}