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
                            "=== 09:00 INICIO AUTOMÁTICO ==="
                        )

                        ShiftAutomationManager
                            .startAutomaticShift(
                                context
                            )
                    }

                    ShiftScheduler
                        .ACTION_END_SHIFT -> {

                        Log.i(
                            TAG,
                            "=== 18:30 FIN AUTOMÁTICO ==="
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
