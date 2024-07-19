package com.revakovskyi.auth.domain

import com.revakovskyi.core.domain.util.DataError
import com.revakovskyi.core.domain.util.EmptyDataResult

interface AuthRepository {

    suspend fun register(email: String, password: String): EmptyDataResult<DataError.Network>
    suspend fun signIn(email: String, password: String): EmptyDataResult<DataError.Network>

}
