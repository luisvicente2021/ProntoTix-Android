package com.luisvicente.prontotix.scheduler

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object WorkSchedule {

    private val WEEKDAY_START =
        LocalTime.of(9, 0)

    private val WEEKDAY_END =
        LocalTime.of(18, 30)

    private val SATURDAY_END =
        LocalTime.of(14, 0)

    fun isWorkingDay(
        date: LocalDate = LocalDate.now()
    ): Boolean {
        return date.dayOfWeek != DayOfWeek.SUNDAY
    }

    fun startTime(
        date: LocalDate = LocalDate.now()
    ): LocalTime? {
        return if (isWorkingDay(date)) {
            WEEKDAY_START
        } else {
            null
        }
    }

    fun endTime(
        date: LocalDate = LocalDate.now()
    ): LocalTime? {
        return when (date.dayOfWeek) {

            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY ->
                WEEKDAY_END

            DayOfWeek.SATURDAY ->
                SATURDAY_END

            DayOfWeek.SUNDAY ->
                null
        }
    }

    fun isWithinWorkingHours(
        dateTime: LocalDateTime =
            LocalDateTime.now()
    ): Boolean {

        val start =
            startTime(dateTime.toLocalDate())
                ?: return false

        val end =
            endTime(dateTime.toLocalDate())
                ?: return false

        val time =
            dateTime.toLocalTime()

        return !time.isBefore(start) &&
            time.isBefore(end)
    }
}
