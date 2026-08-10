package com.luisvicente.prontotix.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class DriverLocation(
    val latitude: Double,
    val longitude: Double
)

class LocationRepository(
    context: Context
) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(
            context.applicationContext
        )

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<DriverLocation> {
        return suspendCancellableCoroutine { continuation ->

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->

                    if (location != null) {
                        continuation.resume(
                            Result.success(
                                DriverLocation(
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            )
                        )
                    } else {
                        continuation.resume(
                            Result.failure(
                                Exception(
                                    "No fue posible obtener la ubicación."
                                )
                            )
                        )
                    }
                }
                .addOnFailureListener { error ->

                    continuation.resume(
                        Result.failure(error)
                    )
                }
        }
    }
}