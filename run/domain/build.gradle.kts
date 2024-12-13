plugins {
    alias(libs.plugins.runningtracker.jvm.library)
}

dependencies {

    // Module
    implementation(projects.core.domain)
    implementation(projects.core.connectivity.domain)

    // Kotlin
    implementation(libs.kotlinx.coroutines.core)

}