package com.luisvicente.prontotix.data.remote

import com.luisvicente.prontotix.data.model.CreateDeliveryReportEvidenceRequest
import com.luisvicente.prontotix.data.model.Ticket
import com.luisvicente.prontotix.data.model.UpdateTicketStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import com.luisvicente.prontotix.data.model.DeliveryReportRequest
import com.luisvicente.prontotix.data.model.DeliveryReportResponse
import com.luisvicente.prontotix.data.model.UpdateDeliveryReportFilesRequest
import retrofit2.http.PATCH
import retrofit2.http.POST
import com.luisvicente.prontotix.data.model.DriverLocationRequest

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

    @POST("api/tickets/{id}/delivery-report")
    suspend fun createDeliveryReport(
        @Header("Authorization") authorization: String,
        @Path("id") ticketId: Long,
        @Body request: DeliveryReportRequest
    ): Response<DeliveryReportResponse>

    @POST("api/tickets/{id}/delivery-report/evidence")
    suspend fun addDeliveryReportEvidence(
        @Header("Authorization") authorization: String,
        @Path("id") ticketId: Long,
        @Body request: CreateDeliveryReportEvidenceRequest
    ): Response<Unit>

    @PATCH("api/tickets/{id}/delivery-report/files")
    suspend fun updateDeliveryReportFiles(
        @Header("Authorization") authorization: String,
        @Path("id") ticketId: Long,
        @Body request: UpdateDeliveryReportFilesRequest
    ): Response<DeliveryReportResponse>

    @POST("api/driver-location")
    suspend fun updateDriverLocation(
        @Header("Authorization") authorization: String,
        @Body request: DriverLocationRequest
    ): Response<Unit>
}