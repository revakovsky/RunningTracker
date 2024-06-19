plugins {
    alias(libs.plugins.runningtracker.android.library)
}

android {
    namespace = "com.revakovskyi.core.data"
}

dependencies {

    implementation(projects.core.domain)
    implementation(projects.core.database)

    // Timber
    implementation(libs.timber)

}