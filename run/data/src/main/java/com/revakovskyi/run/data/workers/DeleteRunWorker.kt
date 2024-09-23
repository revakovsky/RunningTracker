package com.revakovskyi.run.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.revakovskyi.core.database.dao.RunPendingSyncDao
import com.revakovskyi.core.domain.run.RemoteRunDataSource
import com.revakovskyi.run.data.util.toWorkerResult

class DeleteRunWorker(
    context: Context,
    private val params: WorkerParameters,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val pendingSyncDao: RunPendingSyncDao,
) : CoroutineWorker(context, params) {

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