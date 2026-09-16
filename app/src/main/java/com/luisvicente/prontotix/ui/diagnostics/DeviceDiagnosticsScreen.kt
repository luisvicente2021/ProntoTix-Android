package com.luisvicente.prontotix.ui.diagnostics

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luisvicente.prontotix.BuildConfig
import com.luisvicente.prontotix.admin.DevicePolicyManagerHelper
import com.luisvicente.prontotix.admin.MaintenanceModeManager
import com.luisvicente.prontotix.service.TrackingStatusManager
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable

private val ProntoNavy =
    Color(0xFF0B1930)

private val ProntoGreen =
    Color(0xFF10B981)

private val ProntoDanger =
    Color(0xFFE05252)

private val ProntoBackground =
    Color(0xFFF4F7FB)

private val ProntoText =
    Color(0xFF12233D)

private val ProntoMuted =
    Color(0xFF64748B)

@Composable
fun DeviceDiagnosticsScreen(
    onBack: () -> Unit
) {

    val context =
        LocalContext.current

    var locationEnabled by remember {
        mutableStateOf(
            checkLocationEnabled(context)
        )
    }

    var internetConnected by remember {
        mutableStateOf(
            checkInternetConnected(context)
        )
    }

    var trackingRunning by remember {
        mutableStateOf(
            TrackingStatusManager.isRunning
        )
    }

    val isDeviceOwner =
        remember {
            DevicePolicyManagerHelper
                .isDeviceOwner(context)
        }

    var maintenanceMode by remember {
        mutableStateOf(
            MaintenanceModeManager
                .isEnabled(context)
        )
    }

    /*
     * Actualizamos el diagnóstico
     * automáticamente.
     */
    LaunchedEffect(Unit) {

        while (true) {

            locationEnabled =
                checkLocationEnabled(context)

            internetConnected =
                checkInternetConnected(context)

            trackingRunning =
                TrackingStatusManager.isRunning

            maintenanceMode =
                MaintenanceModeManager
                    .isEnabled(context)

            delay(2_000)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    ProntoBackground
                )
    ) {

        /*
         * HEADER
         */
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        ProntoNavy
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 18.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "‹",
                color = Color.White,
                style =
                    MaterialTheme
                        .typography
                        .headlineMedium,
                modifier =
                    Modifier
                        .clickable {
                            onBack()
                        }
                        .padding(
                            end = 14.dp
                        )
            )

            Column {

                Text(
                    text =
                        "Diagnóstico del dispositivo",
                    color =
                        Color.White,
                    fontWeight =
                        FontWeight.Bold,
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge
                )

                Text(
                    text = "ProntoTix",
                    color =
                        Color(0xFF69D8E8),
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
        ) {

            Text(
                text = "ESTADO GENERAL",
                color = ProntoMuted,
                fontWeight =
                    FontWeight.Bold,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation =
                            2.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            16.dp
                        )
                ) {

                    DiagnosticRow(
                        title = "Ubicación",
                        active = locationEnabled,
                        activeText = "Activada",
                        inactiveText = "Desactivada"
                    )

                    DiagnosticRow(
                        title = "Internet",
                        active = internetConnected,
                        activeText = "Conectado",
                        inactiveText = "Sin conexión"
                    )

                    DiagnosticRow(
                        title = "Tracking",
                        active = trackingRunning,
                        activeText = "Ejecutándose",
                        inactiveText = "Detenido"
                    )

                    DiagnosticRow(
                        title = "Device Owner",
                        active = isDeviceOwner,
                        activeText = "Activo",
                        inactiveText = "No configurado"
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )

            Text(
                text = "APLICACIÓN",
                color = ProntoMuted,
                fontWeight =
                    FontWeight.Bold,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation =
                            2.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            16.dp
                        )
                ) {

                    InformationRow(
                        title = "Aplicación",
                        value = "ProntoTix"
                    )

                    InformationRow(
                        title = "Versión",
                        value =
                            BuildConfig.VERSION_NAME
                    )

                    InformationRow(
                        title =
                            "Modo mantenimiento",
                        value =
                            if (maintenanceMode) {
                                "Activado"
                            } else {
                                "Desactivado"
                            }
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Text(
                text =
                    "Los estados se actualizan automáticamente.",
                color =
                    ProntoMuted,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )
        }
    }
}

@Composable
private fun DiagnosticRow(
    title: String,
    active: Boolean,
    activeText: String,
    inactiveText: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = ProntoText,
            fontWeight =
                FontWeight.Medium
        )

        Text(
            text =
                if (active) {
                    "● $activeText"
                } else {
                    "● $inactiveText"
                },
            color =
                if (active) {
                    ProntoGreen
                } else {
                    ProntoDanger
                },
            fontWeight =
                FontWeight.Medium
        )
    }
}

@Composable
private fun InformationRow(
    title: String,
    value: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            color = ProntoText
        )

        Text(
            text = value,
            color = ProntoMuted,
            fontWeight =
                FontWeight.Medium
        )
    }
}

private fun checkLocationEnabled(
    context: Context
): Boolean {

    val locationManager =
        context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

    return locationManager
        .isLocationEnabled
}

private fun checkInternetConnected(
    context: Context
): Boolean {

    val connectivityManager =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    val network =
        connectivityManager
            .activeNetwork
            ?: return false

    val capabilities =
        connectivityManager
            .getNetworkCapabilities(
                network
            )
            ?: return false

    return capabilities.hasCapability(
        NetworkCapabilities
            .NET_CAPABILITY_INTERNET
    ) &&
            capabilities.hasCapability(
                NetworkCapabilities
                    .NET_CAPABILITY_VALIDATED
            )
}
