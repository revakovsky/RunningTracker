package com.revakovskyi.core.data.run

import com.revakovskyi.core.data.network.get
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
import com.revakovskyi.core.domain.syncing.SyncRunScheduler
import com.revakovskyi.core.domain.util.DataError
import com.revakovskyi.core.domain.util.EmptyDataResult
import com.revakovskyi.core.domain.util.Result
import com.revakovskyi.core.domain.util.asEmptyDataResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val LOG_OUT_END_POINT = "/logout"

/**
 * Repository for managing run data in an offline-first approach.
 *
 * This repository synchronizes run data between a local data source (e.g., Room database)
 * and a remote data source (e.g., API). It handles operations such as fetching, inserting,
 * updating, and deleting runs while ensuring synchronization during offline and online states.
 *
 * Key Features:
 * - Offline-first behavior with local storage using [LocalRunDataSource].
 * - Remote synchronization using [RemoteRunDataSource].
 * - Scheduling background synchronization with [SyncRunScheduler].
 * - Handles authentication tokens for secure requests.
 * - Implements strategies to manage runs created or deleted in offline mode.
 */
class OfflineFirstRunRepository(
    private val localRunDataSource: LocalRunDataSource,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val applicationScope: CoroutineScope,
    private val runPendingSyncDao: RunPendingSyncDao,
    private val sessionStorage: SessionStorage,
    private val syncRunScheduler: SyncRunScheduler,
    private val client: HttpClient,
) : RunRepository {

    /**
     * Retrieves all runs from the local data source as a [Flow].
     * Emits updates in real time when the local database changes.
     */
    override fun getRuns(): Flow<List<Run>> {
        return localRunDataSource.getRuns()
    }

    /**
     * Fetches runs from the remote data source and updates the local database.
     * Ensures that the local database remains consistent with the remote source.
     *
     * @return [EmptyDataResult] indicating success or an error during the fetch.
     */
    override suspend fun fetchRuns(): EmptyDataResult<DataError> {
        return when (val result = remoteRunDataSource.getRuns()) {
            is Result.Error -> result.asEmptyDataResult()
            is Result.Success -> upsertRunsToLocalRunDataSource(result.data)
        }
    }

    /**
     * Inserts or updates a list of runs into the local data source.
     * Used during synchronization with the remote data source.
     */
    private suspend fun upsertRunsToLocalRunDataSource(runs: List<Run>): EmptyDataResult<DataError.Local> =
        applicationScope.async {
            localRunDataSource.upsertRuns(runs).asEmptyDataResult()
        }.await()


    /**
     * Adds or updates a single run. Handles offline creation by scheduling a sync task.
     *
     * @param run The run object to be upserted.
     * @param mapPicture A binary representation of the map for the run.
     */
    override suspend fun upsertRun(run: Run, mapPicture: ByteArray): EmptyDataResult<DataError> {
        val localResult = localRunDataSource.upsertRun(run)
        if (localResult !is Result.Success) return localResult.asEmptyDataResult()

        val runWithId = run.copy(id = localResult.data)

        return when (val remoteResult = postRunToRemoteDataSource(runWithId, mapPicture)) {
            is Result.Error -> launchSyncSchedulerToCreateRun(runWithId, mapPicture)
            is Result.Success -> upsertRunToLocalRunDataSource(remoteResult.data)
        }
    }

    private suspend fun postRunToRemoteDataSource(
        run: Run,
        mapPicture: ByteArray,
    ): Result<Run, DataError.Network> {
        return remoteRunDataSource.postRun(run = run, mapPicture = mapPicture)
    }

    /**
     * Schedules a background task to sync a newly created run when offline.
     */
    private suspend fun launchSyncSchedulerToCreateRun(
        run: Run,
        mapPicture: ByteArray,
    ): Result.Success<Unit> {
        applicationScope.launch {
            syncRunScheduler.scheduleSync(
                syncType = SyncRunScheduler.SyncType.CreateRun(run, mapPicture)
            )
        }.join()
        return Result.Success(Unit)
    }

    /**
     * Inserts or updates a single run in the local data source.
     * This method is used to ensure the local database is updated
     * with the latest run data, either newly created or modified.
     *
     * @param run The run object to be upserted into the local data source.
     * @return [EmptyDataResult] indicating success or any local database error.
     */
    private suspend fun upsertRunToLocalRunDataSource(run: Run): EmptyDataResult<DataError.Local> =
        applicationScope.async {
            localRunDataSource.upsertRun(run).asEmptyDataResult()
        }.await()

    /**
     * Deletes a run by ID. Ensures local and remote consistency.
     * If a run was created and deleted offline, it avoids unnecessary sync.
     */
    override suspend fun deleteRun(id: RunId) {
        localRunDataSource.deleteRun(id)

        if (checkIfRunWasNotPushedToRemoteDB(id)) return

        val remoteResult = applicationScope
            .async { remoteRunDataSource.deleteRun(id) }
            .await()

        if (remoteResult is Result.Error) launchSyncSchedulerToDeleteRun(id)
    }

    /**
     * Checks if a run created offline was deleted before syncing to the remote database.
     * If so, removes it from the pending sync table.
     */
    private suspend fun checkIfRunWasNotPushedToRemoteDB(id: RunId): Boolean {
        return if (runPendingSyncDao.getRunPendingSyncEntity(id) != null) {
            runPendingSyncDao.deleteRunPendingSyncEntity(id)
            true
        } else false
    }

    /**
     * Schedules a background task to sync a deleted run when offline.
     */
    private suspend fun launchSyncSchedulerToDeleteRun(id: RunId) {
        applicationScope.launch {
            syncRunScheduler.scheduleSync(
                syncType = SyncRunScheduler.SyncType.DeleteRun(id)
            )
        }.join()
    }

    /**
     * Synchronizes pending runs (created or deleted) with the remote server.
     * Handles offline-created runs and deleted runs, ensuring local consistency after sync.
     */
    override suspend fun syncPendingRuns() {
        withContext(Dispatchers.IO) {
            val userId = sessionStorage.get()?.userId ?: return@withContext

            val createdRuns = async { runPendingSyncDao.getAllRunPendingSyncEntities(userId) }
            val deletedRuns = async { runPendingSyncDao.getAllDeletedRunSyncEntities(userId) }

            val createdJobs: List<Job> = createdRuns
                .await()
                .map { createdSyncEntity -> doTheJobOfPosting(createdSyncEntity) }

            val deletedJobs: List<Job> = deletedRuns
                .await()
                .map { deletedSyncEntity -> doTheJobOfDeleting(deletedSyncEntity) }

            createdJobs.forEach { it.join() }
            deletedJobs.forEach { it.join() }
        }
    }

    /**
     * Helper function for syncing a pending created run.
     */
    private fun CoroutineScope.doTheJobOfPosting(createdSyncEntity: RunPendingSyncEntity): Job =
        launch {
            val run = createdSyncEntity.run.toRun()
            val result = postRunToRemoteDataSource(run, createdSyncEntity.mapPicture)

            when (result) {
                is Result.Error -> Unit
                is Result.Success -> deleteCreatedRunPendingSyncEntityFromLocalDB(createdSyncEntity.runId)
            }
        }

    /**
     * Helper function for syncing a pending deleted run.
     */
    private fun CoroutineScope.doTheJobOfDeleting(deletedSyncEntity: DeletedRunSyncEntity): Job =
        launch {
            val result = remoteRunDataSource.deleteRun(deletedSyncEntity.runId)

            when (result) {
                is Result.Error -> Unit
                is Result.Success -> deleteDeletedRunSyncEntityFromLocalDB(deletedSyncEntity.runId)
            }
        }

    /**
     * Deletes a locally stored pending created run entity after successful sync.
     */
    private suspend fun deleteCreatedRunPendingSyncEntityFromLocalDB(runId: String) {
        applicationScope.launch {
            runPendingSyncDao.deleteRunPendingSyncEntity(runId = runId)
        }.join()
    }

    /**
     * Deletes a locally stored pending deleted run entity after successful sync.
     */
    private suspend fun deleteDeletedRunSyncEntityFromLocalDB(runId: String) {
        applicationScope.launch {
            runPendingSyncDao.deleteDeletedRunSyncEntity(runId = runId)
        }.join()
    }

    /**
     * Deletes all runs from the local database.
     */
    override suspend fun deleteAllRuns() {
        localRunDataSource.deleteAllRuns()
    }

    /**
     * Logs the user out and clears authentication tokens.
     *
     * @return [EmptyDataResult] indicating success or failure of the logout operation.
     */
    override suspend fun logOut(): EmptyDataResult<DataError.Network> {
        val result = client.get<Unit>(route = LOG_OUT_END_POINT).asEmptyDataResult()

        client
            .plugin(Auth).providers
            .filterIsInstance<BearerAuthProvider>()
            .firstOrNull()
            ?.clearToken()

        return result
    }

}
