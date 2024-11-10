plugins {
    alias(libs.plugins.runningtracker.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.revakovskyi.core.connectivity.data"
}

dependencies {

    implementation(projects.core.domain)
    implementation(projects.core.connectivity.domain)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)

    // Wearable
    implementation(libs.play.services.wearable)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Koin
    implementation(libs.bundles.koin)

}