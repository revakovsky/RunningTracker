package com.revakovskyi.run.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.revakovskyi.core.domain.run.RunRepository
import com.revakovskyi.run.data.util.toWorkerResult

/**
 * A `CoroutineWorker` responsible for fetching runs from the remote server.
 *
 * This worker ensures the local database is synchronized with the remote server by
 * fetching the latest runs data. It is designed to handle transient failures gracefully
 * and retry the operation when necessary.
 *
 * @param context The application context in which the worker operates.
 * @param params The worker's parameters, including input data and runtime configuration.
 * @param runRepository The repository responsible for accessing and managing run data.
 */
class FetchRunsWorker(
    context: Context,
    params: WorkerParameters,
    private val runRepository: RunRepository,
) : CoroutineWorker(context, params) {

    /**
     * Executes the task of fetching runs from the remote server and updating the local database.
     *
     * - Invokes the `fetchRuns` method of the `RunRepository` to retrieve the latest runs.
     * - Handles the result of the operation:
     *   - On success, returns `Result.success()` indicating the operation completed successfully.
     *   - On error, converts the error into a worker-friendly result using `toWorkerResult()`.
     * - Retries up to 5 times in case of transient failures.
     *
     * @return A `Result` indicating the success or failure of the operation.
     * - Returns `Result.failure()` if the maximum retry count is reached.
     */
    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) return Result.failure()

        return when (val result = runRepository.fetchRuns()) {
            is com.revakovskyi.core.domain.util.Result.Error -> result.error.toWorkerResult()
            is com.revakovskyi.core.domain.util.Result.Success -> Result.success()
        }
    }

}