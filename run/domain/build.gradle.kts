plugins {
    alias(libs.plugins.runningtracker.jvm.library)
}

dependencies {

    // Module
    implementation(projects.core.domain)

    // Kotlin
    implementation(libs.kotlinx.coroutines.core)

}