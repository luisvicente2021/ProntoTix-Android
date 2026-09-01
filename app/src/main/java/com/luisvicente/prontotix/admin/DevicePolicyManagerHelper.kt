package com.luisvicente.prontotix.admin

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserManager
import android.util.Log

object DevicePolicyManagerHelper {

    private const val TAG = "ProntoDevicePolicy"

    fun isDeviceOwner(context: Context): Boolean {

        val dpm =
            context.getSystemService(
                Context.DEVICE_POLICY_SERVICE
            ) as DevicePolicyManager

        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun enforceLocation(context: Context): Boolean {

        val dpm =
            context.getSystemService(
                Context.DEVICE_POLICY_SERVICE
            ) as DevicePolicyManager

        if (!dpm.isDeviceOwnerApp(context.packageName)) {
            Log.e(TAG, "ProntoTix NO es Device Owner")
            return false
        }

        val admin =
            ComponentName(
                context,
                ProntoDeviceAdminReceiver::class.java
            )

        return try {

            // 1. Encender ubicación general del teléfono
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                dpm.setLocationEnabled(
                    admin,
                    true
                )
            }

            // 2. Impedir que el usuario apague
            //    la ubicación general del teléfono
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.addUserRestriction(
                    admin,
                    UserManager.DISALLOW_CONFIG_LOCATION
                )
            }

            Log.d(
                TAG,
                "Ubicación ACTIVADA y BLOQUEADA por Device Owner"
            )

            true

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Error aplicando política de ubicación",
                e
            )

            false
        }
    }

    fun lockLocationPermission(context: Context): Boolean {

        val dpm =
            context.getSystemService(
                Context.DEVICE_POLICY_SERVICE
            ) as DevicePolicyManager

        if (!dpm.isDeviceOwnerApp(context.packageName)) {
            Log.e(TAG, "No se puede bloquear permiso: NO es Device Owner")
            return false
        }

        val admin =
            ComponentName(
                context,
                ProntoDeviceAdminReceiver::class.java
            )

        return try {

            val fineGranted =
                dpm.setPermissionGrantState(
                    admin,
                    context.packageName,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                )

            val coarseGranted =
                dpm.setPermissionGrantState(
                    admin,
                    context.packageName,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                )

            Log.d(
                TAG,
                "Permiso GPS BLOQUEADO - FINE=$fineGranted COARSE=$coarseGranted"
            )

            fineGranted && coarseGranted

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Error bloqueando permiso GPS de ProntoTix",
                e
            )

            false
        }
    }

    fun releaseLocationRestriction(context: Context): Boolean {

        val dpm =
            context.getSystemService(
                Context.DEVICE_POLICY_SERVICE
            ) as DevicePolicyManager

        if (!dpm.isDeviceOwnerApp(context.packageName)) {
            return false
        }

        val admin =
            ComponentName(
                context,
                ProntoDeviceAdminReceiver::class.java
            )

        return try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                dpm.clearUserRestriction(
                    admin,
                    UserManager.DISALLOW_CONFIG_LOCATION
                )
            }

            Log.d(
                TAG,
                "Restricción de ubicación liberada"
            )

            true

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Error liberando política de ubicación",
                e
            )

            false
        }
    }

    fun blockAppUninstall(context: Context): Boolean {

        val dpm =
            context.getSystemService(
                Context.DEVICE_POLICY_SERVICE
            ) as DevicePolicyManager

        if (!dpm.isDeviceOwnerApp(context.packageName)) {
            Log.e(TAG, "No se puede bloquear desinstalación: NO es Device Owner")
            return false
        }

        val admin =
            ComponentName(
                context,
                ProntoDeviceAdminReceiver::class.java
            )

        return try {

            dpm.setUninstallBlocked(
                admin,
                context.packageName,
                true
            )

            Log.d(
                TAG,
                "Desinstalación de ProntoTix BLOQUEADA"
            )

            true

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Error bloqueando desinstalación de ProntoTix",
                e
            )

            false
        }
    }
}