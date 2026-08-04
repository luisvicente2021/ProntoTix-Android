package com.luisvicente.prontotix.ui.ticketdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.luisvicente.prontotix.data.local.SessionManager

class TicketDetailViewModelFactory(
    private val ticketId: Long,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                TicketDetailViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return TicketDetailViewModel(
                ticketId = ticketId,
                sessionManager = sessionManager
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}