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

    // Android
    implementation(libs.androidx.health.services.client)

    // Koin
    api(libs.bundles.koin)

}