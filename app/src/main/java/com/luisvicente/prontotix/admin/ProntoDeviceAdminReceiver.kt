package com.luisvicente.prontotix.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ProntoDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(
        context: Context,
        intent: Intent
    ) {
        super.onEnabled(context, intent)

        Log.d(
            "ProntoDeviceAdmin",
            "Administrador del dispositivo habilitado"
        )
    }

    override fun onDisabled(
        context: Context,
        intent: Intent
    ) {
        super.onDisabled(context, intent)

        Log.d(
            "ProntoDeviceAdmin",
            "Administrador del dispositivo deshabilitado"
        )
    }
}
