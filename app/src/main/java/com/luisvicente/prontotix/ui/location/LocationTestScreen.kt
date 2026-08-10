package com.luisvicente.prontotix.ui.location

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.repository.DriverLocationRepository
import com.luisvicente.prontotix.data.repository.LocationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LocationTestScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationRepository = remember {
        LocationRepository(context)
    }

    val driverLocationRepository = remember {
        DriverLocationRepository()
    }

    val sessionManager = remember {
        SessionManager(context.applicationContext)
    }

    var locationText by remember {
        mutableStateOf("Ubicación todavía no obtenida")
    }

    fun obtainLocation() {

        scope.launch {

            val locationResult =
                locationRepository.getCurrentLocation()

            locationResult.onSuccess { location ->

                locationText =
                    """
                    Latitud: ${location.latitude}
                    Longitud: ${location.longitude}
                    
                    Enviando al servidor...
                    """.trimIndent()

                val token =
                    sessionManager.accessToken.first()

                if (token.isNullOrBlank()) {
                    locationText =
                        """
                        Latitud: ${location.latitude}
                        Longitud: ${location.longitude}
                        
                        No se encontró sesión activa
                        """.trimIndent()

                    return@onSuccess
                }

                val sendResult =
                    driverLocationRepository.updateLocation(
                        accessToken = token,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )

                sendResult.onSuccess {

                    locationText =
                        """
                        Latitud: ${location.latitude}
                        Longitud: ${location.longitude}
                        
                        Ubicación enviada correctamente
                        """.trimIndent()
                }

                sendResult.onFailure { error ->

                    locationText =
                        """
                        Latitud: ${location.latitude}
                        Longitud: ${location.longitude}
                        
                        Error: ${error.message}
                        """.trimIndent()
                }
            }

            locationResult.onFailure { error ->

                locationText =
                    error.message
                        ?: "Error obteniendo ubicación"
            }
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocation =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            val coarseLocation =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            if (fineLocation || coarseLocation) {

                obtainLocation()

            } else {

                locationText =
                    "Permiso de ubicación rechazado"
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        Text("Prueba de ubicación")

        Text(locationText)

        Button(
            onClick = {

                val finePermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )

                val coarsePermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )

                if (
                    finePermission ==
                    PackageManager.PERMISSION_GRANTED ||
                    coarsePermission ==
                    PackageManager.PERMISSION_GRANTED
                ) {

                    obtainLocation()

                } else {

                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        ) {
            Text("Obtener y enviar ubicación")
        }
    }
}