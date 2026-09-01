package com.luisvicente.prontotix.data.remote

import android.content.Context
import com.luisvicente.prontotix.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BackendRetrofitClient {

    private lateinit var appContext: Context

    fun initialize(
        context: Context
    ) {
        appContext =
            context.applicationContext
    }

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                HttpLoggingInterceptor.Level.BODY
        }

    private val client: OkHttpClient by lazy {

        check(
            ::appContext.isInitialized
        ) {
            "BackendRetrofitClient debe inicializarse antes de utilizarse"
        }

        OkHttpClient.Builder()
            .authenticator(
                TokenAuthenticator(
                    appContext
                )
            )
            .addInterceptor(
                loggingInterceptor
            )
            .build()
    }

    val ticketsApiService:
        TicketsApiService by lazy {

        Retrofit.Builder()
            .baseUrl(
                BuildConfig.BACKEND_URL
            )
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(
                TicketsApiService::class.java
            )
    }
}
