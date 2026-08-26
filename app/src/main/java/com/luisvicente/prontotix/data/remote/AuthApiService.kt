package com.luisvicente.prontotix.data.remote

import com.luisvicente.prontotix.data.model.AuthResponse
import com.luisvicente.prontotix.data.model.LoginRequest
import com.luisvicente.prontotix.data.model.RefreshTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    @Headers(
        "Content-Type: application/json"
    )
    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type")
        grantType: String = "password",
        @Body
        request: LoginRequest
    ): Response<AuthResponse>

    @Headers(
        "Content-Type: application/json"
    )
    @POST("auth/v1/token")
    suspend fun refreshSession(
        @Query("grant_type")
        grantType: String = "refresh_token",
        @Body
        request: RefreshTokenRequest
    ): Response<AuthResponse>
}