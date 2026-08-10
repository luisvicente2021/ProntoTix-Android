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
import com.luisvicente.prontotix.data.repository.SupabaseStorageRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.luisvicente.prontotix.BuildConfig
import android.net.Uri
import com.luisvicente.prontotix.util.SignatureBitmapUtils

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

    private val storageRepository =
        SupabaseStorageRepository()

    fun uploadReceipt(
        context: Context,
        ticketId: Long
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value

            val photo = currentState.receiptPhoto

            if (photo == null) {
                _uiState.value = currentState.copy(
                    errorMessage = "Primero selecciona una foto del recibo",
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

            val uri = Uri.parse(photo.uri)

            storageRepository.uploadReceipt(
                context = context.applicationContext,
                ticketId = ticketId,
                uri = uri,
                accessToken = token,
                publishableKey = BuildConfig.SUPABASE_KEY
            ).onSuccess { path ->

                _uiState.value = _uiState.value.copy(
                    successMessage = "Recibo subido correctamente: $path",
                    errorMessage = null
                )

            }.onFailure { error ->

                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message
                        ?: "No fue posible subir el recibo",
                    successMessage = null
                )
            }
        }
    }

    fun uploadEvidences(
        context: Context,
        ticketId: Long
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.evidencePhotos.isEmpty()) {
                _uiState.value = currentState.copy(
                    errorMessage = "No hay evidencias para subir",
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

            var uploadedCount = 0

            currentState.evidencePhotos.forEachIndexed { index, photo ->

                // 1. Subimos la imagen a Supabase Storage
                val uploadResult =
                    storageRepository.uploadEvidence(
                        context = context.applicationContext,
                        ticketId = ticketId,
                        uri = Uri.parse(photo.uri),
                        accessToken = token,
                        publishableKey = BuildConfig.SUPABASE_KEY,
                        index = index + 1
                    )

                if (uploadResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage =
                            uploadResult.exceptionOrNull()?.message
                                ?: "Error al subir evidencia",
                        successMessage = null
                    )

                    return@launch
                }

                val imagePath = uploadResult.getOrThrow()

                // 2. Registramos la ruta en Vapor/PostgreSQL
                val registerResult =
                    repository.addEvidence(
                        ticketId = ticketId,
                        accessToken = token,
                        imageUrl = imagePath
                    )

                if (registerResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage =
                            registerResult.exceptionOrNull()?.message
                                ?: "La imagen se subió, pero no se pudo registrar",
                        successMessage = null
                    )

                    return@launch
                }

                uploadedCount++
            }

            _uiState.value = _uiState.value.copy(
                successMessage =
                    "$uploadedCount evidencia(s) subida(s) y registradas correctamente",
                errorMessage = null
            )
        }
    }

    fun uploadSignature(
        context: Context,
        ticketId: Long
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value

            val signature = currentState.signature

            if (signature == null) {
                _uiState.value = currentState.copy(
                    errorMessage = "Primero captura la firma",
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

            val signatureBytes =
                SignatureBitmapUtils.toPngBytes(
                    signature = signature
                )

            val uploadResult =
                storageRepository.uploadSignature(
                    ticketId = ticketId,
                    signatureBytes = signatureBytes,
                    accessToken = token,
                    publishableKey = BuildConfig.SUPABASE_KEY
                )

            if (uploadResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage =
                        uploadResult.exceptionOrNull()?.message
                            ?: "No fue posible subir la firma",
                    successMessage = null
                )
                return@launch
            }

            val signaturePath =
                uploadResult.getOrThrow()

            val updateResult =
                repository.updateFiles(
                    ticketId = ticketId,
                    accessToken = token,
                    signatureUrl = signaturePath
                )

            if (updateResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage =
                        updateResult.exceptionOrNull()?.message
                            ?: "La firma se subió, pero no se pudo registrar",
                    successMessage = null
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                successMessage =
                    "Firma subida y registrada correctamente",
                errorMessage = null
            )
        }
    }

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

    fun saveReport(
        context: Context,
        ticketId: Long
    ) {
        viewModelScope.launch {

            val currentState = _uiState.value

            if (currentState.isSaving) {
                return@launch
            }

            // 1. Validar materiales
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

            // 2. Validar receptor
            if (currentState.receiverName.isBlank()) {
                _uiState.value = currentState.copy(
                    errorMessage = "Escribe el nombre de quien recibe",
                    successMessage = null
                )
                return@launch
            }

            // 3. Obtener sesión
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

            // =========================================================
            // PASO 1 - CREAR EL REPORTE PRIMERO
            // =========================================================

            val reportResult = repository.createReport(
                ticketId = ticketId,
                accessToken = token,
                request = request
            )

            if (reportResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = reportResult.exceptionOrNull()?.message
                        ?: "No fue posible guardar el reporte",
                    successMessage = null
                )
                return@launch
            }

            val response = reportResult.getOrThrow()

            // =========================================================
            // PASO 2 - SUBIR RECIBO
            // =========================================================

            var receiptPath: String? = null

            currentState.receiptPhoto?.let { photo ->

                val receiptResult = storageRepository.uploadReceipt(
                    context = context.applicationContext,
                    ticketId = ticketId,
                    uri = Uri.parse(photo.uri),
                    accessToken = token,
                    publishableKey = BuildConfig.SUPABASE_KEY
                )

                if (receiptResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = receiptResult.exceptionOrNull()?.message
                            ?: "El reporte se creó, pero falló la subida del recibo",
                        successMessage = null
                    )
                    return@launch
                }

                receiptPath = receiptResult.getOrThrow()
            }

            // =========================================================
            // PASO 3 - SUBIR FIRMA
            // =========================================================

            var signaturePath: String? = null

            currentState.signature?.let { signature ->

                val signatureBytes =
                    SignatureBitmapUtils.toPngBytes(signature)

                val signatureResult =
                    storageRepository.uploadSignature(
                        ticketId = ticketId,
                        signatureBytes = signatureBytes,
                        accessToken = token,
                        publishableKey = BuildConfig.SUPABASE_KEY
                    )

                if (signatureResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage =
                            signatureResult.exceptionOrNull()?.message
                                ?: "El reporte se creó, pero falló la firma",
                        successMessage = null
                    )
                    return@launch
                }

                signaturePath = signatureResult.getOrThrow()
            }

            // =========================================================
            // PASO 4 - REGISTRAR RECIBO Y FIRMA EN BACKEND
            // =========================================================

            if (receiptPath != null || signaturePath != null) {

                val filesResult = repository.updateFiles(
                    ticketId = ticketId,
                    accessToken = token,
                    receiptUrl = receiptPath,
                    signatureUrl = signaturePath
                )

                if (filesResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage =
                            filesResult.exceptionOrNull()?.message
                                ?: "Los archivos se subieron, pero no pudieron registrarse",
                        successMessage = null
                    )
                    return@launch
                }
            }

            // =========================================================
            // PASO 5 - SUBIR Y REGISTRAR EVIDENCIAS
            // =========================================================

            currentState.evidencePhotos.forEachIndexed { index, photo ->

                val uploadResult =
                    storageRepository.uploadEvidence(
                        context = context.applicationContext,
                        ticketId = ticketId,
                        uri = Uri.parse(photo.uri),
                        accessToken = token,
                        publishableKey = BuildConfig.SUPABASE_KEY,
                        index = index + 1
                    )

                if (uploadResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage =
                            uploadResult.exceptionOrNull()?.message
                                ?: "El reporte se creó, pero falló una evidencia",
                        successMessage = null
                    )
                    return@launch
                }

                val imagePath = uploadResult.getOrThrow()

                val registerResult =
                    repository.addEvidence(
                        ticketId = ticketId,
                        accessToken = token,
                        imageUrl = imagePath
                    )

                if (registerResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage =
                            registerResult.exceptionOrNull()?.message
                                ?: "La evidencia se subió, pero no pudo registrarse",
                        successMessage = null
                    )
                    return@launch
                }
            }

            // =========================================================
            // PASO 6 - GUARDAR ESTADO LOCAL
            // =========================================================

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
                successMessage = "Reporte completo guardado correctamente",
                errorMessage = null
            )
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