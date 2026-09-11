package com.luisvicente.prontotix.ui.driverhome

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DriverHomeScreen(
    adminPassword: String,
    onAdminClick: () -> Unit = {}
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

    var currentTime by remember {
        mutableStateOf(
            LocalTime.now()
        )
    }

    var progress by remember {
        mutableFloatStateOf(
            calculateShiftProgress(
                LocalTime.now()
            )
        )
    }

    var showAdminDialog by remember {
        mutableStateOf(false)
    }

    var adminInput by remember {
        mutableStateOf("")
    }

    var adminError by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        while (true) {

            currentTime =
                LocalTime.now()

            progress =
                calculateShiftProgress(
                    currentTime
                )

            locationEnabled =
                checkLocationEnabled(
                    context
                )

            internetConnected =
                checkInternetConnected(
                    context
                )

            delay(5_000)
        }
    }

    val shiftStart =
        LocalTime.of(
            9,
            0
        )

    val shiftEnd =
        LocalTime.of(
            18,
            30
        )

    val shiftActive =
        currentTime >= shiftStart &&
                currentTime < shiftEnd

    val timeFormatter =
        DateTimeFormatter.ofPattern(
            "HH:mm"
        )

    Scaffold { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(
                        horizontal = 24.dp,
                        vertical = 28.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "ProntoTix",
                style =
                    MaterialTheme
                        .typography
                        .headlineLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "Seguimiento de jornada",
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )

            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(24.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            if (shiftActive) {
                                "● JORNADA ACTIVA"
                            } else {
                                "FUERA DE JORNADA"
                            },
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                    )

                    Text(
                        text =
                            currentTime.format(
                                timeFormatter
                            ),
                        style =
                            MaterialTheme
                                .typography
                                .displayMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(28.dp)
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "09:00"
                        )

                        Text(
                            text = "18:30"
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    LinearProgressIndicator(
                        progress = {
                            progress
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "${(progress * 100).toInt()}% de la jornada",
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Text(
                        text =
                            getShiftMessage(
                                currentTime
                            ),
                        textAlign =
                            TextAlign.Center,
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        text =
                            "Estado del dispositivo",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Text(
                        text =
                            if (locationEnabled) {
                                "📍 Ubicación: activa"
                            } else {
                                "⚠️ Ubicación: desactivada"
                            }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            if (internetConnected) {
                                "🌐 Internet: conectado"
                            } else {
                                "⚠️ Internet: sin conexión"
                            }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            if (shiftActive) {
                                "⚙️ ProntoTix está operando automáticamente"
                            } else {
                                "⚙️ Esperando próxima jornada"
                            }
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.weight(1f)
            )

            Text(
                text =
                    "ProntoTix funciona automáticamente durante el horario de trabajo.",
                textAlign =
                    TextAlign.Center,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Button(
                onClick = {
                    showAdminDialog = true
                }
            ) {

                Text(
                    text =
                        "🔒 Administrador"
                )
            }
        }
    }

    if (showAdminDialog) {

        AlertDialog(
            onDismissRequest = {

                showAdminDialog =
                    false

                adminInput =
                    ""

                adminError =
                    false
            },
            title = {

                Text(
                    text =
                        "Acceso de administrador"
                )
            },
            text = {

                Column {

                    OutlinedTextField(
                        value =
                            adminInput,
                        onValueChange = {

                            adminInput =
                                it

                            adminError =
                                false
                        },
                        label = {

                            Text(
                                text =
                                    "Contraseña"
                            )
                        },
                        visualTransformation =
                            PasswordVisualTransformation(),
                        singleLine =
                            true
                    )

                    if (adminError) {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    8.dp
                                )
                        )

                        Text(
                            text =
                                "Contraseña incorrecta",
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }
                }
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        if (
                            adminInput ==
                            adminPassword
                        ) {

                            showAdminDialog =
                                false

                            adminInput =
                                ""

                            adminError =
                                false

                            onAdminClick()

                        } else {

                            adminError =
                                true
                        }
                    }
                ) {

                    Text(
                        text = "Entrar"
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {

                        showAdminDialog =
                            false

                        adminInput =
                            ""

                        adminError =
                            false
                    }
                ) {

                    Text(
                        text = "Cancelar"
                    )
                }
            }
        )
    }
}

private fun calculateShiftProgress(
    currentTime: LocalTime
): Float {

    val start =
        LocalTime.of(
            9,
            0
        )

    val end =
        LocalTime.of(
            18,
            30
        )

    if (currentTime <= start) {
        return 0f
    }

    if (currentTime >= end) {
        return 1f
    }

    val totalMinutes =
        Duration
            .between(
                start,
                end
            )
            .toMinutes()

    val elapsedMinutes =
        Duration
            .between(
                start,
                currentTime
            )
            .toMinutes()

    return (
            elapsedMinutes.toFloat() /
                    totalMinutes.toFloat()
            )
        .coerceIn(
            0f,
            1f
        )
}

private fun getShiftMessage(
    currentTime: LocalTime
): String {

    val start =
        LocalTime.of(
            9,
            0
        )

    val end =
        LocalTime.of(
            18,
            30
        )

    return when {

        currentTime < start ->
            "La jornada iniciará automáticamente a las 09:00"

        currentTime >= end ->
            "La jornada ha finalizado"

        else ->
            "ProntoTix está trabajando en segundo plano"
    }
}

private fun checkLocationEnabled(
    context: Context
): Boolean {

    val locationManager =
        context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

    return locationManager.isLocationEnabled
}

private fun checkInternetConnected(
    context: Context
): Boolean {

    val connectivityManager =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    val network =
        connectivityManager.activeNetwork
            ?: return false

    val capabilities =
        connectivityManager
            .getNetworkCapabilities(
                network
            )
            ?: return false

    return capabilities.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET
    ) &&
            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )
}