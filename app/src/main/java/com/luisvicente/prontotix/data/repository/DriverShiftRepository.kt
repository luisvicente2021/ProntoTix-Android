package com.luisvicente.prontotix.data.repository

import com.luisvicente.prontotix.data.model.ActiveDriverShiftResponse
import com.luisvicente.prontotix.data.model.DriverShift
import com.luisvicente.prontotix.data.remote.BackendRetrofitClient

class DriverShiftRepository {

    private val api =
        BackendRetrofitClient.ticketsApiService

    suspend fun startShift(
        accessToken: String
    ): Result<DriverShift> {

        return try {

            val response =
                api.startDriverShift(
                    authorization = "Bearer $accessToken"
                )

            if (response.isSuccessful) {

                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(
                        Exception(
                            "El servidor no devolvió la jornada."
                        )
                    )
                }

            } else {

                Result.failure(
                    Exception(
                        "Error al iniciar jornada: ${response.code()}"
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveShift(
        accessToken: String
    ): Result<ActiveDriverShiftResponse> {

        return try {

            val response =
                api.getActiveDriverShift(
                    authorization = "Bearer $accessToken"
                )

            if (response.isSuccessful) {

                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(
                        Exception(
                            "Respuesta de jornada vacía."
                        )
                    )
                }

            } else {

                Result.failure(
                    Exception(
                        "Error al consultar jornada: ${response.code()}"
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun endShift(
        accessToken: String
    ): Result<DriverShift> {

        return try {

            val response =
                api.endDriverShift(
                    authorization = "Bearer $accessToken"
                )

            if (response.isSuccessful) {

                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(
                        Exception(
                            "El servidor no devolvió la jornada."
                        )
                    )
                }

            } else {

                Result.failure(
                    Exception(
                        "Error al finalizar jornada: ${response.code()}"
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}