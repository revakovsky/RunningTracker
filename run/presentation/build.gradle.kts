plugins {
    alias(libs.plugins.runningtracker.android.feature.ui)
    alias(libs.plugins.mapsplatform.secrets.plugin)
}

android {
    namespace = "com.revakovskyi.run.presentation"
}

dependencies {

    // Other modules
    implementation(projects.core.domain)
    implementation(projects.run.domain)
    implementation(projects.core.connectivity.domain)
    implementation(projects.core.notification)

    // Compose
    implementation(libs.androidx.activity.compose)

    // Coil
    implementation(libs.coil.compose)

    // Location
    implementation(libs.google.maps.android.compose)

    // Timber
    implementation(libs.timber)

}