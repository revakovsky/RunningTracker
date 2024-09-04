package com.revakovskyi.run.network

import com.revakovskyi.core.data.network.constructRoute
import com.revakovskyi.core.data.network.delete
import com.revakovskyi.core.data.network.get
import com.revakovskyi.core.data.network.safeCall
import com.revakovskyi.core.domain.run.RemoteRunDataSource
import com.revakovskyi.core.domain.run.Run
import com.revakovskyi.core.domain.util.DataError
import com.revakovskyi.core.domain.util.EmptyDataResult
import com.revakovskyi.core.domain.util.Result
import com.revakovskyi.core.domain.util.map
import com.revakovskyi.run.network.dto.RunDto
import com.revakovskyi.run.network.mappers.toRun
import com.revakovskyi.run.network.mappers.toRunRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.PartData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val GET_END_POINT = "/runs"
private const val POST_DELETE_END_POINT = "/run"

private const val RUN_KEY = "RUN_DATA"
private const val MAP_KEY = "MAP_PICTURE"

class KtorRemoteRunDataSource(
    private val httpClient: HttpClient,
) : RemoteRunDataSource {

    override suspend fun getRuns(): Result<List<Run>, DataError.Network> {
        return httpClient
            .get<List<RunDto>>(route = GET_END_POINT)
            .map { dtoList: List<RunDto> ->
                dtoList.map { it.toRun() }
            }
    }

    override suspend fun postRun(run: Run, mapPicture: ByteArray): Result<Run, DataError.Network> {
        val runRequestJson = Json.encodeToString(run.toRunRequest())
        val result: Result<RunDto, DataError.Network> = safeCall<RunDto> {
            httpResponse(runRequestJson, mapPicture)
        }
        return result.map { it.toRun() }
    }

    private suspend fun httpResponse(
        runRequestJson: String,
        mapPicture: ByteArray,
    ): HttpResponse {
        return httpClient.submitFormWithBinaryData(
            url = constructRoute(POST_DELETE_END_POINT),
            formData = createPartDataList(runRequestJson, mapPicture)
        ) {
            method = HttpMethod.Post
        }
    }

    private fun createPartDataList(
        runRequestJson: String,
        mapPicture: ByteArray,
    ): List<PartData> {
        return formData {
            append(RUN_KEY, runRequestJson, Headers.build {
                append(HttpHeaders.ContentType, "text/plain")
                append(HttpHeaders.ContentDisposition, "form-data; name=\"$RUN_KEY\"")
            })
            append(MAP_KEY, mapPicture, Headers.build {
                append(HttpHeaders.ContentType, "image/jpeg")
                append(HttpHeaders.ContentDisposition, "filename=map_picture.jpg")
            })
        }
    }

    override suspend fun deleteRun(id: String): EmptyDataResult<DataError.Network> {
        return httpClient.delete(
            route = POST_DELETE_END_POINT,
            queryParameters = mapOf("id" to id)
        )
    }

}
