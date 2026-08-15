package com.luisvicente.prontotix.service

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.repository.DriverLocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    companion object {
        private const val CHANNEL_ID =
            "location_tracking"

        private const val NOTIFICATION_ID =
            1001
    }

    private val serviceScope =
        CoroutineScope(
            Dispatchers.IO + Job()
        )

    private val fusedLocationClient by lazy {
        LocationServices
            .getFusedLocationProviderClient(this)
    }

    private val driverLocationRepository =
        DriverLocationRepository()

    private val sessionManager by lazy {
        SessionManager(
            applicationContext
        )
    }

    private val locationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        )
            .setMinUpdateIntervalMillis(
                5_000L
            )
            .build()

    private val locationCallback =
        object : LocationCallback() {

            override fun onLocationResult(
                result: LocationResult
            ) {
                val location =
                    result.lastLocation
                        ?: return

                serviceScope.launch {

                    val token =
                        sessionManager
                            .accessToken
                            .first()

                    if (token.isNullOrBlank()) {
                        return@launch
                    }

                    driverLocationRepository
                        .updateLocation(
                            accessToken =
                                token,
                            latitude =
                                location.latitude,
                            longitude =
                                location.longitude
                        )
                        .onFailure { error ->

                            Log.e(
                                "LocationTracking",
                                "Error enviando ubicación",
                                error
                            )
                        }
                }
            }
        }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {

        val fineGranted =
            ActivityCompat
                .checkSelfPermission(
                    this,
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ) ==
                    PackageManager
                        .PERMISSION_GRANTED

        val coarseGranted =
            ActivityCompat
                .checkSelfPermission(
                    this,
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ) ==
                    PackageManager
                        .PERMISSION_GRANTED

        if (
            !fineGranted &&
            !coarseGranted
        ) {
            stopSelf()
            return
        }

        fusedLocationClient
            .requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
    }

    private fun stopLocationUpdates() {

        fusedLocationClient
            .removeLocationUpdates(
                locationCallback
            )
    }

    override fun onDestroy() {

        stopLocationUpdates()

        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null

    private fun createNotification() =
        NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle(
                "ProntoTix"
            )
            .setContentText(
                "Compartiendo ubicación durante la diligencia"
            )
            .setSmallIcon(
                R.drawable
                    .ic_menu_mylocation
            )
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Seguimiento de ubicación",
                    NotificationManager
                        .IMPORTANCE_LOW
                )

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }
}