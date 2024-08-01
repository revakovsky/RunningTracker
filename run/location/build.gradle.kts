plugins {
    alias(libs.plugins.runningtracker.android.library)
}

android {
    namespace = "com.revakovskyi.run.location"
}

dependencies {

    implementation(projects.core.domain)
    implementation(projects.run.domain)

    // Android
    implementation(libs.androidx.core.ktx)

    // Kotlin
    implementation(libs.kotlinx.coroutines.core)

    // Location
    implementation(libs.google.android.gms.play.services.location)

    // Koin
    implementation(libs.bundles.koin)

}