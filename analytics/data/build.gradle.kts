plugins {
    alias(libs.plugins.runningtracker.android.library)
    alias(libs.plugins.runningtracker.android.room)
}

android {
    namespace = "com.revakovskyi.analytics.data"
}

dependencies {

    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.analytics.domain)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.koin)

}