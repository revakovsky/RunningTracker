package com.revakovskyi.run.data

import android.content.Context
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.await
import com.revakovskyi.core.database.dao.RunPendingSyncDao
import com.revakovskyi.core.database.entity.DeletedRunSyncEntity
import com.revakovskyi.core.database.entity.RunPendingSyncEntity
import com.revakovskyi.core.database.mappers.toEntity
import com.revakovskyi.core.domain.auth.SessionStorage
import com.revakovskyi.core.domain.run.Run
import com.revakovskyi.core.domain.run.RunId
import com.revakovskyi.core.domain.syncing.SyncRunScheduler
import com.revakovskyi.run.data.util.createOneTimeWorkRequest
import com.revakovskyi.run.data.util.createPeriodicWorkRequest
import com.revakovskyi.run.data.workers.CreateRunWorker
import com.revakovskyi.run.data.workers.DeleteRunWorker
import com.revakovskyi.run.data.workers.FetchRunsWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration

class SyncRunWorkerScheduler(
    context: Context,
    private val pendingSyncDao: RunPendingSyncDao,
    private val sessionStorage: SessionStorage,
    private val applicationScope: CoroutineScope,
) : SyncRunScheduler {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun scheduleSync(syncType: SyncRunScheduler.SyncType) {
        when (syncType) {
            is SyncRunScheduler.SyncType.FetchRuns -> scheduleFetchRunWorker(syncType.interval)
            is SyncRunScheduler.SyncType.CreateRun -> scheduleCreateRunWorker(syncType.run, syncType.mapPictureBytes)
            is SyncRunScheduler.SyncType.DeleteRun -> scheduleDeleteRunWorker(syncType.runId)
        }
    }

    override suspend fun cancelAllSyncs() {
        workManager
            .cancelAllWork()
            .await()
    }

    private suspend fun scheduleFetchRunWorker(interval: Duration) {
        if (hasSyncingBeenAlreadyScheduled()) return

        val workRequest = createPeriodicWorkRequest<FetchRunsWorker>(
            tag = SYNC_WORK,
            interval = interval
        )

        workManager
            .enqueue(workRequest)
            .await()
    }

    private suspend fun scheduleCreateRunWorker(run: Run, mapPictureBytes: ByteArray) {
        val userId = sessionStorage.get()?.userId ?: return
        val pendingRun = RunPendingSyncEntity(
            run = run.toEntity(),
            mapPicture = mapPictureBytes,
            userId = userId
        )
        pendingSyncDao.upsertRunPendingSyncEntity(pendingRun)

        val workRequest = createOneTimeWorkRequest<CreateRunWorker>(
            tag = CREATE_WORK,
            inputDataBuilder = { putString(CreateRunWorker.RUN_ID, pendingRun.runId) }
        )

        launchWorkManager(workRequest)
    }

    private suspend fun scheduleDeleteRunWorker(runId: RunId) {
        val userId = sessionStorage.get()?.userId ?: return
        val deletedEntity = DeletedRunSyncEntity(
            runId = runId,
            userId = userId
        )

        pendingSyncDao.upsertDeletedRunSyncEntity(deletedEntity)

        val workRequest = createOneTimeWorkRequest<DeleteRunWorker>(
            tag = DELETE_WORK,
            inputDataBuilder = { putString(DeleteRunWorker.RUN_ID, deletedEntity.runId) }
        )

        launchWorkManager(workRequest)
    }

    private suspend fun hasSyncingBeenAlreadyScheduled(): Boolean {
        return withContext(Dispatchers.IO) {
            workManager
                .getWorkInfosByTag(SYNC_WORK)
                .get()
                .isNotEmpty()
        }
    }

    private suspend fun launchWorkManager(workRequest: WorkRequest) {
        applicationScope.launch {
            workManager
                .enqueue(workRequest)
                .await()
        }.join()
    }


    companion object {
        private const val SYNC_WORK = "SYNC_WORK"
        private const val CREATE_WORK = "CREATE_WORK"
        private const val DELETE_WORK = "DELETE_WORK"
    }

}