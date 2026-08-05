package com.luisvicente.prontotix.ui.ticketdetail

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

data class TicketDetailUiState(
    val isLoading: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val ticket: Ticket? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class TicketDetailViewModel(
    private val ticketId: Long,
    private val sessionManager: SessionManager,
    private val repository: TicketsRepository = TicketsRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(TicketDetailUiState())

    val uiState: StateFlow<TicketDetailUiState> =
        _uiState.asStateFlow()

    init {
        loadTicket()
    }

    fun loadTicket() {
        viewModelScope.launch {
            _uiState.value = TicketDetailUiState(
                isLoading = true
            )

            val token = sessionManager.accessToken.first()

            if (token.isNullOrBlank()) {
                _uiState.value = TicketDetailUiState(
                    errorMessage = "No se encontró una sesión activa"
                )
                return@launch
            }

            repository.getTicketDetail(
                ticketId = ticketId,
                accessToken = token
            ).onSuccess { ticket ->
                _uiState.value = TicketDetailUiState(
                    ticket = ticket
                )
            }.onFailure { error ->
                _uiState.value = TicketDetailUiState(
                    errorMessage = error.message
                        ?: "No fue posible cargar la asignación"
                )
            }
        }
    }

    fun updateStatus(newStatus: String) {
        viewModelScope.launch {
            val token = sessionManager.accessToken.first()

            if (token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No se encontró una sesión activa"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isUpdatingStatus = true,
                errorMessage = null,
                successMessage = null
            )

            repository.updateTicketStatus(
                ticketId = ticketId,
                newStatus = newStatus,
                accessToken = token
            ).onSuccess { updatedTicket ->
                _uiState.value = TicketDetailUiState(
                    ticket = updatedTicket,
                    successMessage = "Estado actualizado correctamente"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isUpdatingStatus = false,
                    errorMessage = error.message
                        ?: "No fue posible actualizar el estado"
                )
            }
        }
    }
}