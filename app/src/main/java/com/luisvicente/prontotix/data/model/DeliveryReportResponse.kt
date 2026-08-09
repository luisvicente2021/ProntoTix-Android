package com.luisvicente.prontotix.data.model

data class DeliveryReportResponse(
    val id: String?,
    val ticketId: Long,
    val provider: String,
    val receiverName: String,
    val observations: String?,
    val totalAmount: Double,
    val receiptUrl: String?,
    val signatureUrl: String?,
    val pdfUrl: String?,
    val createdAt: String,
    val items: List<DeliveryReportItemResponse>
)

data class DeliveryReportItemResponse(
    val id: String?,
    val material: String,
    val quantity: Double,
    val unitPrice: Double,
    val total: Double
)