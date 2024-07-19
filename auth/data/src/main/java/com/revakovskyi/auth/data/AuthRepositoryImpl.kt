package com.revakovskyi.auth.data

import com.revakovskyi.auth.domain.AuthRepository
import com.revakovskyi.core.data.network.post
import com.revakovskyi.core.domain.auth.AuthInfo
import com.revakovskyi.core.domain.auth.SessionStorage
import com.revakovskyi.core.domain.util.DataError
import com.revakovskyi.core.domain.util.EmptyDataResult
import com.revakovskyi.core.domain.util.Result
import com.revakovskyi.core.domain.util.asEmptyDataResult
import io.ktor.client.HttpClient

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
    ): EmptyDataResult<DataError.Network> {

        return httpClient.post<RegisterRequest, Unit>(
            route = "/register",
            body = RegisterRequest(email, password)
        )
    }

    override suspend fun signIn(
        email: String,
        password: String,
    ): EmptyDataResult<DataError.Network> {
        val result = httpClient.post<SignInRequest, SignInResponse>(
            route = "/login",
            body = SignInRequest(email, password)
        )

        if (result is Result.Success) {
            sessionStorage.set(
                AuthInfo(
                    accessToken = result.data.accessToken,
                    refreshToken = result.data.refreshToken,
                    userId = result.data.userId
                )
            )
        }
        return result.asEmptyDataResult()
    }

}