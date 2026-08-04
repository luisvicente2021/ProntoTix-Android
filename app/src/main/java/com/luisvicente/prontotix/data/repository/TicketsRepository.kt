package com.luisvicente.prontotix.data.repository

import com.luisvicente.prontotix.data.model.Ticket
import com.luisvicente.prontotix.data.remote.BackendRetrofitClient

class TicketsRepository {

    suspend fun getTickets(
        accessToken: String
    ): Result<List<Ticket>> {
        return try {
            val response =
                BackendRetrofitClient.ticketsApiService.getTickets(
                    authorization = "Bearer $accessToken"
                )

            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                val message = when (response.code()) {
                    401 -> "Tu sesión venció. Inicia sesión nuevamente."
                    403 -> "No tienes permiso para consultar los tickets."
                    404 -> "No se encontró la ruta de tickets."
                    503 -> "El servidor está iniciando. Intenta nuevamente."
                    else -> "Error del servidor: ${response.code()}"
                }

                Result.failure(Exception(message))
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


    suspend fun getTicketDetail(
        ticketId: Long,
        accessToken: String
    ): Result<Ticket> {
        return try {
            val response =
                BackendRetrofitClient.ticketsApiService.getTicketDetail(
                    authorization = "Bearer $accessToken",
                    ticketId = ticketId
                )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val message = when (response.code()) {
                    401 -> "Tu sesión venció"
                    403 -> "No tienes permiso para consultar este ticket"
                    404 -> "No se encontró el ticket"
                    else -> "Error al consultar el ticket: ${response.code()}"
                }

                Result.failure(Exception(message))
            }
        } catch (exception: Exception) {
            Result.failure(
                Exception(
                    exception.message
                        ?: "No fue posible cargar el detalle"
                )
            )
        }
    }
}