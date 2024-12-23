package com.revakovskyi.core.data.network

import com.revakovskyi.core.data.BuildConfig
import com.revakovskyi.core.domain.auth.AuthInfo
import com.revakovskyi.core.domain.auth.SessionStorage
import com.revakovskyi.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Factory class for creating a configured [HttpClient] instance.
 *
 * This factory integrates various features such as:
 * - Content negotiation for JSON
 * - Logging for debugging
 * - Default request configuration, including API key header
 * - Authentication with Bearer tokens
 *
 * @property sessionStorage A storage implementation to manage authentication tokens.
 */
class HttpClientFactory(
    private val sessionStorage: SessionStorage,
) {

    fun build(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    json = Json { ignoreUnknownKeys = true }
                )
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) { Timber.d(message) }
                }
                level = LogLevel.ALL
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
                header(key = "x-api-key", BuildConfig.API_KEY)
            }

            /***
             * Bearer token-based authentication
             */
            install(Auth) {
                bearer {

                    /**
                     * Load initial authentication tokens from the session storage.
                     */
                    loadTokens {
                        val info = sessionStorage.get()
                        BearerTokens(
                            accessToken = info?.accessToken ?: "",
                            refreshToken = info?.refreshToken ?: ""
                        )
                    }

                    /**
                     * Refresh tokens when the access token is invalid or expired.
                     * This sends a request to the `/accessToken` endpoint to get a new access token.
                     */
                    refreshTokens {
                        val info = sessionStorage.get()
                        val response = client.post<AccessTokenRequest, AccessTokenResponse>(
                            route = "/accessToken",
                            body = AccessTokenRequest(
                                refreshToken = info?.refreshToken ?: "",
                                userId = info?.userId ?: ""
                            )
                        )
                        if (response is Result.Success) {
                            val newAuthInfo = AuthInfo(
                                accessToken = response.data.accessToken,
                                refreshToken = info?.refreshToken ?: "",
                                userId = info?.userId ?: "",
                            )
                            sessionStorage.set(newAuthInfo)

                            BearerTokens(
                                accessToken = newAuthInfo.accessToken,
                                refreshToken = newAuthInfo.refreshToken
                            )
                        } else {
                            BearerTokens(
                                accessToken = "",
                                refreshToken = ""
                            )
                        }
                    }
                }
            }
        }
    }

}