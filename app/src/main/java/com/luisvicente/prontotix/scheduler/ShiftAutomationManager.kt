package com.luisvicente.prontotix.scheduler

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.repository.DriverShiftRepository
import com.luisvicente.prontotix.service.LocationTrackingService
import kotlinx.coroutines.flow.first

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

        /*
         * Utilizamos el access token actualmente
         * almacenado.
         *
         * Si el backend responde 401 porque el
         * token expiró, TokenAuthenticator se
         * encargará de renovar la sesión y
         * reintentar automáticamente la petición.
         */
        val token =
            getAccessToken(
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
         * Si ya existe una jornada activa,
         * el GPS ya fue iniciado arriba.
         */
        val activeResponse =
            activeShiftResult.getOrNull()

        if (
            activeResponse?.active == true
        ) {
            return
        }

        /*
         * Si no pudimos consultar el backend,
         * no creamos una jornada a ciegas.
         *
         * Esto evita posibles jornadas
         * duplicadas cuando hay un problema
         * de Internet o del servidor.
         */
        if (activeResponse == null) {

            Log.e(
                TAG,
                "No se iniciará jornada porque no fue posible consultar su estado"
            )

            return
        }

        /*
         * El backend respondió correctamente
         * y confirmó que no existe una jornada
         * activa. Creamos una nueva.
         */
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
         * Primero detenemos el rastreo.
         *
         * Así garantizamos que después del
         * fin de jornada no continuemos
         * enviando coordenadas aunque exista
         * algún problema con el backend.
         */
        stopTracking(
            appContext
        )

        val sessionManager =
            SessionManager(appContext)

        val token =
            getAccessToken(
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

    /*
     * No renovamos anticipadamente la sesión.
     *
     * BackendRetrofitClient utiliza
     * TokenAuthenticator, que detecta una
     * respuesta 401, renueva la sesión,
     * guarda los nuevos tokens y reintenta
     * automáticamente la petición.
     */
    private suspend fun getAccessToken(
        sessionManager: SessionManager
    ): String? {

        return sessionManager
            .accessToken
            .first()
    }

    private fun startTracking(
        context: Context
    ) {

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

        Log.i(
            TAG,
            "Servicio GPS detenido"
        )
    }
}