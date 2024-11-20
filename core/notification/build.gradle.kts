plugins {
    alias(libs.plugins.runningtracker.android.library)
}

android {
    namespace = "com.revakovskyi.core.notification"
}

dependencies {

    // Other modules
    implementation(projects.core.domain)
    implementation(projects.core.presentation.ui)
    implementation(projects.core.presentation.designSystem)

    // Android Core
    implementation(libs.androidx.core.ktx)

    // Koin
    implementation(libs.bundles.koin)

}