package com.luisvicente.prontotix.ui.deliveryreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.luisvicente.prontotix.data.local.SessionManager

class DeliveryReportViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                DeliveryReportViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return DeliveryReportViewModel(
                sessionManager = sessionManager
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}