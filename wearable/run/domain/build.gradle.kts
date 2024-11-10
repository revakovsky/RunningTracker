plugins {
    alias(libs.plugins.runningtracker.jvm.library)
}

dependencies {

    // Other modules
    implementation(projects.core.domain)
    implementation(projects.core.connectivity.domain)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

}