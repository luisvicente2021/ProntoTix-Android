package com.luisvicente.prontotix.navigation

object AppRoute {
    const val LOGIN = "login"
    const val TICKETS = "tickets"
    const val TICKET_DETAIL = "ticket_detail/{ticketId}"
    const val LOCATION_TEST = "location_test"

    fun ticketDetail(ticketId: Long): String {
        return "ticket_detail/$ticketId"
    }

    const val DELIVERY_REPORT = "delivery_report/{ticketId}"

    fun deliveryReport(ticketId: Long): String {
        return "delivery_report/$ticketId"
    }
}