@file:Suppress("UnstableApiUsage")

include(":core:test")


pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

gradle.startParameter.excludedTaskNames.addAll(listOf(":build-logic:convention:testClasses"))

rootProject.name = "RunningTracker"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

include(":auth:presentation")
include(":auth:domain")
include(":auth:data")

include(":core:presentation:design_system")
include(":core:presentation:design_system_wear")
include(":core:presentation:ui")
include(":core:domain")
include(":core:data")
include(":core:database")
include(":core:notification")

include(":run:presentation")
include(":run:domain")
include(":run:data")
include(":run:network")
include(":run:location")

include(":analytics:analytics_feature")
include(":analytics:presentation")
include(":analytics:domain")
include(":analytics:data")

include(":wearable:app")
include(":wearable:run:presentation")
include(":wearable:run:domain")
include(":wearable:run:data")

include(":core:connectivity:data")
include(":core:connectivity:domain")
