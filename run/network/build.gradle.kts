plugins {
    alias(libs.plugins.runningtracker.android.library)
}

android {
    namespace = "com.revakovskyi.run.network"
}

dependencies {

    implementation(projects.core.domain)
    implementation(projects.core.data)

}