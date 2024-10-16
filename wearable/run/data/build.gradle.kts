plugins {
    alias(libs.plugins.runningtracker.android.library)
}

android {
    namespace = "com.revakovskyi.wear.run.data"

    defaultConfig {
        minSdk = libs.versions.wearMinSdk.get().toInt()
    }
}

dependencies {

    implementation(projects.wearable.run.domain)
    implementation(projects.core.domain)

    // Android Health services
    implementation(libs.androidx.health.services.client)

    // Koin
    api(libs.bundles.koin)

}