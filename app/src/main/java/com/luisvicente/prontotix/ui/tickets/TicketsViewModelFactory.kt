package com.luisvicente.prontotix.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.luisvicente.prontotix.data.local.SessionManager

class TicketsViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(TicketsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TicketsViewModel(
                sessionManager = sessionManager
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}