package com.revakovskyi.run.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.revakovskyi.core.database.dao.RunPendingSyncDao
import com.revakovskyi.core.database.mappers.toRun
import com.revakovskyi.core.domain.run.RemoteRunDataSource
import com.revakovskyi.run.data.util.toWorkerResult

class CreateRunWorker(
    context: Context,
    private val params: WorkerParameters,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val pendingSyncDao: RunPendingSyncDao,
) : CoroutineWorker(context, params) {

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