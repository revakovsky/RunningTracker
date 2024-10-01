plugins {
    alias(libs.plugins.runningtracker.android.dynamic.feature)
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

}