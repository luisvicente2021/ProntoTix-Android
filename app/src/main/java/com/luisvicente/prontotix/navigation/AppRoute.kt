package com.luisvicente.prontotix.navigation

object AppRoute {

    const val LOGIN = "login"
    const val DRIVER_HOME = "driver_home"
    const val ADMIN = "admin"
    const val TICKETS = "tickets"
    const val DIAGNOSTICS = "diagnostics"

    const val TICKET_DETAIL =
        "ticket_detail/{ticketId}"

    const val LOCATION_TEST =
        "location_test"

    const val DELIVERY_REPORT =
        "delivery_report/{ticketId}"

    fun ticketDetail(
        ticketId: Long
    ): String {

        return "ticket_detail/$ticketId"
    }

    fun deliveryReport(
        ticketId: Long
    ): String {

        return "delivery_report/$ticketId"
    }
}