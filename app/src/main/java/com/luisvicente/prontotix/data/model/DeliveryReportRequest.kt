package com.luisvicente.prontotix.data.model

data class DeliveryReportRequest(
    val provider: String,
    val receiverName: String,
    val observations: String?,
    val items: List<DeliveryReportItemRequest>
)

data class DeliveryReportItemRequest(
    val material: String,
    val quantity: Double,
    val unitPrice: Double
)