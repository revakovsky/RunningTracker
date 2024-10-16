package com.revakovskyi.wear.run.domain

import com.revakovskyi.core.domain.util.EmptyDataResult
import kotlinx.coroutines.flow.Flow

interface ExerciseTracker {

    val heartRate: Flow<Int>

    suspend fun isHeartRateTrackingSupported(): Boolean
    suspend fun prepareExercise(): EmptyDataResult<ExerciseError>
    suspend fun startExercise(): EmptyDataResult<ExerciseError>
    suspend fun resumeExercise(): EmptyDataResult<ExerciseError>
    suspend fun pauseExercise(): EmptyDataResult<ExerciseError>
    suspend fun stopExercise(): EmptyDataResult<ExerciseError>

}
