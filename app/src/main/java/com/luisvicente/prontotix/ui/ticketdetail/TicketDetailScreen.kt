package com.luisvicente.prontotix.ui.ticketdetail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
fun TicketDetailScreen(
    ticketId: Long,
    onBack: () -> Unit,
    onStatusUpdated: () -> Unit,
    onOpenDeliveryReport: () -> Unit
) {
    val context = LocalContext.current

    var pendingTrackingStart by remember {
        mutableStateOf(false)
    }

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
                pendingTrackingStart &&
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

            pendingTrackingStart = false
        }

    val detailViewModel: TicketDetailViewModel =
        viewModel(
            key = "ticket-$ticketId",
            factory = TicketDetailViewModelFactory(
                ticketId = ticketId,
                sessionManager = SessionManager(
                    context.applicationContext
                )
            )
        )

    val uiState by
    detailViewModel.uiState
        .collectAsStateWithLifecycle()

    var showStatusDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        uiState.lastUpdatedStatus
    ) {
        when (
            uiState.lastUpdatedStatus
        ) {
            "En Proceso" -> {

                val fineGranted =
                    ContextCompat
                        .checkSelfPermission(
                            context,
                            Manifest.permission
                                .ACCESS_FINE_LOCATION
                        ) ==
                            PackageManager
                                .PERMISSION_GRANTED

                val coarseGranted =
                    ContextCompat
                        .checkSelfPermission(
                            context,
                            Manifest.permission
                                .ACCESS_COARSE_LOCATION
                        ) ==
                            PackageManager
                                .PERMISSION_GRANTED

                if (
                    fineGranted ||
                    coarseGranted
                ) {
                    val intent = Intent(
                        context,
                        LocationTrackingService::class.java
                    )

                    ContextCompat
                        .startForegroundService(
                            context,
                            intent
                        )
                } else {
                    pendingTrackingStart = true

                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

            "Cerrada" -> {
                val intent = Intent(
                    context,
                    LocationTrackingService::class.java
                )

                context.stopService(intent)
            }
        }
    }

    LaunchedEffect(
        uiState.successMessage
    ) {
        if (
            uiState.successMessage != null
        ) {
            onStatusUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detalle de diligencia",
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "Folio #$ticketId",
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
                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text("Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->

        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement =
                        Arrangement.Center,
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null &&
                    uiState.ticket == null -> {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    verticalArrangement =
                        Arrangement.Center,
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        text =
                            "No pudimos cargar la diligencia",
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            uiState.errorMessage
                                .orEmpty(),
                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )

                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                    )

                    Button(
                        onClick =
                            detailViewModel::loadTicket
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            uiState.ticket != null -> {
                TicketDetailContent(
                    ticket =
                        uiState.ticket!!,
                    isUpdatingStatus =
                        uiState
                            .isUpdatingStatus,
                    successMessage =
                        uiState
                            .successMessage,
                    errorMessage =
                        uiState
                            .errorMessage,
                    onChangeStatus = {
                        showStatusDialog = true
                    },
                    onOpenDeliveryReport =
                        onOpenDeliveryReport,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            paddingValues
                        )
                )
            }
        }
    }

    if (showStatusDialog) {
        StatusDialog(
            currentStatus =
                uiState.ticket?.status,
            onDismiss = {
                showStatusDialog = false
            },
            onStatusSelected = { status ->
                showStatusDialog = false

                detailViewModel
                    .updateStatus(status)
            }
        )
    }
}

@Composable
private fun TicketDetailContent(
    ticket: Ticket,
    isUpdatingStatus: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onChangeStatus: () -> Unit,
    onOpenDeliveryReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        ticket.title
                            ?: "Diligencia sin título",
                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,
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
                                    4.dp
                                )
                        )

                        Text(
                            text = client,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
            }

            StatusBadge(
                status = ticket.status
            )
        }

        if (
            ticket.status
                ?.equals(
                    "En Proceso",
                    ignoreCase = true
                ) == true
        ) {
            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            TrackingCard()
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        DetailCard(
            title = "Información general"
        ) {
            DetailField(
                label = "Folio",
                value =
                    ticket.id
                        ?.toString()
            )

            DetailField(
                label = "Estado",
                value =
                    statusLabel(
                        ticket.status
                            ?: "Sin estado"
                    )
            )

            DetailField(
                label = "Prioridad",
                value =
                    ticket.priority
            )

            DetailField(
                label = "Fecha",
                value =
                    formatTicketDate(
                        ticket.openedAt
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.height(14.dp)
        )

        DetailCard(
            title = "Solicitud"
        ) {
            DetailField(
                label = "Solicitante",
                value =
                    ticket.reportedBy
            )

            DetailField(
                label = "Área",
                value =
                    ticket.department
            )

            DetailField(
                label = "Puesto",
                value =
                    ticket.jobTitle
            )

            DetailField(
                label = "Teléfono",
                value =
                    ticket.reporterPhone
            )
        }

        Spacer(
            modifier =
                Modifier.height(14.dp)
        )

        DetailCard(
            title = "Descripción"
        ) {
            Text(
                text =
                    ticket.description
                        ?: "Sin descripción",
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )
        }

        ticket.closedAt?.let {
                closedAt ->

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            DetailCard(
                title = "Cierre"
            ) {
                DetailField(
                    label =
                        "Fecha de cierre",
                    value =
                        formatTicketDate(
                            closedAt
                        )
                )
            }
        }

        successMessage?.let {
                message ->

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Surface(
                shape =
                    RoundedCornerShape(
                        14.dp
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    modifier =
                        Modifier.padding(
                            14.dp
                        ),
                    color =
                        MaterialTheme
                            .colorScheme
                            .onPrimaryContainer,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }

        errorMessage?.let {
                message ->

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Surface(
                shape =
                    RoundedCornerShape(
                        14.dp
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .errorContainer,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    modifier =
                        Modifier.padding(
                            14.dp
                        ),
                    color =
                        MaterialTheme
                            .colorScheme
                            .onErrorContainer
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Button(
            onClick =
                onChangeStatus,
            enabled =
                !isUpdatingStatus,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape =
                RoundedCornerShape(
                    14.dp
                )
        ) {
            if (
                isUpdatingStatus
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onPrimary
                )
            } else {
                Text(
                    text =
                        "Cambiar estado",
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedButton(
            onClick =
                onOpenDeliveryReport,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape =
                RoundedCornerShape(
                    14.dp
                )
        ) {
            Text(
                text =
                    "Reporte de entrega"
            )
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )
    }
}

@Composable
private fun TrackingCard() {
    Surface(
        shape =
            RoundedCornerShape(16.dp),
        color =
            MaterialTheme
                .colorScheme
                .primaryContainer,
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {
            Text(
                text =
                    "📍 Seguimiento activo",
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    "Tu ubicación se está compartiendo mientras esta diligencia está en progreso.",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            )
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(18.dp),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            content()
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String?
) {
    if (
        !value.isNullOrBlank()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 5.dp
                )
        ) {
            Text(
                text = "$label:",
                fontWeight =
                    FontWeight.SemiBold,
                modifier =
                    Modifier.weight(
                        0.42f
                    )
            )

            Text(
                text = value,
                modifier =
                    Modifier.weight(
                        0.58f
                    )
            )
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
                    .primaryContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        }

    Surface(
        shape =
            RoundedCornerShape(50),
        color = background
    ) {
        Text(
            text = label,
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
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
private fun StatusDialog(
    currentStatus: String?,
    onDismiss: () -> Unit,
    onStatusSelected:
        (String) -> Unit
) {
    val statuses = listOf(
        "Abierta",
        "En Proceso",
        "Cerrada"
    )

    AlertDialog(
        onDismissRequest =
            onDismiss,
        title = {
            Text(
                text =
                    "Actualizar diligencia"
            )
        },
        text = {
            Column(
                verticalArrangement =
                    Arrangement
                        .spacedBy(8.dp)
            ) {
                Text(
                    text =
                        "Selecciona el nuevo estado."
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                statuses.forEach {
                        status ->

                    OutlinedButton(
                        onClick = {
                            onStatusSelected(
                                status
                            )
                        },
                        enabled =
                            status !=
                                    currentStatus,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                    ) {
                        Text(
                            text =
                                statusLabel(
                                    status
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick =
                    onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

private fun statusLabel(
    status: String
): String {
    return when (status) {
        "Abierta" ->
            "Pendiente"

        "En Proceso" ->
            "En progreso"

        "Cerrada" ->
            "Terminada"

        else ->
            status
    }
}

private fun formatTicketDate(
    date: String?
): String {
    if (
        date.isNullOrBlank()
    ) {
        return "Sin fecha"
    }

    return try {
        val instant =
            java.time.Instant
                .parse(date)

        val formatter =
            java.time.format
                .DateTimeFormatter
                .ofPattern(
                    "dd MMM yyyy · HH:mm"
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

        formatter.format(instant)

    } catch (
        _: Exception
    ) {
        date
    }
}