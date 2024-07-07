package com.revakovskyi.auth.data

import com.revakovskyi.auth.domain.AuthRepository
import com.revakovskyi.core.data.network.post
import com.revakovskyi.core.domain.util.DataError
import com.revakovskyi.core.domain.util.EmptyDataResult
import io.ktor.client.HttpClient

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
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

}