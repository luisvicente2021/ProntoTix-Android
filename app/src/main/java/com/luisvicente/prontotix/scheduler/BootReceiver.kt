package com.luisvicente.prontotix.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.i(
            TAG,
            "Teléfono reiniciado"
        )

        // Android elimina las alarmas después
        // de reiniciar, así que las programamos otra vez.
        ShiftScheduler.scheduleDailyShift(
            context.applicationContext
        )

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val now = Calendar.getInstance()

                val minutes =
                    now.get(Calendar.HOUR_OF_DAY) * 60 +
                    now.get(Calendar.MINUTE)

                Log.i(
                    TAG,
                    "Hora detectada después del reinicio: $minutes minutos"
                )

                val shiftStart = 9 * 60
                val shiftEnd = 18 * 60 + 30

                if (minutes in shiftStart until shiftEnd) {

                    Log.i(
                        TAG,
                        "Reinicio dentro de jornada. Recuperando seguimiento."
                    )

                    ShiftAutomationManager
                        .startAutomaticShift(
                            context.applicationContext
                        )

                    Log.i(
                        TAG,
                        "Recuperación automática terminada"
                    )

                } else {

                    Log.i(
                        TAG,
                        "Reinicio fuera del horario de jornada"
                    )
                }

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Error recuperando jornada después del reinicio",
                    exception
                )

            } finally {

                pendingResult.finish()
            }
        }
    }
}
