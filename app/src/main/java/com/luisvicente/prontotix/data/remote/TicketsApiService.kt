package com.luisvicente.prontotix.data.remote

import com.luisvicente.prontotix.data.model.Ticket
import com.luisvicente.prontotix.data.model.UpdateTicketStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

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

    @PUT("api/tickets/{id}")
    suspend fun updateTicketStatus(
        @Header("Authorization") authorization: String,
        @Path("id") ticketId: Long,
        @Body request: UpdateTicketStatusRequest
    ): Response<Ticket>
}