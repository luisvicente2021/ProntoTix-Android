package com.luisvicente.prontotix.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val accessToken: String? = null,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(
                errorMessage = "Escribe tu correo y contraseña"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            repository.login(email.trim(), password)
                .onSuccess { response ->
                    val token = response.access_token

                    if (token.isNullOrBlank()) {
                        _uiState.value = LoginUiState(
                            errorMessage = "Supabase no devolvió el token"
                        )
                    } else {
                        sessionManager.saveAccessToken(token)

                        _uiState.value = LoginUiState(
                            isSuccess = true,
                            accessToken = token
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState(
                        errorMessage = error.message
                            ?: "No fue posible iniciar sesión"
                    )
                }
        }
    }
}