plugins {
    alias(libs.plugins.runningtracker.jvm.library)
    alias(libs.plugins.runningtracker.jvm.junit5)
}

dependencies {

    implementation(projects.core.domain)

}