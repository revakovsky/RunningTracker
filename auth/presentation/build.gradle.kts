plugins {
    alias(libs.plugins.runningtracker.android.feature.ui)
}

android {
    namespace = "com.revakovskyi.auth.presentation"
}

dependencies {

    implementation(projects.auth.domain)
    implementation(projects.core.domain)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

}