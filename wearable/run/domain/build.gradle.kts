plugins {
    alias(libs.plugins.runningtracker.jvm.library)
}

dependencies {

    implementation(projects.core.domain)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

}