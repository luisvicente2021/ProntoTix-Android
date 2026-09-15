package com.luisvicente.prontotix

import android.app.Application
import com.luisvicente.prontotix.data.remote.BackendRetrofitClient

class ProntoTixApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        BackendRetrofitClient.initialize(
            applicationContext
        )
    }
}
