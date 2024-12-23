package com.revakovskyi.run.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.revakovskyi.core.database.dao.RunPendingSyncDao
import com.revakovskyi.core.domain.run.RemoteRunDataSource
import com.revakovskyi.run.data.util.toWorkerResult

/**
 * A `CoroutineWorker` that handles the deletion of a run on the remote server.
 *
 * This worker is part of the offline-first strategy, ensuring that runs marked for deletion
 * locally are also removed from the remote server. It attempts to delete a pending run and
 * manages failure scenarios gracefully.
 *
 * @param context The application context in which the worker operates.
 * @param params The worker's parameters, including input data and runtime configuration.
 * @param remoteRunDataSource The data source responsible for handling remote API calls related to runs.
 * @param pendingSyncDao DAO for accessing and managing locally stored pending deletion synchronization data.
 */
class DeleteRunWorker(
    context: Context,
    private val params: WorkerParameters,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val pendingSyncDao: RunPendingSyncDao,
) : CoroutineWorker(context, params) {

    /**
     * Executes the task of synchronizing a run deletion request with the remote server.
     *
     * - Retrieves the run ID from the input data.
     * - Sends a request to delete the run from the remote server.
     * - On success, deletes the corresponding pending deletion entity from the local database.
     * - Retries up to 5 times in case of transient failures.
     *
     * @return A `Result` indicating the success or failure of the operation.
     * - Returns `Result.failure()` if:
     *   - The maximum retry count is reached.
     *   - The run ID is missing or invalid.
     * - Returns `Result.success()` if the synchronization is successful.
     */
    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) return Result.failure()

        val runId = params.inputData.getString(RUN_ID) ?: return Result.failure()

        return when (val result = remoteRunDataSource.deleteRun(id = runId)) {
            is com.revakovskyi.core.domain.util.Result.Error -> result.error.toWorkerResult()
            is com.revakovskyi.core.domain.util.Result.Success -> {
                pendingSyncDao.deleteDeletedRunSyncEntity(runId)
                Result.success()
            }
        }
    }


    companion object {
        const val RUN_ID = "RUN_ID"
    }

}