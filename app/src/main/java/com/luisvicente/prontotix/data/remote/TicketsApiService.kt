package com.luisvicente.prontotix.data.remote

import com.luisvicente.prontotix.data.model.Ticket
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface TicketsApiService {

    @GET("api/tickets")
    suspend fun getTickets(
        @Header("Authorization") authorization: String
    ): Response<List<Ticket>>


    @GET("api/tickets/{id}")
    suspend fun getTicketDetail(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Path("id") ticketId: Long
    ): Response<Ticket>
}