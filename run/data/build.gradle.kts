plugins {
    alias(libs.plugins.runningtracker.android.library)
}

android {
    namespace = "com.revakovskyi.run.data"
}

dependencies {

    implementation(projects.core.domain)
    implementation(projects.run.domain)
    implementation(projects.core.database)
    implementation(projects.core.connectivity.domain)

    // Work Manager
    implementation(libs.androidx.work)

    // Kotlin
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Location
    implementation(libs.google.android.gms.play.services.location)

    // Koin Work Manager
    implementation(libs.koin.android.workmanager)

}