package com.luisvicente.prontotix.admin

import android.content.Context

object MaintenanceModeManager {

    private const val PREFS_NAME =
        "prontotix_admin_preferences"

    private const val KEY_MAINTENANCE_MODE =
        "maintenance_mode"

    fun isEnabled(
        context: Context
    ): Boolean {

        return context
            .applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .getBoolean(
                KEY_MAINTENANCE_MODE,
                false
            )
    }

    fun setEnabled(
        context: Context,
        enabled: Boolean
    ) {

        context
            .applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putBoolean(
                KEY_MAINTENANCE_MODE,
                enabled
            )
            .apply()
    }
}