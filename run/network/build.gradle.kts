plugins {
    alias(libs.plugins.runningtracker.android.library)
    alias(libs.plugins.runningtracker.jvm.ktor)
}

android {
    namespace = "com.revakovskyi.run.network"
}

dependencies {

    implementation(projects.core.domain)
    implementation(projects.core.data)

    // Koin
    implementation(libs.bundles.koin)

}