package com.revakovskyi.core.data.auth

import android.content.SharedPreferences
import com.revakovskyi.core.domain.auth.AuthInfo
import com.revakovskyi.core.domain.auth.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A class responsible for securely storing and retrieving session data (e.g., authentication info)
 * in encrypted shared preferences.
 *
 * @param sharedPrefs The [SharedPreferences] instance used for encrypted storage.
 *
 * ### Key Features:
 * - Stores session data in an encrypted format using [Json] serialization.
 * - Supports adding, retrieving, and removing session information securely.
 * - Operates on a background thread using Kotlin Coroutines and [Dispatchers.IO].
 */
class EncryptedSessionStorage(
    private val sharedPrefs: SharedPreferences,
) : SessionStorage {

    /**
     * Retrieves the stored [AuthInfo] from encrypted shared preferences.
     *
     * @return [AuthInfo] if present, or `null` if no session data exists.
     */
    override suspend fun get(): AuthInfo? {
        return withContext(Dispatchers.IO) {
            val jsonString = sharedPrefs.getString(KEY_AUTH_INFO, null)
            jsonString?.let {
                Json.decodeFromString<AuthInfoSerializable>(it).toAuthInfo()
            }
        }
    }

    /**
     * Stores the provided [AuthInfo] securely in encrypted shared preferences.
     *
     * @param info The [AuthInfo] to store, or `null` to remove existing session data.
     */
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