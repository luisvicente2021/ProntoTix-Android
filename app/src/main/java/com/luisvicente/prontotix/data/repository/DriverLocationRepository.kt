package com.luisvicente.prontotix.data.repository

import com.luisvicente.prontotix.data.model.DriverLocationRequest
import com.luisvicente.prontotix.data.remote.BackendRetrofitClient

class DriverLocationRepository {

    suspend fun updateLocation(
        accessToken: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit> {

        return try {

            val response =
                BackendRetrofitClient.ticketsApiService
                    .updateDriverLocation(
                        authorization = "Bearer $accessToken",
                        request = DriverLocationRequest(
                            latitude = latitude,
                            longitude = longitude
                        )
                    )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        "Error al enviar ubicación: ${response.code()}"
                    )
                )
            }

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}