plugins {
    alias(libs.plugins.runningtracker.android.library.compose)
}

android {
    namespace = "com.revakovskyi.core.peresentation.ui"
}

dependencies {

    implementation(projects.core.domain)
    implementation(projects.core.presentation.designSystem)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

}