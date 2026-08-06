package com.luisvicente.prontotix.data.model

data class SignatureData(
    val points: List<SignaturePoint> = emptyList()
)

data class SignaturePoint(
    val x: Float,
    val y: Float,
    val isNewLine: Boolean = false
)