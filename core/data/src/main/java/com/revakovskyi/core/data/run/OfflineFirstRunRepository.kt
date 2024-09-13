package com.revakovskyi.core.data.run

import com.revakovskyi.core.database.dao.RunPendingSyncDao
import com.revakovskyi.core.database.entity.DeletedRunSyncEntity
import com.revakovskyi.core.database.entity.RunPendingSyncEntity
import com.revakovskyi.core.database.mappers.toRun
import com.revakovskyi.core.domain.auth.SessionStorage
import com.revakovskyi.core.domain.run.LocalRunDataSource
import com.revakovskyi.core.domain.run.RemoteRunDataSource
import com.revakovskyi.core.domain.run.Run
import com.revakovskyi.core.domain.run.RunId
import com.revakovskyi.core.domain.run.RunRepository
import com.revakovskyi.core.domain.util.DataError
import com.revakovskyi.core.domain.util.EmptyDataResult
import com.revakovskyi.core.domain.util.Result
import com.revakovskyi.core.domain.util.asEmptyDataResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineFirstRunRepository(
    private val localRunDataSource: LocalRunDataSource,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val applicationScope: CoroutineScope,
    private val runPendingSyncDao: RunPendingSyncDao,
    private val sessionStorage: SessionStorage,
) : RunRepository {

    override fun getRuns(): Flow<List<Run>> {
        return localRunDataSource.getRuns()
    }

    override suspend fun fetchRuns(): EmptyDataResult<DataError> {
        return when (val result = remoteRunDataSource.getRuns()) {
            is Result.Error -> result.asEmptyDataResult()
            is Result.Success -> upsertRunsToLocalRunDataSource(result.data)
        }
    }

    private suspend fun upsertRunsToLocalRunDataSource(runs: List<Run>): EmptyDataResult<DataError.Local> =
        applicationScope.async {
            localRunDataSource.upsertRuns(runs).asEmptyDataResult()
        }.await()


    override suspend fun upsertRun(run: Run, mapPicture: ByteArray): EmptyDataResult<DataError> {
        val localResult = localRunDataSource.upsertRun(run)
        if (localResult !is Result.Success) return localResult.asEmptyDataResult()

        val runWithId = run.copy(id = localResult.data)

        return when (val remoteResult = postRunToRemoteDataSource(runWithId, mapPicture)) {
            is Result.Error -> {
                // TODO: we didn't save our run because of the error and we should do it later!
                Result.Success(Unit)
            }

            is Result.Success -> upsertRunToLocalRunDataSource(remoteResult.data)
        }
    }

    private suspend fun postRunToRemoteDataSource(
        run: Run,
        mapPicture: ByteArray,
    ): Result<Run, DataError.Network> {
        return remoteRunDataSource.postRun(run = run, mapPicture = mapPicture)
    }

    private suspend fun upsertRunToLocalRunDataSource(run: Run): EmptyDataResult<DataError.Local> =
        applicationScope.async {
            localRunDataSource.upsertRun(run).asEmptyDataResult()
        }.await()


    override suspend fun deleteRun(id: RunId) {
        localRunDataSource.deleteRun(id)

        if (checkIfRunWasNotPushedToRemoteDB(id)) return

        applicationScope
            .async { remoteRunDataSource.deleteRun(id) }
            .await()
    }

    /**
     * Edge case where the run is created in offline mode, and then deleted in offline mode as well.
     * In this case we don't need sync anything
     */
    private suspend fun checkIfRunWasNotPushedToRemoteDB(id: RunId): Boolean {
        return if (runPendingSyncDao.getRunPendingSyncEntity(id) != null) {
            runPendingSyncDao.deleteRunPendingSyncEntity(id)
            true
        } else false
    }

    override suspend fun syncPendingRuns() {
        withContext(Dispatchers.IO) {
            val userId = sessionStorage.get()?.userId ?: return@withContext

            val createdRuns = async { runPendingSyncDao.getAllRunPendingSyncEntities(userId) }
            val deletedRuns = async { runPendingSyncDao.getAllDeletedRunSyncEntities(userId) }

            val createdJobs: List<Job> = syncPendingRunsAndGiveJobs(createdRuns)
            val deletedJobs: List<Job> = deleteOnRemoteDeletedRunsAndGiveJobs(deletedRuns)

            createdJobs.forEach { it.join() }
            deletedJobs.forEach { it.join() }
        }
    }

    private suspend fun CoroutineScope.syncPendingRunsAndGiveJobs(
        createdRuns: Deferred<List<RunPendingSyncEntity>>,
    ): List<Job> {
        return createdRuns
            .await()
            .map { createdSyncEntity -> doTheJobOfPosting(createdSyncEntity) }
    }

    private fun CoroutineScope.doTheJobOfPosting(createdSyncEntity: RunPendingSyncEntity): Job =
        launch {
            val run = createdSyncEntity.run.toRun()
            val result = postRunToRemoteDataSource(run, createdSyncEntity.mapPicture)

            when (result) {
                is Result.Error -> Unit
                is Result.Success -> deleteCreatedRunPendingSyncEntityFromLocalDB(createdSyncEntity.runId)
            }
        }

    private suspend fun deleteCreatedRunPendingSyncEntityFromLocalDB(runId: String) {
        applicationScope.launch {
            runPendingSyncDao.deleteRunPendingSyncEntity(runId = runId)
        }.join()
    }

    private suspend fun CoroutineScope.deleteOnRemoteDeletedRunsAndGiveJobs(
        deletedRuns: Deferred<List<DeletedRunSyncEntity>>,
    ): List<Job> {
        return deletedRuns
            .await()
            .map { deletedSyncEntity -> doTheJobOfDeleting(deletedSyncEntity) }
    }

    private fun CoroutineScope.doTheJobOfDeleting(deletedSyncEntity: DeletedRunSyncEntity): Job =
        launch {
            val result = remoteRunDataSource.deleteRun(deletedSyncEntity.runId)

            when (result) {
                is Result.Error -> Unit
                is Result.Success -> deleteDeletedRunSyncEntityFromLocalDB(deletedSyncEntity.runId)
            }
        }

    private suspend fun deleteDeletedRunSyncEntityFromLocalDB(runId: String) {
        applicationScope.launch {
            runPendingSyncDao.deleteDeletedRunSyncEntity(runId = runId)
        }.join()
    }

}
