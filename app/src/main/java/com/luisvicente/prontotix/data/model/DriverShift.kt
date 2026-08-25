package com.luisvicente.prontotix.data.model

data class DriverShift(
    val id: String,
    val userId: String,
    val startedAt: String,
    val endedAt: String?,
    val status: String
)

data class ActiveDriverShiftResponse(
    val active: Boolean,
    val shift: DriverShift?
)