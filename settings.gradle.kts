@file:Suppress("UnstableApiUsage")

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

rootProject.name = "RunningTracker"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

include(":auth:presentation")
include(":auth:domain")
include(":auth:data")

include(":core:presentation:design_system")
include(":core:presentation:ui")
include(":core:domain")
include(":core:data")
include(":core:database")

include(":run:presentation")
include(":run:domain")
include(":run:data")
include(":run:network")
include(":run:location")
