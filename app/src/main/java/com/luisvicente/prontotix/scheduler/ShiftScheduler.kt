package com.luisvicente.prontotix.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ShiftScheduler {

    private const val TAG = "ShiftScheduler"

    const val ACTION_START_SHIFT =
        "com.luisvicente.prontotix.START_SHIFT"

    const val ACTION_END_SHIFT =
        "com.luisvicente.prontotix.END_SHIFT"

    private const val START_REQUEST_CODE = 900
    private const val END_REQUEST_CODE = 1830

    fun scheduleDailyShift(
        context: Context
    ) {

        val now =
            LocalDateTime.now()

        val nextStart =
            findNextAlarm(
                now = now,
                getTime = {
                    WorkSchedule.startTime(it)
                }
            )

        val nextEnd =
            findNextAlarm(
                now = now,
                getTime = {
                    WorkSchedule.endTime(it)
                }
            )

        scheduleAlarm(
            context = context,
            dateTime = nextStart,
            action = ACTION_START_SHIFT,
            requestCode = START_REQUEST_CODE
        )

        scheduleAlarm(
            context = context,
            dateTime = nextEnd,
            action = ACTION_END_SHIFT,
            requestCode = END_REQUEST_CODE
        )
    }

    private fun findNextAlarm(
        now: LocalDateTime,
        getTime: (LocalDate) -> LocalTime?
    ): LocalDateTime {

        var date =
            now.toLocalDate()

        repeat(8) {

            val time =
                getTime(date)

            if (time != null) {

                val candidate =
                    LocalDateTime.of(
                        date,
                        time
                    )

                if (candidate.isAfter(now)) {
                    return candidate
                }
            }

            date =
                date.plusDays(1)
        }

        /*
         * Con la configuración actual siempre
         * encontraremos un día laborable dentro
         * de los siguientes 8 días.
         */
        error(
            "No se encontró próxima alarma de jornada"
        )
    }

    private fun scheduleAlarm(
        context: Context,
        dateTime: LocalDateTime,
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

        val triggerAtMillis =
            dateTime
                .atZone(
                    ZoneId.systemDefault()
                )
                .toInstant()
                .toEpochMilli()

        val canScheduleExact =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S
            ) {
                alarmManager
                    .canScheduleExactAlarms()
            } else {
                true
            }

        if (canScheduleExact) {

            alarmManager
                .setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )

            Log.d(
                TAG,
                "Alarma exacta programada: " +
                        "$action -> $dateTime"
            )

        } else {

            alarmManager
                .setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )

            Log.w(
                TAG,
                "Alarma aproximada programada: " +
                        "$action -> $dateTime"
            )
        }
    }
}