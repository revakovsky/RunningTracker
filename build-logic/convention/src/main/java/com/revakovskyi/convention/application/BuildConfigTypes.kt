package com.revakovskyi.convention.application

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

private const val API_KEY = "API_KEY"
private const val BASE_URL = "BASE_URL"

internal fun Project.configureBuildTypes(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    extensionType: ExtensionType,
) {
    commonExtension.run {
        buildFeatures { buildConfig = true }

        val apiKey = gradleLocalProperties(
            rootDir,
            rootProject.providers
        ).getProperty(API_KEY)

        when (extensionType) {
            ExtensionType.APPLICATION -> setUpApplicationBuildTypes(apiKey, commonExtension)
            ExtensionType.LIBRARY -> setUpLibraryBuildTypes(apiKey, commonExtension)
        }
    }
}

private fun Project.setUpApplicationBuildTypes(
    apiKey: String,
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    extensions.configure<ApplicationExtension> {
        buildTypes {
            debug { configureDebugBuildType(apiKey) }
            release { configureReleaseBuildType(commonExtension, apiKey) }
        }
    }
}

private fun Project.setUpLibraryBuildTypes(
    apiKey: String,
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    extensions.configure<LibraryExtension> {
        buildTypes {
            debug { configureDebugBuildType(apiKey) }
            release { configureReleaseBuildType(commonExtension, apiKey) }
        }
    }
}

private fun BuildType.configureDebugBuildType(apiKey: String) {
    buildConfigField("String", API_KEY, "\"$apiKey\"")
    buildConfigField("String", BASE_URL, "\"https://runique.pl-coding.com:8080\"")
}

private fun BuildType.configureReleaseBuildType(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    apiKey: String,
) {
    buildConfigField("String", API_KEY, "\"$apiKey\"")
    buildConfigField("String", BASE_URL, "\"https://runique.pl-coding.com:8080\"")

    isMinifyEnabled = true
    proguardFiles(
        commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
