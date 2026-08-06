package com.luisvicente.prontotix.data.model

data class EvidencePhoto(
    val id: Long = System.nanoTime(),
    val uri: String
)