package com.luisvicente.prontotix.navigation

object AppRoute {
    const val LOGIN = "login"
    const val TICKETS = "tickets"
    const val TICKET_DETAIL = "ticket_detail/{ticketId}"

    fun ticketDetail(ticketId: Long): String {
        return "ticket_detail/$ticketId"
    }
}