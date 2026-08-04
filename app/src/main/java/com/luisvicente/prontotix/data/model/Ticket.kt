package com.luisvicente.prontotix.data.model

import com.google.gson.annotations.SerializedName

data class Ticket(
    val id: Long?,
    val clientId: String?,
    val clientName: String?,
    val title: String?,
    val description: String?,
    val priority: String?,
    val status: String?,
    val openedAt: String?,
    val closedAt: String?,
    val reportedBy: String?,
    val reporterPhone: String?,
    val department: String?,
    val jobTitle: String?
)