plugins {
    alias(libs.plugins.runningtracker.android.library)
    alias(libs.plugins.runningtracker.android.junit5)
}

android {
    namespace = "com.revakovskyi.core.android_test"
}

dependencies {

    // Other modules
    implementation(projects.auth.data)
    implementation(projects.core.domain)
    api(projects.core.test)

    // Ktor
    implementation(libs.ktor.client.mock)
    implementation(libs.bundles.ktor)

    // Coroutines
    implementation(libs.coroutines.test)

}