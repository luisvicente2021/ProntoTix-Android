package com.luisvicente.prontotix.scheduler

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.luisvicente.prontotix.admin.DevicePolicyManagerHelper
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.repository.AuthRepository
import com.luisvicente.prontotix.data.repository.DriverShiftRepository
import com.luisvicente.prontotix.service.LocationTrackingService
import kotlinx.coroutines.flow.first
import com.luisvicente.prontotix.admin.MaintenanceModeManager

object ShiftAutomationManager {

    private const val TAG =
        "ShiftAutomation"

    suspend fun startAutomaticShift(
        context: Context
    ) {

        Log.i(
            TAG,
            "Iniciando proceso automático de jornada"
        )

        val appContext =
            context.applicationContext

        val sessionManager =
            SessionManager(appContext)

        val token =
            getValidAccessToken(
                sessionManager
            )

        if (token.isNullOrBlank()) {

            Log.e(
                TAG,
                "No existe una sesión válida"
            )

            return
        }

        val shiftRepository =
            DriverShiftRepository()

        val activeShiftResult =
            shiftRepository
                .getActiveShift(token)

        activeShiftResult
            .onSuccess { response ->

                if (response.active) {

                    Log.i(
                        TAG,
                        "Ya existe una jornada activa"
                    )

                    startTracking(
                        appContext
                    )

                } else {

                    /*
                     * No existe jornada:
                     * la iniciamos automáticamente.
                     */
                }
            }
            .onFailure { error ->

                Log.e(
                    TAG,
                    "No se pudo consultar jornada activa",
                    error
                )
            }

        /*
         * Si ya estaba activa terminamos aquí.
         */
        val activeResponse =
            activeShiftResult.getOrNull()

        if (
            activeResponse?.active == true
        ) {
            return
        }

        /*
         * Si ni siquiera pudimos consultar
         * el backend, no iniciamos GPS.
         */
        if (activeResponse == null) {
            return
        }

        shiftRepository
            .startShift(token)
            .onSuccess {

                Log.i(
                    TAG,
                    "Jornada iniciada automáticamente"
                )

                startTracking(
                    appContext
                )
            }
            .onFailure { error ->

                Log.e(
                    TAG,
                    "Error iniciando jornada automática",
                    error
                )
            }
    }

    suspend fun endAutomaticShift(
        context: Context
    ) {

        Log.i(
            TAG,
            "Finalizando jornada automática"
        )

        val appContext =
            context.applicationContext

        /*
         * PRIMERO detenemos el rastreo.
         *
         * Así garantizamos que después
         * de las 18:30 no continuemos
         * enviando coordenadas aunque
         * el backend tenga algún problema.
         */
        stopTracking(
            appContext
        )

        val sessionManager =
            SessionManager(appContext)

        val token =
            getValidAccessToken(
                sessionManager
            )

        if (token.isNullOrBlank()) {

            Log.e(
                TAG,
                "No existe sesión válida para cerrar jornada"
            )

            return
        }

        val shiftRepository =
            DriverShiftRepository()

        shiftRepository
            .getActiveShift(token)
            .onSuccess { response ->

                if (!response.active) {

                    Log.i(
                        TAG,
                        "No existe jornada activa para finalizar"
                    )

                    return@onSuccess
                }

                shiftRepository
                    .endShift(token)
                    .onSuccess {

                        Log.i(
                            TAG,
                            "Jornada finalizada automáticamente"
                        )
                    }
                    .onFailure { error ->

                        Log.e(
                            TAG,
                            "Error finalizando jornada",
                            error
                        )
                    }
            }
            .onFailure { error ->

                Log.e(
                    TAG,
                    "No se pudo consultar jornada al finalizar",
                    error
                )
            }
    }

    private suspend fun getValidAccessToken(
        sessionManager: SessionManager
    ): String? {

        val accessToken =
            sessionManager
                .accessToken
                .first()

        val refreshToken =
            sessionManager
                .refreshToken
                .first()

        /*
         * Si tenemos refresh token,
         * intentamos renovar siempre antes
         * de ejecutar la automatización.
         */
        if (
            !refreshToken.isNullOrBlank()
        ) {

            val authRepository =
                AuthRepository()

            val refreshResult =
                authRepository
                    .refreshSession(
                        refreshToken
                    )

            refreshResult
                .onSuccess { response ->

                    val newAccessToken =
                        response.access_token

                    if (
                        !newAccessToken.isNullOrBlank()
                    ) {

                        sessionManager
                            .saveSession(
                                accessToken =
                                    newAccessToken,

                                refreshToken =
                                    response.refresh_token
                                        ?: refreshToken
                            )
                    }
                }

            val refreshed =
                refreshResult.getOrNull()
                    ?.access_token

            if (
                !refreshed.isNullOrBlank()
            ) {
                return refreshed
            }
        }

        /*
         * Compatibilidad con las sesiones
         * antiguas que solamente tienen
         * access token.
         */
        return accessToken
    }

    private fun startTracking(
        context: Context
    ) {

        /*
         * Si ProntoTix es Device Owner:
         *
         * - encendemos ubicación
         * - evitamos que se desactive
         * - concedemos los permisos
         */
        val maintenanceMode =
            MaintenanceModeManager
                .isEnabled(context)

        if (
            DevicePolicyManagerHelper
                .isDeviceOwner(context) &&
            !maintenanceMode
        ) {

            DevicePolicyManagerHelper
                .enforceLocation(context)

            DevicePolicyManagerHelper
                .lockLocationPermission(context)

            DevicePolicyManagerHelper
                .enforceWorkDeviceRestrictions(context)

            DevicePolicyManagerHelper
                .blockAppUninstall(context)
        }

        val intent =
            Intent(
                context,
                LocationTrackingService::class.java
            )

        ContextCompat
            .startForegroundService(
                context,
                intent
            )

        Log.i(
            TAG,
            "Servicio GPS iniciado"
        )
    }

    private fun stopTracking(
        context: Context
    ) {

        val intent =
            Intent(
                context,
                LocationTrackingService::class.java
            )

        context.stopService(
            intent
        )

        /*
         * Fuera del horario laboral dejamos
         * de impedir que se configure GPS.
         */
        if (
            DevicePolicyManagerHelper
                .isDeviceOwner(context)
        ) {

            DevicePolicyManagerHelper
                .releaseLocationRestriction(
                    context
                )
        }

        Log.i(
            TAG,
            "Servicio GPS detenido"
        )
    }
}
