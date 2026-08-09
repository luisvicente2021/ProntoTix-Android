package com.luisvicente.prontotix.ui.deliveryreport

import androidx.lifecycle.ViewModel
import com.luisvicente.prontotix.data.model.DeliveryItem
import com.luisvicente.prontotix.data.model.DeliveryReport
import com.luisvicente.prontotix.data.model.EvidencePhoto
import com.luisvicente.prontotix.data.model.SignatureData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Context
import com.luisvicente.prontotix.util.DeliveryPdfGenerator
import com.luisvicente.prontotix.util.PdfShareHelper
import androidx.lifecycle.viewModelScope
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.model.DeliveryReportItemRequest
import com.luisvicente.prontotix.data.model.DeliveryReportRequest
import com.luisvicente.prontotix.data.repository.DeliveryReportRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DeliveryReportUiState(
    val items: List<DeliveryItem> = listOf(DeliveryItem()),
    val provider: String = "",
    val receiverName: String = "",
    val observations: String = "",
    val isSaving: Boolean = false,
    val savedReport: DeliveryReport? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val receiptPhoto: EvidencePhoto? = null,
    val evidencePhotos: List<EvidencePhoto> = emptyList(),
    val signature: SignatureData? = null,
    val isGeneratingPdf: Boolean = false,
    val generatedPdfPath: String? = null
) {
    val grandTotal: Double
        get() = items.sumOf { it.total }
}

class DeliveryReportViewModel(
    private val sessionManager: SessionManager,
    private val repository: DeliveryReportRepository = DeliveryReportRepository()
) : ViewModel() {

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
        viewModelScope.launch {
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
                return@launch
            }

            if (currentState.receiverName.isBlank()) {
                _uiState.value = currentState.copy(
                    errorMessage = "Escribe el nombre de quien recibe",
                    successMessage = null
                )
                return@launch
            }

            val token = sessionManager.accessToken.first()

            if (token.isNullOrBlank()) {
                _uiState.value = currentState.copy(
                    errorMessage = "No se encontró una sesión activa",
                    successMessage = null
                )
                return@launch
            }

            val request = DeliveryReportRequest(
                provider = currentState.provider.trim(),
                receiverName = currentState.receiverName.trim(),
                observations = currentState.observations
                    .trim()
                    .ifBlank { null },
                items = currentState.items.map { item ->
                    DeliveryReportItemRequest(
                        material = item.material.trim(),
                        quantity = item.quantity.toDouble(),
                        unitPrice = item.unitPrice.toDouble()
                    )
                }
            )

            _uiState.value = currentState.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            repository.createReport(
                ticketId = ticketId,
                accessToken = token,
                request = request
            ).onSuccess { response ->

                val localReport = DeliveryReport(
                    ticketId = ticketId,
                    items = currentState.items,
                    provider = response.provider,
                    receiverName = response.receiverName,
                    observations = response.observations.orEmpty(),
                    totalAmount = response.totalAmount,
                    signature = currentState.signature
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    savedReport = localReport,
                    successMessage =
                        "Reporte guardado correctamente en el servidor",
                    errorMessage = null
                )

            }.onFailure { error ->

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.message
                        ?: "No fue posible guardar el reporte",
                    successMessage = null
                )
            }
        }
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

    fun updateReceiptPhoto(photo: EvidencePhoto) {
        _uiState.value = _uiState.value.copy(
            receiptPhoto = photo,
            successMessage = null,
            errorMessage = null
        )
    }

    fun addEvidence(photo: EvidencePhoto) {
        _uiState.value = _uiState.value.copy(
            evidencePhotos = _uiState.value.evidencePhotos + photo,
            successMessage = null,
            errorMessage = null
        )
    }

    fun removeEvidence(photoId: Long) {
        _uiState.value = _uiState.value.copy(
            evidencePhotos = _uiState.value.evidencePhotos.filterNot {
                it.id == photoId
            },
            successMessage = null,
            errorMessage = null
        )
    }

    fun updateSignature(signature: SignatureData) {
        _uiState.value = _uiState.value.copy(
            signature = signature,
            successMessage = null,
            errorMessage = null
        )
    }

    fun clearSignature() {
        _uiState.value = _uiState.value.copy(
            signature = null,
            successMessage = null,
            errorMessage = null
        )
    }

    fun generatePdf(
        context: Context,
        ticketId: Long
    ) {
        val currentState = _uiState.value

        if (currentState.savedReport == null) {
            _uiState.value = currentState.copy(
                errorMessage = "Primero guarda el reporte",
                successMessage = null
            )
            return
        }

        _uiState.value = currentState.copy(
            isGeneratingPdf = true,
            errorMessage = null,
            successMessage = null
        )

        DeliveryPdfGenerator.generate(
            context = context.applicationContext,
            ticketId = ticketId,
            report = currentState
        ).onSuccess { file ->
            _uiState.value = _uiState.value.copy(
                isGeneratingPdf = false,
                generatedPdfPath = file.absolutePath,
                successMessage = "PDF generado correctamente: ${file.name}",
                errorMessage = null
            )
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(
                isGeneratingPdf = false,
                generatedPdfPath = null,
                errorMessage = "Error al generar PDF: ${error.message}",
                successMessage = null
            )
        }
    }

    fun openGeneratedPdf(context: Context) {
        val path = _uiState.value.generatedPdfPath

        if (path.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Primero genera el PDF",
                successMessage = null
            )
            return
        }

        PdfShareHelper.openPdf(
            context = context,
            filePath = path
        ).onFailure { error ->
            _uiState.value = _uiState.value.copy(
                errorMessage = error.message
                    ?: "No fue posible abrir el PDF",
                successMessage = null
            )
        }
    }

    fun shareGeneratedPdf(context: Context) {
        val path = _uiState.value.generatedPdfPath

        if (path.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Primero genera el PDF",
                successMessage = null
            )
            return
        }

        PdfShareHelper.sharePdf(
            context = context,
            filePath = path
        ).onFailure { error ->
            _uiState.value = _uiState.value.copy(
                errorMessage = error.message
                    ?: "No fue posible compartir el PDF",
                successMessage = null
            )
        }
    }
}