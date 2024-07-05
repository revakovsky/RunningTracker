plugins {
    alias(libs.plugins.runningtracker.android.library)
    alias(libs.plugins.runningtracker.jvm.ktor)
}

android {
    namespace = "com.revakovskyi.auth.data"
}

dependencies {

    implementation(projects.auth.domain)
    implementation(projects.core.domain)
    implementation(projects.core.data)

    // Koin
    api(libs.bundles.koin)

}