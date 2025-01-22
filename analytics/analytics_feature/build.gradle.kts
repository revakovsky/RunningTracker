plugins {
    alias(libs.plugins.runningtracker.android.dynamic.feature)
    alias(libs.plugins.runningtracker.jvm.ktor)
}
android {
    namespace = "com.revakovskyi.analytics.analytics_feature"
}

dependencies {

    implementation(projects.app)
    implementation(projects.core.database)

    api(projects.analytics.presentation)
    implementation(projects.analytics.domain)
    implementation(projects.analytics.data)

    // Compose
    implementation(libs.androidx.navigation.compose)

}