package com.luisvicente.prontotix.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val action = intent.action

        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val appContext =
            context.applicationContext

        when (action) {

            Intent.ACTION_BOOT_COMPLETED ->
                Log.i(
                    TAG,
                    "Teléfono reiniciado"
                )

            Intent.ACTION_MY_PACKAGE_REPLACED ->
                Log.i(
                    TAG,
                    "ProntoTix actualizado"
                )
        }

        /*
         * Las alarmas pueden necesitar ser
         * programadas nuevamente después
         * de un reinicio o actualización.
         */
        ShiftScheduler.scheduleDailyShift(
            appContext
        )

        val pendingResult =
            goAsync()

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val now =
                    LocalDateTime.now()

                Log.i(
                    TAG,
                    "Fecha y hora detectadas: $now"
                )

                if (
                    WorkSchedule
                        .isWithinWorkingHours(now)
                ) {

                    Log.i(
                        TAG,
                        "Dentro de jornada. Recuperando seguimiento."
                    )

                    val result =
                        ShiftAutomationManager
                            .startAutomaticShift(
                                appContext
                            )

                    when (result) {

                        StartShiftResult.SUCCESS -> {
                            Log.i(
                                TAG,
                                "Recuperación automática completada"
                            )
                        }

                        StartShiftResult.RETRY -> {
                            Log.w(
                                TAG,
                                "Falló la recuperación. Se programará un reintento."
                            )

                            ShiftScheduler
                                .scheduleStartRetry(
                                    context = appContext,
                                    delayMinutes = 1
                                )
                        }

                        StartShiftResult.NO_SESSION -> {
                            Log.w(
                                TAG,
                                "No existe sesión. No se programará reintento."
                            )
                        }
                    }

                } else {

                    Log.i(
                        TAG,
                        "Fuera del horario de jornada"
                    )
                }

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Error recuperando jornada",
                    exception
                )

            } finally {

                pendingResult.finish()
            }
        }
    }
}