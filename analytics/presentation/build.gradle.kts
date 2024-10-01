plugins {
    alias(libs.plugins.runningtracker.android.feature.ui)
}

android {
    namespace = "com.revakovskyi.analytics.presentation"
}

dependencies {

    implementation(projects.analytics.domain)

}