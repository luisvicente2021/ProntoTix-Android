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
import com.luisvicente.prontotix.data.repository.DriverShiftRepository

data class TicketsUiState(
    val isLoading: Boolean = false,
    val tickets: List<Ticket> = emptyList(),
    val errorMessage: String? = null,

    val isShiftLoading: Boolean = false,
    val isShiftActive: Boolean = false,
    val shiftMessage: String? = null
)

class TicketsViewModel(
    private val sessionManager: SessionManager,
    private val repository: TicketsRepository = TicketsRepository(),
    private val shiftRepository: DriverShiftRepository =
        DriverShiftRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsUiState())
    val uiState: StateFlow<TicketsUiState> = _uiState.asStateFlow()

    init {
        loadTickets()
        loadActiveShift()
    }

    fun loadTickets() {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
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
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            tickets = tickets,
                            errorMessage = null
                        )
                }
                .onFailure { error ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage =
                                error.message
                                    ?: "No fue posible cargar las diligencias"
                        )
                }
        }
    }

    fun loadActiveShift() {

        viewModelScope.launch {

            val token =
                sessionManager.accessToken.first()

            if (token.isNullOrBlank()) {
                return@launch
            }

            shiftRepository
                .getActiveShift(token)
                .onSuccess { response ->

                    _uiState.value =
                        _uiState.value.copy(
                            isShiftActive =
                                response.active,
                            isShiftLoading = false
                        )
                }
                .onFailure {

                    _uiState.value =
                        _uiState.value.copy(
                            isShiftLoading = false
                        )
                }
        }
    }

    fun startShift() {

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isShiftLoading = true,
                    shiftMessage = null
                )

            val token =
                sessionManager.accessToken.first()

            if (token.isNullOrBlank()) {

                _uiState.value =
                    _uiState.value.copy(
                        isShiftLoading = false,
                        shiftMessage =
                            "No se encontró una sesión activa"
                    )

                return@launch
            }

            shiftRepository
                .startShift(token)
                .onSuccess {

                    _uiState.value =
                        _uiState.value.copy(
                            isShiftLoading = false,
                            isShiftActive = true,
                            shiftMessage =
                                "Jornada iniciada"
                        )
                }
                .onFailure { error ->

                    _uiState.value =
                        _uiState.value.copy(
                            isShiftLoading = false,
                            shiftMessage =
                                error.message
                                    ?: "No fue posible iniciar la jornada"
                        )
                }
        }
    }

    fun endShift() {

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isShiftLoading = true,
                    shiftMessage = null
                )

            val token =
                sessionManager.accessToken.first()

            if (token.isNullOrBlank()) {

                _uiState.value =
                    _uiState.value.copy(
                        isShiftLoading = false,
                        shiftMessage =
                            "No se encontró una sesión activa"
                    )

                return@launch
            }

            shiftRepository
                .endShift(token)
                .onSuccess {

                    _uiState.value =
                        _uiState.value.copy(
                            isShiftLoading = false,
                            isShiftActive = false,
                            shiftMessage =
                                "Jornada finalizada"
                        )
                }
                .onFailure { error ->

                    _uiState.value =
                        _uiState.value.copy(
                            isShiftLoading = false,
                            shiftMessage =
                                error.message
                                    ?: "No fue posible finalizar la jornada"
                        )
                }
        }
    }
}