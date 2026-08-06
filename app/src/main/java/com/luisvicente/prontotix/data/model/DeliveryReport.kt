package com.luisvicente.prontotix.data.model

data class DeliveryReport(
    val ticketId: Long,
    val items: List<DeliveryItem>,
    val provider: String,
    val receiverName: String,
    val observations: String,
    val totalAmount: Double,
    val createdAt: Long = System.currentTimeMillis()
)