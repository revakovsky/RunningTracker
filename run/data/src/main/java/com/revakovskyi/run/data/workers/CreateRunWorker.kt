package com.revakovskyi.run.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.revakovskyi.core.database.dao.RunPendingSyncDao
import com.revakovskyi.core.database.mappers.toRun
import com.revakovskyi.core.domain.run.RemoteRunDataSource
import com.revakovskyi.run.data.util.toWorkerResult

/**
 * A `CoroutineWorker` that handles the creation of a run on a remote server.
 *
 * This worker is part of the offline-first strategy, ensuring that runs created
 * locally are synchronized with the remote server. It attempts to push a pending run
 * to the server and handles failure scenarios gracefully.
 *
 * @param context The application context in which the worker operates.
 * @param params The worker's parameters, including input data and runtime configuration.
 * @param remoteRunDataSource The data source responsible for handling remote API calls related to runs.
 * @param pendingSyncDao DAO for accessing and managing locally stored pending run synchronization data.
 */
class CreateRunWorker(
    context: Context,
    private val params: WorkerParameters,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val pendingSyncDao: RunPendingSyncDao,
) : CoroutineWorker(context, params) {

    /**
     * Executes the task of synchronizing a pending run with the remote server.
     *
     * - Retrieves the run ID from the input data.
     * - Fetches the corresponding pending run entity from the local database.
     * - Attempts to push the run to the remote server.
     * - On success, deletes the pending entity from the local database.
     * - Retries up to 5 times in case of transient failures.
     *
     * @return A `Result` indicating the success or failure of the operation.
     * - Returns `Result.failure()` if:
     *   - The maximum retry count is reached.
     *   - The run ID is missing or invalid.
     *   - The corresponding pending run entity is not found in the database.
     * - Returns `Result.success()` if the synchronization is successful.
     */
    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) return Result.failure()

        val pendingRunId = params.inputData.getString(RUN_ID) ?: return Result.failure()
        val pendingSyncRunEntity = pendingSyncDao.getRunPendingSyncEntity(pendingRunId) ?: return Result.failure()
        val run = pendingSyncRunEntity.run.toRun()

        val result = remoteRunDataSource.postRun(
            run = run,
            mapPicture = pendingSyncRunEntity.mapPicture
        )

        return when (result) {
            is com.revakovskyi.core.domain.util.Result.Error -> result.error.toWorkerResult()
            is com.revakovskyi.core.domain.util.Result.Success -> {
                pendingSyncDao.deleteRunPendingSyncEntity(pendingRunId)
                Result.success()
            }
        }
    }


    companion object {
        const val RUN_ID = "RUN_ID"
    }

}