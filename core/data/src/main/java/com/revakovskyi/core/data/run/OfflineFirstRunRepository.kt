package com.revakovskyi.core.data.run

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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow

class OfflineFirstRunRepository(
    private val localRunDataSource: LocalRunDataSource,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val applicationScope: CoroutineScope,
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
        val remoteResult = remoteRunDataSource.postRun(
            run = runWithId,
            mapPicture = mapPicture
        )

        return when (remoteResult) {
            is Result.Error -> {
                // TODO: we didn't save our run because of the error and we should do it later!
                Result.Success(Unit)
            }

            is Result.Success -> upsertRunToLocalRunDataSource(remoteResult.data)
        }
    }

    private suspend fun upsertRunToLocalRunDataSource(run: Run): EmptyDataResult<DataError.Local> =
        applicationScope.async {
            localRunDataSource.upsertRun(run).asEmptyDataResult()
        }.await()


    override suspend fun deleteRun(id: RunId) {
        localRunDataSource.deleteRun(id)

        val remoteResult = applicationScope.async {
            remoteRunDataSource.deleteRun(id)
        }.await()
    }

}
