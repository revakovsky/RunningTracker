plugins {
    alias(libs.plugins.runningtracker.android.application.wear.compose)
}

android {
    namespace = "com.revakovskyi.wearable.app"
}

dependencies {

    // Other modules
    implementation(projects.core.presentation.designSystemWear)
    implementation(projects.wearable.run.presentation)
    implementation(projects.wearable.run.data)

    implementation(projects.core.connectivity.domain)
    implementation(projects.core.connectivity.data)
    implementation(projects.core.notification)

    // Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.splashscreen)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling)

    // Koin
    api(libs.bundles.koin.compose)

}