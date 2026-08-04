package com.luisvicente.prontotix.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.model.Ticket
import com.luisvicente.prontotix.data.repository.TicketsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class TicketsUiState(
    val isLoading: Boolean = false,
    val tickets: List<Ticket> = emptyList(),
    val errorMessage: String? = null
)

class TicketsViewModel(
    private val sessionManager: SessionManager,
    private val repository: TicketsRepository = TicketsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsUiState())
    val uiState: StateFlow<TicketsUiState> = _uiState.asStateFlow()

    init {
        loadTickets()
    }

    fun loadTickets() {
        viewModelScope.launch {
            _uiState.value = TicketsUiState(
                isLoading = true
            )

            val token = sessionManager.accessToken.first()

            if (token.isNullOrBlank()) {
                _uiState.value = TicketsUiState(
                    errorMessage = "No se encontró una sesión activa"
                )
                return@launch
            }

            repository.getTickets(token)
                .onSuccess { tickets ->
                    _uiState.value = TicketsUiState(
                        tickets = tickets
                    )
                }
                .onFailure { error ->
                    _uiState.value = TicketsUiState(
                        errorMessage = error.message
                            ?: "No fue posible cargar los tickets"
                    )
                }
        }
    }
}