plugins {
    alias(libs.plugins.runningtracker.jvm.library)
    alias(libs.plugins.runningtracker.jvm.junit5)
}

dependencies {

    // Other modules
    implementation(projects.core.domain)
    implementation(projects.core.connectivity.domain)
    implementation(projects.run.domain)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Testing
    implementation(libs.junit5.api)
    implementation(libs.coroutines.test)

}
