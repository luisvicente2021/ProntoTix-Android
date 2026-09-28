package com.luisvicente.prontotix.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShiftAlarmReceiver :
    BroadcastReceiver() {

    companion object {
        private const val TAG =
            "ShiftAlarmReceiver"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        /*
         * BroadcastReceiver no puede quedarse
         * ejecutando operaciones suspend largas
         * directamente.
         */
        val pendingResult =
            goAsync()

        CoroutineScope(
            Dispatchers.IO
        ).launch {

            try {

                when (intent.action) {

                    ShiftScheduler
                        .ACTION_START_SHIFT -> {

                        Log.i(
                            TAG,
                            "=== INICIO AUTOMÁTICO DE JORNADA ==="
                        )

                        val result =
                            ShiftAutomationManager
                                .startAutomaticShift(
                                    context
                                )

                        when (result) {

                            StartShiftResult.SUCCESS -> {
                                Log.i(
                                    TAG,
                                    "Inicio automático completado correctamente"
                                )
                            }

                            StartShiftResult.RETRY -> {
                                if (WorkSchedule.isWithinWorkingHours()) {

                                    Log.w(
                                        TAG,
                                        "Fallo temporal. Se programará un reintento."
                                    )

                                    ShiftScheduler
                                        .scheduleStartRetry(
                                            context = context.applicationContext,
                                            delayMinutes = 1
                                        )

                                } else {
                                    Log.w(
                                        TAG,
                                        "Falló el inicio, pero ya estamos fuera del horario laboral."
                                    )
                                }
                            }

                            StartShiftResult.NO_SESSION -> {
                                Log.w(
                                    TAG,
                                    "No existe sesión. No se programará reintento."
                                )
                            }
                        }
                    }

                    ShiftScheduler
                        .ACTION_END_SHIFT -> {

                        Log.i(
                            TAG,
                            "=== FIN AUTOMÁTICO DE JORNADA ==="
                        )

                        ShiftAutomationManager
                            .endAutomaticShift(
                                context
                            )
                    }
                }

            } catch (
                exception: Exception
            ) {

                Log.e(
                    TAG,
                    "Error ejecutando automatización",
                    exception
                )

            } finally {

                /*
                 * Volvemos a programar
                 * las siguientes alarmas.
                 */
                ShiftScheduler
                    .scheduleDailyShift(
                        context.applicationContext
                    )

                pendingResult.finish()
            }
        }
    }
}
