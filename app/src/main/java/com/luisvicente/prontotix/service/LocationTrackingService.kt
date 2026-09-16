package com.luisvicente.prontotix.service

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
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
import com.luisvicente.prontotix.data.repository.DriverTrackingEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    companion object {
        private const val CHANNEL_ID =
            "location_tracking"

        private const val NOTIFICATION_ID =
            1001

        private const val TAG =
            "LocationTracking"

        /*
         * Cada cuánto comprobamos si
         * Android tiene la ubicación
         * activada o desactivada.
         */
        private const val GPS_CHECK_INTERVAL =
            5_000L
    }

    private val serviceScope =
        CoroutineScope(
            Dispatchers.IO + Job()
        )

    private val fusedLocationClient by lazy {
        LocationServices
            .getFusedLocationProviderClient(
                this
            )
    }

    private val locationManager by lazy {
        getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager
    }

    private val driverLocationRepository =
        DriverLocationRepository()

    private val trackingEventRepository =
        DriverTrackingEventRepository()

    private val sessionManager by lazy {
        SessionManager(
            applicationContext
        )
    }

    /*
     * Último estado conocido.
     *
     * null  = todavía no comprobado
     * true  = ubicación activada
     * false = ubicación desactivada
     */
    private var lastLocationEnabledState:
            Boolean? = null

    private var receiverRegistered =
        false

    /*
     * Job encargado de comprobar
     * periódicamente el estado del GPS.
     */
    private var gpsMonitorJob:
            Job? = null

    private val locationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        )
            .setMinUpdateIntervalMillis(
                5_000L
            )
            .build()

    /*
     * Recibe las coordenadas normales.
     */
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

                    if (
                        token.isNullOrBlank()
                    ) {
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
                        .onSuccess {

                            Log.d(
                                TAG,
                                "Ubicación enviada: " +
                                        "${location.latitude}, " +
                                        "${location.longitude}"
                            )
                        }
                        .onFailure { error ->

                            Log.e(
                                TAG,
                                "Error enviando ubicación",
                                error
                            )
                        }
                }
            }
        }

    /*
     * Primer mecanismo:
     *
     * Android puede avisarnos cuando
     * cambian los proveedores de ubicación.
     */
    private val locationStateReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                if (
                    intent?.action ==
                    LocationManager
                        .PROVIDERS_CHANGED_ACTION
                ) {
                    Log.d(
                        TAG,
                        "Cambio de proveedor de ubicación detectado"
                    )

                    checkLocationState(
                        sendOnlyIfChanged =
                            true
                    )
                }
            }
        }

    override fun onCreate() {
        super.onCreate()

        TrackingStatusManager.setRunning(true)

        createNotificationChannel()

        registerLocationStateReceiver()

        /*
         * Conocemos el estado inicial.
         */
        checkLocationState(
            sendOnlyIfChanged =
                false
        )

        /*
         * Segundo mecanismo:
         *
         * comprobación periódica.
         *
         * Esto nos protege cuando un
         * dispositivo no entrega correctamente
         * PROVIDERS_CHANGED_ACTION.
         */
        startGpsStateMonitor()
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

    /*
     * Comprueba si Android tiene
     * los servicios de ubicación activos.
     */
    private fun isLocationEnabled():
            Boolean {

        return try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.P
            ) {
                locationManager
                    .isLocationEnabled
            } else {
                locationManager
                    .isProviderEnabled(
                        LocationManager
                            .GPS_PROVIDER
                    ) ||
                        locationManager
                            .isProviderEnabled(
                                LocationManager
                                    .NETWORK_PROVIDER
                            )
            }

        } catch (
            exception: Exception
        ) {

            Log.e(
                TAG,
                "Error consultando estado GPS",
                exception
            )

            false
        }
    }

    /*
     * Compara el estado actual con
     * el último estado conocido.
     *
     * Solo envía un evento cuando
     * realmente existe un cambio.
     */
    private fun checkLocationState(
        sendOnlyIfChanged: Boolean
    ) {

        val enabled =
            isLocationEnabled()

        val previousState =
            lastLocationEnabledState

        if (
            sendOnlyIfChanged &&
            previousState == enabled
        ) {
            return
        }

        lastLocationEnabledState =
            enabled

        if (enabled) {

            Log.i(
                TAG,
                "GPS / ubicación ACTIVADA"
            )

            sendTrackingEvent(
                "GPS_ENABLED"
            )

        } else {

            Log.w(
                TAG,
                "GPS / ubicación DESACTIVADA"
            )

            sendTrackingEvent(
                "GPS_DISABLED"
            )
        }
    }

    /*
     * Revisa el estado del GPS
     * cada 5 segundos.
     *
     * No manda eventos cada 5 segundos:
     * checkLocationState() compara contra
     * lastLocationEnabledState.
     */
    private fun startGpsStateMonitor() {

        if (
            gpsMonitorJob?.isActive ==
            true
        ) {
            return
        }

        gpsMonitorJob =
            serviceScope.launch {

                while (isActive) {

                    delay(
                        GPS_CHECK_INTERVAL
                    )

                    checkLocationState(
                        sendOnlyIfChanged =
                            true
                    )
                }
            }
    }

    private fun stopGpsStateMonitor() {

        gpsMonitorJob?.cancel()

        gpsMonitorJob =
            null
    }

    /*
     * Envía GPS_ENABLED o GPS_DISABLED
     * al backend.
     */
    private fun sendTrackingEvent(
        eventType: String
    ) {

        serviceScope.launch {

            val token =
                sessionManager
                    .accessToken
                    .first()

            if (
                token.isNullOrBlank()
            ) {

                Log.w(
                    TAG,
                    "No hay token para enviar $eventType"
                )

                return@launch
            }

            trackingEventRepository
                .sendEvent(
                    accessToken =
                        token,
                    eventType =
                        eventType
                )
                .onSuccess {

                    Log.i(
                        TAG,
                        "Evento enviado: $eventType"
                    )
                }
                .onFailure { error ->

                    Log.e(
                        TAG,
                        "Error enviando evento $eventType",
                        error
                    )
                }
        }
    }

    /*
     * Registra el BroadcastReceiver.
     */
    private fun registerLocationStateReceiver() {

        if (
            receiverRegistered
        ) {
            return
        }

        val filter =
            IntentFilter(
                LocationManager
                    .PROVIDERS_CHANGED_ACTION
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            registerReceiver(
                locationStateReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                locationStateReceiver,
                filter
            )
        }

        receiverRegistered =
            true
    }

    private fun unregisterLocationStateReceiver() {

        if (
            !receiverRegistered
        ) {
            return
        }

        try {

            unregisterReceiver(
                locationStateReceiver
            )

        } catch (
            exception: Exception
        ) {

            Log.w(
                TAG,
                "Receiver ya estaba eliminado",
                exception
            )
        }

        receiverRegistered =
            false
    }

    /*
     * Inicia las actualizaciones
     * normales de coordenadas.
     */
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

            Log.w(
                TAG,
                "Permiso de ubicación no concedido"
            )

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

        stopGpsStateMonitor()

        unregisterLocationStateReceiver()

        serviceScope.cancel()

        TrackingStatusManager.setRunning(
            false
        )

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
                "Jornada activa · Compartiendo ubicación"
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

            manager
                .createNotificationChannel(
                    channel
                )
        }
    }
}