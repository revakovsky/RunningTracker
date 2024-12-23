package com.revakovskyi.run.data.util

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * Creates a `OneTimeWorkRequest` for a specified `CoroutineWorker` with optional input data and configuration.
 *
 * @param T The type of `CoroutineWorker` this request is targeting.
 * @param tag A unique tag to identify this work request.
 * @param inputDataBuilder A lambda to define the input data using a `Data.Builder`.
 *                         Defaults to an empty builder.
 * @return A configured `OneTimeWorkRequest`.
 *
 * Usage:
 * ```
 * val request = createOneTimeWorkRequest<MyWorker>("MyTag") {
 *     putString("key", "value")
 * }
 * ```
 *
 * - Constraints: Requires a connected network.
 * - Backoff policy: Exponential with a 2-second delay.
 */
inline fun <reified T : CoroutineWorker> createOneTimeWorkRequest(
    tag: String,
    inputDataBuilder: Data.Builder.() -> Unit = {},
): OneTimeWorkRequest {
    return OneTimeWorkRequestBuilder<T>()

        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .setBackoffCriteria(
            backoffPolicy = BackoffPolicy.EXPONENTIAL,
            backoffDelay = 2000L,
            timeUnit = TimeUnit.MILLISECONDS
        )
        .setInputData(
            inputData = Data.Builder()
                .apply(inputDataBuilder)
                .build()
        )
        .addTag(tag)
        .build()
}


/**
 * Creates a `PeriodicWorkRequest` for a specified `CoroutineWorker` with a defined interval.
 *
 * @param T The type of `CoroutineWorker` this request is targeting.
 * @param tag A unique tag to identify this work request.
 * @param interval The periodic interval at which the work should repeat.
 * @return A configured `PeriodicWorkRequest`.
 *
 * Usage:
 * ```
 * val request = createPeriodicWorkRequest<MyWorker>("MyPeriodicTag", 1.hours)
 * ```
 *
 * - Constraints: Requires a connected network.
 * - Backoff policy: Exponential with a 2-second delay.
 * - Initial delay: 30 minutes.
 */
inline fun <reified T : CoroutineWorker> createPeriodicWorkRequest(
    tag: String,
    interval: Duration,
): PeriodicWorkRequest {
    return PeriodicWorkRequestBuilder<T>(interval.toJavaDuration())
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .setBackoffCriteria(
            backoffPolicy = BackoffPolicy.EXPONENTIAL,
            backoffDelay = 2000L,
            timeUnit = TimeUnit.MILLISECONDS
        )
        .setInitialDelay(
            duration = 30,
            timeUnit = TimeUnit.MINUTES
        )
        .addTag(tag)
        .build()
}
