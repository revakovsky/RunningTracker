package com.revakovskyi.core.data.network

import com.revakovskyi.core.data.BuildConfig
import com.revakovskyi.core.domain.util.DataError
import com.revakovskyi.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

/**
 * Constructs a full URL based on the provided route.
 *
 * - If the route already contains the base URL, it is returned as-is.
 * - If the route starts with "/", it is appended to the base URL.
 * - Otherwise, the route is appended to the base URL with a "/" separator.
 *
 * @param route The relative or full URL.
 * @return The constructed full URL as a [String].
 */
fun constructRoute(route: String): String {
    return when {
        route.contains(BuildConfig.BASE_URL) -> route
        route.startsWith("/") -> BuildConfig.BASE_URL + route
        else -> BuildConfig.BASE_URL + "/$route"
    }
}


/**
 * Converts an HTTP response to a [Result] object.
 *
 * - Maps HTTP status codes to predefined [DataError.Network] error types.
 * - Extracts the response body for success cases.
 *
 * @param response The [HttpResponse] to process.
 * @return A [Result] containing either the parsed response body or a network error.
 */
suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, DataError.Network> {
    return when (response.status.value) {
        in 200..299 -> Result.Success(response.body<T>())
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Network.CONFLICT)
        413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}


/**
 * Executes a network call safely, converting exceptions into [Result] errors.
 *
 * - Handles common exceptions like network unavailability and serialization issues.
 * - Re-throws [CancellationException] to preserve coroutine behavior.
 *
 * @param execute A lambda that executes the network call and returns an [HttpResponse].
 * @return A [Result] containing either the parsed response body or a network error.
 */
suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        e.printStackTrace()
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SerializationException) {
        e.printStackTrace()
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        e.printStackTrace()
        if (e is CancellationException) throw e
        return Result.Error(DataError.Network.UNKNOWN)
    }

    return responseToResult(response)
}


/**
 * Executes an HTTP GET request and safely handles the result.
 *
 * - Constructs the URL using [constructRoute].
 * - Adds query parameters to the request.
 *
 * @param route The relative or full URL of the endpoint.
 * @param queryParameters A map of query parameters to include in the request.
 * @return A [Result] containing either the parsed response body or a network error.
 */
suspend inline fun <reified Response : Any> HttpClient.get(
    route: String,
    queryParameters: Map<String, Any?> = mapOf(),
): Result<Response, DataError.Network> {

    return safeCall {
        get {
            url(constructRoute(route))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}


/**
 * Executes an HTTP POST request and safely handles the result.
 *
 * - Constructs the URL using [constructRoute].
 * - Sets the request body.
 *
 * @param route The relative or full URL of the endpoint.
 * @param body The request body to send.
 * @return A [Result] containing either the parsed response body or a network error.
 */
suspend inline fun <reified Request, reified Response : Any> HttpClient.post(
    route: String,
    body: Request,
): Result<Response, DataError.Network> {

    return safeCall {
        post {
            url(constructRoute(route))
            setBody(body)
        }
    }
}


/**
 * Executes an HTTP DELETE request and safely handles the result.
 *
 * - Constructs the URL using [constructRoute].
 * - Adds query parameters to the request.
 *
 * @param route The relative or full URL of the endpoint.
 * @param queryParameters A map of query parameters to include in the request.
 * @return A [Result] containing either the parsed response body or a network error.
 */
suspend inline fun <reified Response : Any> HttpClient.delete(
    route: String,
    queryParameters: Map<String, Any?> = mapOf(),
): Result<Response, DataError.Network> {

    return safeCall {
        delete {
            url(constructRoute(route))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}
