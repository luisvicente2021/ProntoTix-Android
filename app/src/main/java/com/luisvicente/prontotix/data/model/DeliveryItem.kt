package com.luisvicente.prontotix.data.model


data class DeliveryItem(
    val id: Long = System.nanoTime(),
    val material: String = "",
    val quantity: String = "",
    val unitPrice: String = ""
) {
    val total: Double
        get() {
            val parsedQuantity = quantity.toDoubleOrNull() ?: 0.0
            val parsedPrice = unitPrice.toDoubleOrNull() ?: 0.0
            return parsedQuantity * parsedPrice
        }
}