plugins {
    alias(libs.plugins.runningtracker.jvm.library)
    alias(libs.plugins.runningtracker.jvm.junit5)
}

dependencies {

    // Other modules
    implementation(projects.core.domain)
    implementation(projects.core.connectivity.domain)
    testImplementation(projects.core.test)

    // Kotlin
    implementation(libs.kotlinx.coroutines.core)

}