package com.revakovskyi.run.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.revakovskyi.core.domain.run.RunRepository
import com.revakovskyi.run.data.util.toWorkerResult

class FetchRunsWorker(
    context: Context,
    params: WorkerParameters,
    private val runRepository: RunRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) return Result.failure()

        return when (val result = runRepository.fetchRuns()) {
            is com.revakovskyi.core.domain.util.Result.Error -> result.error.toWorkerResult()
            is com.revakovskyi.core.domain.util.Result.Success -> Result.success()
        }
    }

}