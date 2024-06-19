plugins {
    alias(libs.plugins.runningtracker.android.library)
    alias(libs.plugins.runningtracker.jvm.ktor)
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