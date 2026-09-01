package com.luisvicente.prontotix

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.luisvicente.prontotix.admin.DevicePolicyManagerHelper
import com.luisvicente.prontotix.navigation.AppNavigation
import com.luisvicente.prontotix.ui.theme.ProntoTixTheme
import com.luisvicente.prontotix.data.remote.BackendRetrofitClient

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BackendRetrofitClient.initialize(
            applicationContext
        )

        val isOwner =
            DevicePolicyManagerHelper.isDeviceOwner(this)

        Log.d(
            "ProntoDevicePolicy",
            "¿ProntoTix es Device Owner? $isOwner"
        )

        if (isOwner) {
            DevicePolicyManagerHelper.enforceLocation(this)
            DevicePolicyManagerHelper.lockLocationPermission(this)
            DevicePolicyManagerHelper.blockAppUninstall(this)
        }

        enableEdgeToEdge()

        setContent {
            ProntoTixTheme {
                AppNavigation()
            }
        }
    }
}
