package com.luisvicente.prontotix.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SupabaseStorageApiService {

    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun uploadFile(
        @Header("Authorization")
        authorization: String,

        @Header("apikey")
        apiKey: String,

        @Header("Content-Type")
        contentType: String,

        @Header("x-upsert")
        upsert: String = "true",

        @Path("bucket")
        bucket: String,

        @Path(
            value = "path",
            encoded = true
        )
        path: String,

        @Body
        body: RequestBody
    ): Response<ResponseBody>
}