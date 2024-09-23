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
