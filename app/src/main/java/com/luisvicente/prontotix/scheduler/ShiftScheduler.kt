package com.luisvicente.prontotix.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object ShiftScheduler {

    private const val TAG = "ShiftScheduler"

    const val ACTION_START_SHIFT =
        "com.luisvicente.prontotix.START_SHIFT"

    const val ACTION_END_SHIFT =
        "com.luisvicente.prontotix.END_SHIFT"

    private const val START_REQUEST_CODE = 900
    private const val END_REQUEST_CODE = 1830

    fun scheduleDailyShift(context: Context) {

        scheduleAlarm(
            context = context,
            hour = 9,
            minute = 0,
            action = ACTION_START_SHIFT,
            requestCode = START_REQUEST_CODE
        )

        scheduleAlarm(
            context = context,
            hour = 18,
            minute = 30,
            action = ACTION_END_SHIFT,
            requestCode = END_REQUEST_CODE
        )
    }

    private fun scheduleAlarm(
        context: Context,
        hour: Int,
        minute: Int,
        action: String,
        requestCode: Int
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                ShiftAlarmReceiver::class.java
            ).apply {
                this.action = action
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val calendar =
            Calendar.getInstance().apply {

                set(
                    Calendar.HOUR_OF_DAY,
                    hour
                )

                set(
                    Calendar.MINUTE,
                    minute
                )

                set(
                    Calendar.SECOND,
                    0
                )

                set(
                    Calendar.MILLISECOND,
                    0
                )

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(
                        Calendar.DAY_OF_YEAR,
                        1
                    )
                }
            }

        val canScheduleExact =
            if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S
            ) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

        if (canScheduleExact) {

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Log.d(
                TAG,
                "Alarma exacta programada: $hour:$minute"
            )

        } else {

            /*
             * Fallback mientras configuramos
             * el permiso de alarmas exactas.
             */
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Log.w(
                TAG,
                "Sin permiso exacto. Alarma aproximada: $hour:$minute"
            )
        }
    }
}
