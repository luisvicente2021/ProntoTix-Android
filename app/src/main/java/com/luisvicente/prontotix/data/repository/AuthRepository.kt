package com.luisvicente.prontotix.data.repository

import com.luisvicente.prontotix.data.model.AuthResponse
import com.luisvicente.prontotix.data.model.LoginRequest
import com.luisvicente.prontotix.data.remote.RetrofitClient

class AuthRepository {

    suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponse> {

        return try {

            val response = RetrofitClient.authApiService.login(
                request = LoginRequest(
                    email = email,
                    password = password
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        "Usuario o contraseña incorrectos"
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}