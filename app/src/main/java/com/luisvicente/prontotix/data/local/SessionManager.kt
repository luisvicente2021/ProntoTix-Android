package com.luisvicente.prontotix.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "prontotix_session"
)

class SessionManager(
    private val context: Context
) {

    companion object {
        private val ACCESS_TOKEN_KEY =
            stringPreferencesKey("access_token")

        private val REFRESH_TOKEN_KEY =
            stringPreferencesKey("refresh_token")
    }

    val accessToken: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }

    val refreshToken: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String?
    ) {
        context.dataStore.edit { preferences ->

            preferences[ACCESS_TOKEN_KEY] =
                accessToken

            if (!refreshToken.isNullOrBlank()) {
                preferences[REFRESH_TOKEN_KEY] =
                    refreshToken
            }
        }
    }

    /*
     * Lo conservamos porque otras partes
     * del proyecto podrían utilizarlo.
     */
    suspend fun saveAccessToken(
        token: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] =
                token
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(
                ACCESS_TOKEN_KEY
            )

            preferences.remove(
                REFRESH_TOKEN_KEY
            )
        }
    }
}