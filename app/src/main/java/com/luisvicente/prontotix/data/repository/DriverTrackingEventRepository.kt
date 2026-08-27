package com.luisvicente.prontotix.data.repository

import com.luisvicente.prontotix.data.model.DriverTrackingEventRequest
import com.luisvicente.prontotix.data.remote.BackendRetrofitClient

class DriverTrackingEventRepository {

    suspend fun sendEvent(
        accessToken: String,
        eventType: String
    ): Result<Unit> {

        return try {
            val response =
                BackendRetrofitClient
                    .ticketsApiService
                    .sendDriverTrackingEvent(
                        authorization =
                            "Bearer $accessToken",
                        request =
                            DriverTrackingEventRequest(
                                eventType = eventType
                            )
                    )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        "Error enviando evento: ${response.code()}"
                    )
                )
            }

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}