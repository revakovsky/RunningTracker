package com.revakovskyi.convention.compose

import com.revakovskyi.convention.application.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

fun DependencyHandlerScope.addUiLayerDependencies(project: Project) {

    "implementation"(project(":core:presentation:ui"))
    "implementation"(project(":core:presentation:design_system"))

    "implementation"(project.libs.findBundle("koin.compose").get())

    "implementation"(project.libs.findBundle("compose").get())
    "debugImplementation"(project.libs.findBundle("compose.debug").get())
    "androidTestImplementation"(project.libs.findLibrary("androidx.compose.ui.test.junit4").get())

}