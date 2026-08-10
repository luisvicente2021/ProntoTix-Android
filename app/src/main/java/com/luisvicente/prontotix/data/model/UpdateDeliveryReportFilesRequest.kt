package com.luisvicente.prontotix.data.model

data class UpdateDeliveryReportFilesRequest(
    val receiptUrl: String? = null,
    val signatureUrl: String? = null,
    val pdfUrl: String? = null
)