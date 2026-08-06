package com.luisvicente.prontotix.ui.deliveryreport

import androidx.lifecycle.ViewModel
import com.luisvicente.prontotix.data.model.DeliveryItem
import com.luisvicente.prontotix.data.model.DeliveryReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DeliveryReportUiState(
    val items: List<DeliveryItem> = listOf(DeliveryItem()),
    val provider: String = "",
    val receiverName: String = "",
    val observations: String = "",
    val isSaving: Boolean = false,
    val savedReport: DeliveryReport? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val grandTotal: Double
        get() = items.sumOf { it.total }
}

class DeliveryReportViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        DeliveryReportUiState()
    )

    val uiState: StateFlow<DeliveryReportUiState> =
        _uiState.asStateFlow()

    fun addItem() {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items + DeliveryItem(),
            successMessage = null,
            errorMessage = null
        )
    }

    fun removeItem(itemId: Long) {
        val currentItems = _uiState.value.items

        if (currentItems.size == 1) {
            return
        }

        _uiState.value = _uiState.value.copy(
            items = currentItems.filterNot {
                it.id == itemId
            },
            successMessage = null,
            errorMessage = null
        )
    }

    fun updateMaterial(
        itemId: Long,
        material: String
    ) {
        updateItem(itemId) { item ->
            item.copy(material = material)
        }
    }

    fun updateQuantity(
        itemId: Long,
        quantity: String
    ) {
        updateItem(itemId) { item ->
            item.copy(quantity = quantity)
        }
    }

    fun updateUnitPrice(
        itemId: Long,
        unitPrice: String
    ) {
        updateItem(itemId) { item ->
            item.copy(unitPrice = unitPrice)
        }
    }

    fun updateProvider(provider: String) {
        _uiState.value = _uiState.value.copy(
            provider = provider,
            successMessage = null,
            errorMessage = null
        )
    }

    fun updateReceiverName(receiverName: String) {
        _uiState.value = _uiState.value.copy(
            receiverName = receiverName,
            successMessage = null,
            errorMessage = null
        )
    }

    fun updateObservations(observations: String) {
        _uiState.value = _uiState.value.copy(
            observations = observations,
            successMessage = null,
            errorMessage = null
        )
    }

    fun saveReport(ticketId: Long) {
        val currentState = _uiState.value

        val invalidItem = currentState.items.any { item ->
            item.material.isBlank() ||
                    (item.quantity.toDoubleOrNull() ?: 0.0) <= 0 ||
                    (item.unitPrice.toDoubleOrNull() ?: -1.0) < 0
        }

        if (invalidItem) {
            _uiState.value = currentState.copy(
                errorMessage = "Revisa la información de los materiales",
                successMessage = null
            )
            return
        }

        if (currentState.receiverName.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Escribe el nombre de quien recibe",
                successMessage = null
            )
            return
        }

        val report = DeliveryReport(
            ticketId = ticketId,
            items = currentState.items,
            provider = currentState.provider.trim(),
            receiverName = currentState.receiverName.trim(),
            observations = currentState.observations.trim(),
            totalAmount = currentState.grandTotal
        )

        _uiState.value = currentState.copy(
            isSaving = false,
            savedReport = report,
            successMessage = "Reporte preparado correctamente",
            errorMessage = null
        )
    }

    private fun updateItem(
        itemId: Long,
        transformation: (DeliveryItem) -> DeliveryItem
    ) {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map { item ->
                if (item.id == itemId) {
                    transformation(item)
                } else {
                    item
                }
            },
            successMessage = null,
            errorMessage = null
        )
    }
}