package com.luisvicente.prontotix.data.model

data class AuthResponse(
    val access_token: String?,
    val token_type: String?,
    val expires_in: Int?,
    val refresh_token: String?,
    val user: AuthUser?
)

data class AuthUser(
    val id: String?,
    val email: String?
)