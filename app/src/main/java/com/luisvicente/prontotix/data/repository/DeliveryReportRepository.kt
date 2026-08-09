package com.luisvicente.prontotix.data.repository

import com.luisvicente.prontotix.data.model.CreateDeliveryReportEvidenceRequest
import com.luisvicente.prontotix.data.model.DeliveryReportRequest
import com.luisvicente.prontotix.data.model.DeliveryReportResponse
import com.luisvicente.prontotix.data.remote.BackendRetrofitClient

class DeliveryReportRepository {

    suspend fun createReport(
        ticketId: Long,
        accessToken: String,
        request: DeliveryReportRequest
    ): Result<DeliveryReportResponse> {
        return try {
            val response =
                BackendRetrofitClient.ticketsApiService.createDeliveryReport(
                    authorization = "Bearer $accessToken",
                    ticketId = ticketId,
                    request = request
                )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        "Error al guardar reporte: ${response.code()}"
                    )
                )
            }
        } catch (exception: Exception) {
            Result.failure(
                Exception(
                    exception.message
                        ?: "No fue posible conectar con el servidor"
                )
            )
        }
    }

    suspend fun addEvidence(
        ticketId: Long,
        accessToken: String,
        imageUrl: String
    ): Result<Unit> {
        return try {
            val response =
                BackendRetrofitClient.ticketsApiService
                    .addDeliveryReportEvidence(
                        authorization = "Bearer $accessToken",
                        ticketId = ticketId,
                        request = CreateDeliveryReportEvidenceRequest(
                            imageUrl = imageUrl
                        )
                    )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        "Error al registrar evidencia: ${response.code()}"
                    )
                )
            }
        } catch (exception: Exception) {
            Result.failure(
                Exception(
                    exception.message
                        ?: "No fue posible registrar la evidencia"
                )
            )
        }
    }
}