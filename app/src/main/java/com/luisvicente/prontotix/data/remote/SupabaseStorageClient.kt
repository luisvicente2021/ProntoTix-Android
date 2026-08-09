package com.luisvicente.prontotix.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit

object SupabaseStorageClient {

    private lateinit var service: SupabaseStorageApiService

    fun initialize(
        supabaseUrl: String
    ) {
        if (::service.isInitialized) {
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(
                if (supabaseUrl.endsWith("/")) {
                    supabaseUrl
                } else {
                    "$supabaseUrl/"
                }
            )
            .client(
                OkHttpClient.Builder()
                    .build()
            )
            .build()

        service = retrofit.create(
            SupabaseStorageApiService::class.java
        )
    }

    fun api(): SupabaseStorageApiService {
        check(::service.isInitialized) {
            "SupabaseStorageClient no inicializado"
        }

        return service
    }
}