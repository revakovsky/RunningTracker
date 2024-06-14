package com.revakovskyi.convention.compose

import com.android.build.api.dsl.CommonExtension
import com.revakovskyi.convention.application.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.run {
        buildFeatures {
            compose = true
        }
        composeOptions {
            kotlinCompilerExtensionVersion = libs.findVersion("composeCompiler").get().toString()
        }

        dependencies {
            val composeBom = libs.findLibrary("androidx.compose.bom").get()

            "implementation"(platform(composeBom))
            "androidTestImplementation"(platform(composeBom))
            "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling.preview").get())
        }
    }
}
