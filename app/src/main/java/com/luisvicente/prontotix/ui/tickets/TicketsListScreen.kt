package com.luisvicente.prontotix.ui.tickets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.service.LocationTrackingService
import kotlinx.coroutines.launch
import android.net.Uri
import android.os.Build
import android.provider.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsListScreen(
    refreshTrigger: Boolean = false,
    onRefreshHandled: () -> Unit = {},
    onTicketClick: (Long) -> Unit = {},
    onCreateTicket: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current

    val sessionManager = remember {
        SessionManager(
            context.applicationContext
        )
    }

    val coroutineScope =
        rememberCoroutineScope()

    val ticketsViewModel: TicketsViewModel =
        viewModel(
            factory =
                TicketsViewModelFactory(
                    sessionManager =
                        sessionManager
                )
        )

    val uiState by
    ticketsViewModel.uiState
        .collectAsStateWithLifecycle()

    var pendingShiftStart by remember {
        mutableStateOf(false)
    }

    val backgroundLocationSettingsLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            val backgroundGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

            if (pendingShiftStart && backgroundGranted) {

                val intent =
                    Intent(
                        context,
                        LocationTrackingService::class.java
                    )

                ContextCompat.startForegroundService(
                    context,
                    intent
                )

                pendingShiftStart = false
            }
        }

    /*
     * Si todavía no existen permisos de ubicación,
     * los solicitamos cuando inicia la jornada.
     */
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ] == true

            val coarseGranted =
                permissions[
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ] == true

            if (
                pendingShiftStart &&
                (fineGranted || coarseGranted)
            ) {

                val backgroundGranted =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }

                if (backgroundGranted) {

                    val intent =
                        Intent(
                            context,
                            LocationTrackingService::class.java
                        )

                    ContextCompat.startForegroundService(
                        context,
                        intent
                    )

                    pendingShiftStart = false

                } else {

                    val settingsIntent =
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )

                    backgroundLocationSettingsLauncher.launch(
                        settingsIntent
                    )
                }
            }
        }

    /*
     * Si el backend confirma que existe
     * una jornada activa, mantenemos
     * funcionando el servicio GPS.
     */
    LaunchedEffect(
        uiState.isShiftActive,
        uiState.shiftMessage
    ) {

        if (uiState.isShiftActive) {

            val fineGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ) ==
                        PackageManager
                            .PERMISSION_GRANTED

            val coarseGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ) ==
                        PackageManager
                            .PERMISSION_GRANTED

            val foregroundGranted =
                fineGranted || coarseGranted

            val backgroundGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

            if (
                foregroundGranted &&
                backgroundGranted
            ) {

                val intent =
                    Intent(
                        context,
                        LocationTrackingService::class.java
                    )

                ContextCompat.startForegroundService(
                    context,
                    intent
                )

            } else if (
                foregroundGranted &&
                !backgroundGranted
            ) {



                pendingShiftStart = true

                locationPermissionLauncher
                    .launch(
                        arrayOf(
                            Manifest.permission
                                .ACCESS_FINE_LOCATION,
                            Manifest.permission
                                .ACCESS_COARSE_LOCATION
                        )
                    )
            }

        } else if (
            uiState.shiftMessage ==
            "Jornada finalizada"
        ) {

            /*
             * El GPS solamente se detiene
             * cuando el usuario finaliza
             * explícitamente su jornada.
             */
            val intent =
                Intent(
                    context,
                    LocationTrackingService::class.java
                )

            context.stopService(
                intent
            )
        }
    }

    /*
     * Conservamos este comportamiento
     * aunque actualmente no mostremos
     * las diligencias.
     */
    LaunchedEffect(
        refreshTrigger
    ) {
        if (refreshTrigger) {
            ticketsViewModel.loadTickets()
            onRefreshHandled()
        }
    }

    Scaffold(
        containerColor =
            MaterialTheme
                .colorScheme
                .background,

        topBar = {

            TopAppBar(
                colors =
                    TopAppBarDefaults
                        .topAppBarColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .surface
                        ),

                title = {

                    Column {

                        Text(
                            text =
                                "ProntoTix",
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "Monitoreo de ubicación",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                },

                actions = {

                    TextButton(
                        enabled =
                            !uiState
                                .isShiftActive,

                        onClick = {

                            coroutineScope
                                .launch {

                                    sessionManager
                                        .clearSession()

                                    onLogout()
                                }
                        }
                    ) {

                        Text(
                            text =
                                if (
                                    uiState
                                        .isShiftActive
                                ) {
                                    "Jornada activa"
                                } else {
                                    "Cerrar sesión"
                                }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        paddingValues
                    )
        ) {

            /*
             * CONTROL DE JORNADA
             */
            ShiftCard(
                isActive =
                    uiState.isShiftActive,

                isLoading =
                    uiState.isShiftLoading,

                message =
                    uiState.shiftMessage,

                onStart = {
                    ticketsViewModel
                        .startShift()
                },

                onEnd = {
                    ticketsViewModel
                        .endShift()
                }
            )

            /*
             * INFORMACIÓN DE MONITOREO
             *
             * Sustituye visualmente la antigua
             * lista de diligencias.
             */
            MonitoringInfoCard(
                isActive =
                    uiState.isShiftActive
            )
        }
    }
}


/*
 * TARJETA DE JORNADA
 */
@Composable
private fun ShiftCard(
    isActive: Boolean,
    isLoading: Boolean,
    message: String?,
    onStart: () -> Unit,
    onEnd: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        elevation =
            CardDefaults
                .cardElevation(
                    defaultElevation =
                        2.dp
                ),

        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surface
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    18.dp
                )
        ) {

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),

                horizontalArrangement =
                    androidx.compose.foundation
                        .layout
                        .Arrangement
                        .SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text =
                            "Jornada",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                4.dp
                            )
                    )

                    Text(
                        text =
                            if (isActive) {
                                "● Jornada activa"
                            } else {
                                "○ Jornada no iniciada"
                            },

                        color =
                            if (isActive) {
                                MaterialTheme
                                    .colorScheme
                                    .secondary
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                            },

                        fontWeight =
                            FontWeight
                                .SemiBold
                    )
                }

                if (isActive) {

                    Surface(
                        shape =
                            RoundedCornerShape(
                                50
                            ),

                        color =
                            MaterialTheme
                                .colorScheme
                                .secondaryContainer
                    ) {

                        Text(
                            text =
                                "GPS activo",

                            modifier =
                                Modifier.padding(
                                    horizontal =
                                        10.dp,
                                    vertical =
                                        6.dp
                                ),

                            style =
                                MaterialTheme
                                    .typography
                                    .labelMedium,

                            fontWeight =
                                FontWeight
                                    .SemiBold
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Text(
                text =
                    if (isActive) {
                        "Tu ubicación se comparte durante toda tu jornada laboral."
                    } else {
                        "Inicia tu jornada para activar el monitoreo de ubicación."
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            message
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let {

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    Text(
                        text = it,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            Button(
                onClick = {

                    if (isActive) {
                        onEnd()
                    } else {
                        onStart()
                    }
                },

                enabled =
                    !isLoading,

                modifier =
                    Modifier
                        .fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        14.dp
                    )
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        strokeWidth =
                            2.dp,

                        modifier =
                            Modifier.height(
                                22.dp
                            ),

                        color =
                            MaterialTheme
                                .colorScheme
                                .onPrimary
                    )

                } else {

                    Text(
                        text =
                            if (isActive) {
                                "Finalizar jornada"
                            } else {
                                "Iniciar jornada"
                            },

                        fontWeight =
                            FontWeight
                                .SemiBold
                    )
                }
            }
        }
    }
}


/*
 * INFORMACIÓN PARA EL DILIGENCIERO
 */
@Composable
private fun MonitoringInfoCard(
    isActive: Boolean
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        16.dp,
                    vertical =
                        4.dp
                ),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surface
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    18.dp
                )
        ) {

            Text(
                text =
                    if (isActive) {
                        "Monitoreo activo"
                    } else {
                        "Monitoreo"
                    },

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            Text(
                text =
                    if (isActive) {
                        "Puedes usar tu teléfono normalmente. ProntoTix continuará compartiendo tu ubicación mientras tu jornada permanezca activa."
                    } else {
                        "Cuando inicies tu jornada, ProntoTix comenzará a compartir tu ubicación para el seguimiento de tu recorrido."
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}