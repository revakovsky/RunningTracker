plugins {
    alias(libs.plugins.runningtracker.android.feature.ui)
    alias(libs.plugins.mapsplatform.secrets.plugin)
}

android {
    namespace = "com.revakovskyi.run.presentation"
}

dependencies {

    implementation(projects.core.domain)
    implementation(projects.run.domain)

    // Compose
    implementation(libs.androidx.activity.compose)

    // Coil
    implementation(libs.coil.compose)

    // Location
    implementation(libs.google.maps.android.compose)

    // Timber
    implementation(libs.timber)

}