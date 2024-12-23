package com.revakovskyi.wear.run.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesException
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.WarmUpConfig
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.getCurrentExerciseInfo
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.prepareExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import com.revakovskyi.core.domain.util.EmptyDataResult
import com.revakovskyi.core.domain.util.Result
import com.revakovskyi.wear.run.domain.ExerciseError
import com.revakovskyi.wear.run.domain.ExerciseTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

/**
 * Implementation of an `ExerciseTracker` that uses Health Services APIs to track exercises,
 * focusing on heart rate monitoring during activities such as running.
 *
 * @param context The application context, required to interact with the Health Services API.
 */
class HealthServicesExerciseTracker(
    private val context: Context,
) : ExerciseTracker {

    private val client = HealthServices.getClient(context).exerciseClient

    /**
     * A flow that emits heart rate data in BPM (beats per minute) during an active exercise session.
     */
    override val heartRate: Flow<Int>
        get() = callbackFlow {
            val callback = object : ExerciseUpdateCallback {

                override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                    val heartRates = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
                    val currentHeartRate = heartRates.firstOrNull()?.value
                    currentHeartRate?.let { trySend(it.roundToInt()) }
                }

                override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) = Unit
                override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) = Unit
                override fun onRegistered() = Unit

                override fun onRegistrationFailed(throwable: Throwable) {
                    if (BuildConfig.DEBUG) throwable.printStackTrace()
                }

            }
            client.setUpdateCallback(callback)

            awaitClose {
                runBlocking { client.clearUpdateCallback(callback) }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun isHeartRateTrackingSupported(): Boolean {
        return hasBodySensorsPermission() && runCatching {
            val capabilities = client.getCapabilities()
            val supportedDataTypes = capabilities
                .typeToCapabilities[ExerciseType.RUNNING]
                ?.supportedDataTypes ?: setOf()

            DataType.HEART_RATE_BPM in supportedDataTypes
        }.getOrDefault(false)
    }

    /**
     * Prepares the exercise session by configuring warm-up settings.
     * This method ensures that the device and app are ready to begin an exercise session,
     * setting up necessary configurations like exercise type and data types to track.
     *
     * @return A result indicating success if preparation is completed without issues, or an error if
     *         prerequisites (e.g., permissions or device capabilities) are not met.
     */
    override suspend fun prepareExercise(): EmptyDataResult<ExerciseError> {
        val result = checkForErrorsBefore()
        if (result is Result.Error) return result

        client.prepareExercise(buildWarmUpConfig())

        return Result.Success(Unit)
    }

    /**
     * Starts the exercise session with the specified configurations.
     * This involves initiating real-time tracking of exercise data, such as heart rate and duration.
     *
     * @return A result indicating success or any errors encountered, such as missing permissions
     *         or a conflicting exercise session already in progress.
     */
    override suspend fun startExercise(): EmptyDataResult<ExerciseError> {
        val result = checkForErrorsBefore()
        if (result is Result.Error) return result

        client.startExercise(buildExerciseConfig())
        return Result.Success(Unit)
    }

    /**
     * Resumes a paused exercise session.
     * This method allows an ongoing exercise session that has been paused to continue tracking data.
     * It ensures that the exercise session hasn't ended or been replaced by another app.
     *
     * @return A result indicating success, or an error if the exercise session cannot be resumed
     *         (e.g., if it has already ended or been replaced by another exercise session).
     */
    override suspend fun resumeExercise(): EmptyDataResult<ExerciseError> {
        val result = checkForErrorsBefore()
        if (result is Result.Error && result.error == ExerciseError.ONGOING_OTHER_EXERCISE) return result

        return try {
            client.resumeExercise()
            Result.Success(Unit)
        } catch (e: HealthServicesException) {
            Result.Error(ExerciseError.EXERCISE_ALREADY_ENDED)
        }
    }

    /**
     * Pauses an ongoing exercise session.
     * This temporarily halts data tracking while keeping the session active.
     * Useful when the user needs a break but plans to continue the exercise session.
     *
     * @return A result indicating success, or an error if the session cannot be paused
     *         (e.g., if another app is running an exercise or the session has ended).
     */
    override suspend fun pauseExercise(): EmptyDataResult<ExerciseError> {
        val result = checkForErrorsBefore()
        if (result is Result.Error && result.error == ExerciseError.ONGOING_OTHER_EXERCISE) return result

        return try {
            client.pauseExercise()
            Result.Success(Unit)
        } catch (e: HealthServicesException) {
            Result.Error(ExerciseError.EXERCISE_ALREADY_ENDED)
        }
    }

    /**
     * Stops an ongoing exercise session.
     * This finalizes the session, stopping all tracking and saving session data if applicable.
     * Once stopped, the session cannot be resumed.
     *
     * @return A result indicating success, or an error if the session cannot be stopped
     *         (e.g., if it has already ended or another app's session is interfering).
     */
    override suspend fun stopExercise(): EmptyDataResult<ExerciseError> {
        val result = checkForErrorsBefore()
        if (result is Result.Error && result.error == ExerciseError.ONGOING_OTHER_EXERCISE) return result

        return try {
            client.endExercise()
            Result.Success(Unit)
        } catch (e: HealthServicesException) {
            Result.Error(ExerciseError.EXERCISE_ALREADY_ENDED)
        }
    }

    private fun hasBodySensorsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Retrieves the current exercise information and determines the status of the exercise.
     *
     * @return A result indicating whether an exercise is ongoing and its type.
     */
    @SuppressLint("RestrictedApi")
    private suspend fun getActiveExerciseInfo(): EmptyDataResult<ExerciseError> {
        val info = client.getCurrentExerciseInfo()
        return when (info.exerciseTrackedStatus) {
            ExerciseTrackedStatus.NO_EXERCISE_IN_PROGRESS -> Result.Success(Unit)
            ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS -> Result.Error(ExerciseError.ONGOING_OWN_EXERCISE)
            ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS -> Result.Error(ExerciseError.ONGOING_OTHER_EXERCISE)
            else -> Result.Error(ExerciseError.UNKNOWN)
        }
    }

    private suspend fun checkForErrorsBefore(): EmptyDataResult<ExerciseError> {
        if (!isHeartRateTrackingSupported()) return Result.Error(ExerciseError.TRACKING_NOT_SUPPORTED)

        val result = getActiveExerciseInfo()
        if (result is Result.Error) return result

        return Result.Success(Unit)
    }

    /**
     * Builds the warm-up configuration for preparing the exercise session.
     */
    private fun buildWarmUpConfig() = WarmUpConfig(
        exerciseType = ExerciseType.RUNNING,
        dataTypes = setOf(DataType.HEART_RATE_BPM)
    )

    /**
     * Builds the main exercise configuration for starting the session.
     */
    private fun buildExerciseConfig(): ExerciseConfig = ExerciseConfig.builder(ExerciseType.RUNNING)
        .setDataTypes(setOf(DataType.HEART_RATE_BPM))
        .setIsAutoPauseAndResumeEnabled(false)
        .build()

}