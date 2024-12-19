package com.revakovskyi.core.android_test

import com.revakovskyi.core.domain.auth.AuthInfo
import com.revakovskyi.core.domain.auth.SessionStorage

class SessionStorageFake : SessionStorage {

    private var authInfo: AuthInfo? = null

    override suspend fun get(): AuthInfo? {
        return authInfo
    }

    override suspend fun set(info: AuthInfo?) {
        authInfo = info
    }

}
