package com.luisvicente.prontotix.data.remote

import android.content.Context
import android.util.Log
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.repository.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    context: Context
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"

        private val refreshLock = Any()
    }

    private val sessionManager =
        SessionManager(context.applicationContext)

    private val authRepository =
        AuthRepository()

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {

        if (responseCount(response) >= 2) {
            Log.e(
                TAG,
                "La petición continúa devolviendo 401 después del refresh"
            )
            return null
        }

        synchronized(refreshLock) {

            return runBlocking {

                /*
                 * Primero comprobamos si otra petición
                 * ya renovó el token mientras esperábamos.
                 */
                val currentAccessToken =
                    sessionManager.accessToken.first()

                val tokenUsedInRequest =
                    response.request
                        .header("Authorization")
                        ?.removePrefix("Bearer ")
                        ?.trim()

                if (
                    !currentAccessToken.isNullOrBlank() &&
                    !tokenUsedInRequest.isNullOrBlank() &&
                    currentAccessToken != tokenUsedInRequest
                ) {

                    Log.d(
                        TAG,
                        "Otra petición ya renovó la sesión"
                    )

                    return@runBlocking response.request
                        .newBuilder()
                        .header(
                            "Authorization",
                            "Bearer $currentAccessToken"
                        )
                        .build()
                }

                val refreshToken =
                    sessionManager.refreshToken.first()

                if (refreshToken.isNullOrBlank()) {
                    Log.e(
                        TAG,
                        "No existe refresh token"
                    )
                    return@runBlocking null
                }

                Log.d(
                    TAG,
                    "Access token expirado. Renovando sesión..."
                )

                val refreshResult =
                    authRepository.refreshSession(
                        refreshToken
                    )

                val authResponse =
                    refreshResult.getOrNull()

                val newAccessToken =
                    authResponse?.access_token

                if (newAccessToken.isNullOrBlank()) {
                    Log.e(
                        TAG,
                        "No fue posible renovar la sesión"
                    )
                    return@runBlocking null
                }

                val newRefreshToken =
                    authResponse.refresh_token
                        ?: refreshToken

                sessionManager.saveSession(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken
                )

                Log.d(
                    TAG,
                    "Sesión renovada correctamente"
                )

                response.request
                    .newBuilder()
                    .header(
                        "Authorization",
                        "Bearer $newAccessToken"
                    )
                    .build()
            }
        }
    }

    private fun responseCount(
        response: Response
    ): Int {

        var currentResponse: Response? =
            response

        var count = 1

        while (currentResponse?.priorResponse != null) {
            count++
            currentResponse =
                currentResponse.priorResponse
        }

        return count
    }
}