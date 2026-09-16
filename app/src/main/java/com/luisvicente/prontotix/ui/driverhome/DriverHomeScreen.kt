package com.luisvicente.prontotix.ui.driverhome

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luisvicente.prontotix.R
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation

private val ProntoNavy = Color(0xFF0B1930)
private val ProntoBlue = Color(0xFF1677FF)
private val ProntoGreen = Color(0xFF10B981)
private val ProntoBackground = Color(0xFFF4F7FB)
private val ProntoText = Color(0xFF12233D)
private val ProntoMuted = Color(0xFF64748B)
private val ProntoDanger = Color(0xFFE05252)

@Composable
fun DriverHomeScreen(
    adminPassword: String,
    onAdminClick: () -> Unit = {}
) {

    val context = LocalContext.current

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

    var adminPasswordVisible by remember {
        mutableStateOf(false)
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
        LocalTime.of(9, 0)

    val shiftEnd =
        LocalTime.of(18, 30)

    Scaffold(
        containerColor = ProntoBackground
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            /*
             * HEADER
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ProntoNavy)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 11.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Image(
                    painter =
                        painterResource(
                            R.drawable.prontotix_logo
                        ),
                    contentDescription =
                        "Logo ProntoTix",
                    modifier =
                        Modifier.size(40.dp),
                    contentScale =
                        ContentScale.Fit
                )

                Spacer(
                    modifier =
                        Modifier.width(9.dp)
                )

                Text(
                    text = "Pronto",
                    color = Color.White,
                    fontWeight =
                        FontWeight.Bold,
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Tix",
                    color =
                        Color(0xFF12C9DC),
                    fontWeight =
                        FontWeight.Bold,
                    style =
                        MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        showAdminDialog = true
                    }
                ) {

                    Text(
                        text = "⚙",
                        color = Color.White,
                        style =
                            MaterialTheme.typography.titleLarge
                    )
                }
            }

            /*
             * CONTENIDO
             */
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        top = 12.dp,
                        bottom = 4.dp
                    ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                /*
                 * DISPOSITIVO ACTIVO
                 */
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
                            defaultElevation = 2.dp
                        )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 15.dp,
                                vertical = 12.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (
                                        locationEnabled &&
                                        internetConnected
                                    ) {
                                        Color(0xFFE4F8F0)
                                    } else {
                                        Color(0xFFFFEEEE)
                                    }
                                ),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    if (
                                        locationEnabled &&
                                        internetConnected
                                    ) {
                                        "●"
                                    } else {
                                        "!"
                                    },
                                color =
                                    if (
                                        locationEnabled &&
                                        internetConnected
                                    ) {
                                        ProntoGreen
                                    } else {
                                        ProntoDanger
                                    },
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.width(11.dp)
                        )

                        Column {

                            Text(
                                text =
                                    if (
                                        locationEnabled &&
                                        internetConnected
                                    ) {
                                        "Dispositivo activo"
                                    } else {
                                        "Revisar dispositivo"
                                    },
                                color =
                                    ProntoText,
                                fontWeight =
                                    FontWeight.Bold,
                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text =
                                    if (
                                        locationEnabled &&
                                        internetConnected
                                    ) {
                                        "Todo en orden"
                                    } else {
                                        "Revisa la conexión del dispositivo"
                                    },
                                color =
                                    ProntoMuted,
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                /*
                 * UBICACIÓN + INTERNET
                 */
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    StatusCard(
                        modifier =
                            Modifier.weight(1f),
                        symbol = "●",
                        title = "Ubicación",
                        value =
                            if (locationEnabled) {
                                "Activa"
                            } else {
                                "Desactivada"
                            },
                        available =
                            locationEnabled
                    )

                    StatusCard(
                        modifier =
                            Modifier.weight(1f),
                        symbol = "◉",
                        title = "Internet",
                        value =
                            if (internetConnected) {
                                "Con conexión"
                            } else {
                                "Sin conexión"
                            },
                        available =
                            internetConnected
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(13.dp)
                )

                /*
                 * JORNADA
                 */
                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(18.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color.White
                        ),
                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 15.dp,
                                    end = 15.dp,
                                    top = 12.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text = "▣",
                                color =
                                    ProntoBlue,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(7.dp)
                            )

                            Text(
                                text =
                                    "Tu jornada de hoy",
                                color =
                                    ProntoText,
                                fontWeight =
                                    FontWeight.Bold,
                                style =
                                    MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        ShiftProgressCircle(
                            progress = progress,
                            currentTime = currentTime
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(
                            text =
                                when {

                                    currentTime <
                                            shiftStart ->

                                        "ProntoTix iniciará automáticamente tu registro"

                                    currentTime >=
                                            shiftEnd ->

                                        "Gracias por tu trabajo de hoy"

                                    else ->

                                        "Tu actividad se está registrando"
                                },
                            modifier =
                                Modifier.padding(
                                    start = 24.dp,
                                    end = 24.dp,
                                    bottom = 13.dp
                                ),
                            textAlign =
                                TextAlign.Center,
                            color =
                                ProntoMuted,
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }

                /*
                 * ESTE SPACER ES EL QUE EMPUJA
                 * PAISAJE + MOTO HACIA ABAJO
                 */
                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                /*
                 * PAISAJE INFERIOR
                 */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp),
                    contentAlignment =
                        Alignment.BottomCenter
                ) {

                    Image(
                        painter =
                            painterResource(
                                R.drawable.prontotix_landscape
                            ),
                        contentDescription =
                            "Paisaje",
                        modifier =
                            Modifier.fillMaxSize(),
                        contentScale =
                            ContentScale.FillBounds
                    )

                    /*
                     * MOTO PEQUEÑA SUPERPUESTA
                     */
                    Image(
                        painter =
                            painterResource(
                                R.drawable.prontotix_moto
                            ),
                        contentDescription =
                            "Moto ProntoTix",
                        modifier = Modifier
                            .width(82.dp)
                            .height(62.dp)
                            .offset(
                                y = (-4).dp
                            ),
                        contentScale =
                            ContentScale.Fit
                    )
                }

                Text(
                    text =
                        "Tu trabajo en movimiento",
                    modifier =
                        Modifier.padding(
                            top = 3.dp,
                            bottom = 2.dp
                        ),
                    color =
                        Color(0xFF315C9C),
                    fontWeight =
                        FontWeight.Medium,
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    /*
     * ADMIN
     */
    if (showAdminDialog) {

        AlertDialog(
            onDismissRequest = {

                showAdminDialog = false
                adminInput = ""
                adminError = false
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
                            adminInput = it
                            adminError = false
                        },
                        label = {
                            Text(
                                text = "Contraseña"
                            )
                        },
                        visualTransformation =
                            if (adminPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    adminPasswordVisible =
                                        !adminPasswordVisible
                                }
                            ) {
                                Icon(
                                    imageVector =
                                        if (adminPasswordVisible) {
                                            Icons.Default.VisibilityOff
                                        } else {
                                            Icons.Default.Visibility
                                        },
                                    contentDescription =
                                        if (adminPasswordVisible) {
                                            "Ocultar contraseña"
                                        } else {
                                            "Mostrar contraseña"
                                        }
                                )
                            }
                        },
                        singleLine = true
                    )

                    if (adminError) {

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
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

                            showAdminDialog = false
                            adminInput = ""
                            adminError = false

                            onAdminClick()

                        } else {

                            adminError = true
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

                        showAdminDialog = false
                        adminInput = ""
                        adminError = false
                        adminPasswordVisible = false
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

@Composable
private fun StatusCard(
    modifier: Modifier = Modifier,
    symbol: String,
    title: String,
    value: String,
    available: Boolean
) {

    Card(
        modifier =
            modifier,
        shape =
            RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 12.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    symbol,
                color =
                    if (available) {
                        ProntoBlue
                    } else {
                        ProntoDanger
                    },
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )

            Text(
                text =
                    title,
                color =
                    ProntoText,
                fontWeight =
                    FontWeight.SemiBold,
                style =
                    MaterialTheme.typography.bodySmall
            )

            Text(
                text =
                    value,
                color =
                    if (available) {
                        ProntoGreen
                    } else {
                        ProntoDanger
                    },
                fontWeight =
                    FontWeight.SemiBold,
                style =
                    MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ShiftProgressCircle(
    progress: Float,
    currentTime: LocalTime
) {

    val start =
        LocalTime.of(9, 0)

    val end =
        LocalTime.of(18, 30)

    val completed =
        currentTime >= end

    val active =
        currentTime >= start &&
                currentTime < end

    Box(
        modifier =
            Modifier.size(175.dp),
        contentAlignment =
            Alignment.Center
    ) {

        CircularProgressIndicator(
            progress = {
                progress
            },
            modifier =
                Modifier.fillMaxSize(),
            strokeWidth =
                12.dp,
            color =
                if (completed) {
                    ProntoGreen
                } else {
                    ProntoBlue
                },
            trackColor =
                Color(0xFFE1EAF5)
        )

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    when {
                        completed -> "✓"
                        active -> "■"
                        else -> "○"
                    },
                color =
                    when {
                        completed ->
                            ProntoGreen

                        active ->
                            Color(0xFF174C8F)

                        else ->
                            Color(0xFF174C8F)
                    },
                style =
                    MaterialTheme.typography.headlineMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    when {

                        completed ->
                            "JORNADA\nFINALIZADA"

                        active ->
                            "EN JORNADA"

                        else ->
                            "PRÓXIMA\nJORNADA"
                    },
                textAlign =
                    TextAlign.Center,
                color =
                    ProntoText,
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

private fun calculateShiftProgress(
    currentTime: LocalTime
): Float {

    val start =
        LocalTime.of(9, 0)

    val end =
        LocalTime.of(18, 30)

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