package com.luisvicente.prontotix.ui.tickets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.luisvicente.prontotix.data.model.Ticket
import com.luisvicente.prontotix.service.LocationTrackingService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsListScreen(
    refreshTrigger: Boolean = false,
    onRefreshHandled: () -> Unit = {},
    onTicketClick: (Long) -> Unit = {},
    onCreateTicket: () -> Unit = {}
) {
    val context = LocalContext.current

    val ticketsViewModel: TicketsViewModel =
        viewModel(
            factory =
                TicketsViewModelFactory(
                    sessionManager =
                        SessionManager(
                            context.applicationContext
                        )
                )
        )

    val uiState by
    ticketsViewModel.uiState
        .collectAsStateWithLifecycle()

    var pendingShiftStart by remember {
        mutableStateOf(false)
    }

    /*
     * Si el usuario todavía no dio permisos de ubicación,
     * los pedimos cuando inicia su jornada.
     */
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            val coarseGranted =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            if (
                pendingShiftStart &&
                (fineGranted || coarseGranted)
            ) {
                val intent = Intent(
                    context,
                    LocationTrackingService::class.java
                )

                ContextCompat.startForegroundService(
                    context,
                    intent
                )
            }

            pendingShiftStart = false
        }

    /*
     * Cuando el backend nos confirma que existe
     * una jornada activa, iniciamos el servicio GPS.
     *
     * IMPORTANTE:
     * ya no depende de una diligencia "En Proceso".
     */
    LaunchedEffect(
        uiState.isShiftActive,
        uiState.shiftMessage
    ) {
        if (uiState.isShiftActive) {

            val fineGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) ==
                        PackageManager.PERMISSION_GRANTED

            val coarseGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) ==
                        PackageManager.PERMISSION_GRANTED

            if (
                fineGranted ||
                coarseGranted
            ) {
                val intent = Intent(
                    context,
                    LocationTrackingService::class.java
                )

                ContextCompat.startForegroundService(
                    context,
                    intent
                )

            } else {

                pendingShiftStart = true

                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }

        } else if (
            uiState.shiftMessage ==
            "Jornada finalizada"
        ) {
            /*
             * Solamente detenemos el GPS cuando
             * el diligenciero finaliza su jornada.
             */
            val intent = Intent(
                context,
                LocationTrackingService::class.java
            )

            context.stopService(intent)
        }
    }

    /*
     * Cuando regresamos del detalle de una diligencia,
     * refrescamos el listado.
     */
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            ticketsViewModel.loadTickets()
            onRefreshHandled()
        }
    }

    Scaffold(
        containerColor =
            MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    ),
                title = {
                    Column {
                        Text(
                            text = "Mis diligencias",
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "${uiState.tickets.size} asignaciones",
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
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            /*
             * TARJETA DE JORNADA
             */
            ShiftCard(
                isActive =
                    uiState.isShiftActive,
                isLoading =
                    uiState.isShiftLoading,
                message =
                    uiState.shiftMessage,
                onStart = {
                    ticketsViewModel.startShift()
                },
                onEnd = {
                    ticketsViewModel.endShift()
                }
            )

            /*
             * CONTENIDO DE DILIGENCIAS
             */
            Box(
                modifier =
                    Modifier.weight(1f)
            ) {

                when {

                    uiState.isLoading -> {
                        LoadingContent(
                            modifier =
                                Modifier.fillMaxSize()
                        )
                    }

                    uiState.errorMessage != null -> {
                        ErrorContent(
                            message =
                                uiState
                                    .errorMessage
                                    .orEmpty(),
                            onRetry =
                                ticketsViewModel::loadTickets,
                            modifier =
                                Modifier.fillMaxSize()
                        )
                    }

                    uiState.tickets.isEmpty() -> {
                        EmptyContent(
                            modifier =
                                Modifier.fillMaxSize()
                        )
                    }

                    else -> {

                        LazyColumn(
                            modifier =
                                Modifier.fillMaxSize(),
                            contentPadding =
                                PaddingValues(
                                    horizontal =
                                        16.dp,
                                    vertical =
                                        8.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    14.dp
                                )
                        ) {

                            items(
                                items =
                                    uiState.tickets,
                                key = { ticket ->
                                    ticket.id
                                        ?: ticket
                                            .hashCode()
                                            .toLong()
                                }
                            ) { ticket ->

                                DiligenceCard(
                                    ticket =
                                        ticket,
                                    onClick = {
                                        ticket.id
                                            ?.let(
                                                onTicketClick
                                            )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/*
 * Tarjeta que controla la jornada laboral.
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        shape =
            RoundedCornerShape(18.dp),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "Jornada",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
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
                            FontWeight.SemiBold
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
                            text = "GPS activo",
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
                                FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
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
                            Modifier.height(8.dp)
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
                    Modifier.height(16.dp)
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
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(
                        14.dp
                    )
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
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
                            FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DiligenceCard(
    ticket: Ticket,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),
        shape =
            RoundedCornerShape(
                18.dp
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
        colors =
            CardDefaults.cardColors(
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
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "Diligencia #${ticket.id ?: "-"}",
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                StatusBadge(
                    status =
                        ticket.status
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Text(
                text =
                    ticket.title
                        ?: "Diligencia sin título",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            ticket.clientName
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { client ->

                    Spacer(
                        modifier =
                            Modifier.height(
                                6.dp
                            )
                    )

                    Text(
                        text = client,
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

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            "Prioridad",
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text =
                            ticket.priority
                                ?: "Sin prioridad",
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                ticket.openedAt
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { date ->

                        Column(
                            modifier =
                                Modifier.weight(
                                    1f
                                ),
                            horizontalAlignment =
                                Alignment.End
                        ) {

                            Text(
                                text = "Fecha",
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )

                            Text(
                                text =
                                    formatTicketDate(
                                        date
                                    ),
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: String?
) {
    val normalized =
        status
            ?.trim()
            ?.lowercase()
            .orEmpty()

    val label =
        when (normalized) {

            "abierta",
            "abierto" ->
                "Pendiente"

            "en proceso" ->
                "En progreso"

            "cerrada",
            "cerrado",
            "terminada" ->
                "Terminada"

            else ->
                status
                    ?: "Sin estado"
        }

    val background =
        when (normalized) {

            "abierta",
            "abierto" ->
                MaterialTheme
                    .colorScheme
                    .errorContainer

            "en proceso" ->
                MaterialTheme
                    .colorScheme
                    .tertiaryContainer

            "cerrada",
            "cerrado",
            "terminada" ->
                MaterialTheme
                    .colorScheme
                    .secondaryContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        }

    Surface(
        shape =
            RoundedCornerShape(
                50
            ),
        color = background
    ) {

        Text(
            text = label,
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
                FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier =
        Modifier
) {
    Box(
        modifier =
            modifier,
        contentAlignment =
            Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier =
        Modifier
) {
    Column(
        modifier =
            modifier.padding(
                24.dp
            ),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                "No pudimos cargar tus diligencias",
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
            text = message,
            color =
                MaterialTheme
                    .colorScheme
                    .error
        )

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        Button(
            onClick =
                onRetry
        ) {
            Text(
                "Reintentar"
            )
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier =
        Modifier
) {
    Column(
        modifier =
            modifier.padding(
                24.dp
            ),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                "No tienes diligencias asignadas",
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
                "Cuando Compras te asigne una diligencia aparecerá aquí.",
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

private fun formatTicketDate(
    date: String
): String {

    return try {

        val instant =
            java.time.Instant
                .parse(date)

        val formatter =
            java.time.format
                .DateTimeFormatter
                .ofPattern(
                    "dd MMM yyyy"
                )
                .withLocale(
                    java.util.Locale(
                        "es",
                        "MX"
                    )
                )
                .withZone(
                    java.time.ZoneId
                        .systemDefault()
                )

        formatter.format(
            instant
        )

    } catch (
        _: Exception
    ) {
        date
    }
}