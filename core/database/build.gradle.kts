plugins {
    alias(libs.plugins.runningtracker.android.library)
    alias(libs.plugins.runningtracker.android.room)
}

android {
    namespace = "com.revakovskyi.core.database"
}

dependencies {

    implementation(projects.core.domain)

    // MongoDb bson
    implementation(libs.org.mongodb.bson)

}