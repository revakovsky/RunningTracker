package com.revakovskyi.core.data.auth

import android.content.SharedPreferences
import com.revakovskyi.core.domain.auth.AuthInfo
import com.revakovskyi.core.domain.auth.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EncryptedSessionStorage(
    private val sharedPrefs: SharedPreferences,
) : SessionStorage {

    override suspend fun get(): AuthInfo? {
        return withContext(Dispatchers.IO) {
            val jsonString = sharedPrefs.getString(KEY_AUTH_INFO, null)
            jsonString?.let {
                Json.decodeFromString<AuthInfoSerializable>(it).toAuthInfo()
            }
        }
    }

    override suspend fun set(info: AuthInfo?) {
        withContext(Dispatchers.IO) {
            if (info == null) {
                sharedPrefs.edit().remove(KEY_AUTH_INFO).apply()
                return@withContext
            }

            val jsonString = Json.encodeToString(info.toAuthInfoSerializable())
            sharedPrefs
                .edit()
                .putString(KEY_AUTH_INFO, jsonString)
                .apply()
        }
    }

    companion object {
        private const val KEY_AUTH_INFO = "KEY_AUTH_INFO"
    }

}