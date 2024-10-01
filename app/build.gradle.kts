plugins {
    alias(libs.plugins.runningtracker.android.application.compose)
    alias(libs.plugins.runningtracker.jvm.ktor)
    alias(libs.plugins.mapsplatform.secrets.plugin)
}

android {
    namespace = "com.revakovskyi.runningtracker"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    dynamicFeatures += setOf(":analytics:analytics-feature")
}

dependencies {

    // Project modules
    implementation(projects.core.presentation.ui)
    implementation(projects.core.presentation.designSystem)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.database)

    implementation(projects.auth.presentation)
    implementation(projects.auth.domain)
    implementation(projects.auth.data)

    implementation(projects.run.presentation)
    implementation(projects.run.domain)
    implementation(projects.run.data)
    implementation(projects.run.location)
    implementation(projects.run.network)

    // Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.androidx.core.splashscreen)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    // Material
    implementation(libs.androidx.material.icons.extended)

    // Koin
    api(libs.bundles.koin)

    // Google
    api(libs.core)

    // Coil
    implementation(libs.coil.compose)

    // Location
    implementation(libs.google.android.gms.play.services.location)

    // Timber
    implementation(libs.timber)

}