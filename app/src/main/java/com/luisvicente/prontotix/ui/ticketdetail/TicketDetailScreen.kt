package com.luisvicente.prontotix.ui.ticketdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.model.Ticket
import android.content.Intent
import androidx.core.content.ContextCompat
import com.luisvicente.prontotix.service.LocationTrackingService
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

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

    val detailViewModel: TicketDetailViewModel = viewModel(
        key = "ticket-$ticketId",
        factory = TicketDetailViewModelFactory(
            ticketId = ticketId,
            sessionManager = SessionManager(
                context.applicationContext
            )
        )
    )

    val uiState by
    detailViewModel.uiState.collectAsStateWithLifecycle()

    var showStatusDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(uiState.lastUpdatedStatus) {

        when (uiState.lastUpdatedStatus) {

            "En Proceso" -> {

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

    LaunchedEffect(uiState.lastUpdatedStatus) {

        when (uiState.lastUpdatedStatus) {

            "En Proceso" -> {

                val intent = Intent(
                    context,
                    LocationTrackingService::class.java
                )

                ContextCompat.startForegroundService(
                    context,
                    intent
                )
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle de la asignación")
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
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
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
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Button(
                        onClick = detailViewModel::loadTicket
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            uiState.ticket != null -> {
                TicketDetailContent(
                    ticket = uiState.ticket!!,
                    isUpdatingStatus = uiState.isUpdatingStatus,
                    successMessage = uiState.successMessage,
                    errorMessage = uiState.errorMessage,
                    onChangeStatus = {
                        showStatusDialog = true
                    },
                    onOpenDeliveryReport = onOpenDeliveryReport,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }

    if (showStatusDialog) {
        StatusDialog(
            currentStatus = uiState.ticket?.status,
            onDismiss = {
                showStatusDialog = false
            },
            onStatusSelected = { status ->
                showStatusDialog = false
                detailViewModel.updateStatus(status)
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
){
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = ticket.title ?: "Asignación sin título",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailCard(
            title = "Información general"
        ) {
            DetailField(
                label = "Folio",
                value = ticket.id?.toString()
            )

            DetailField(
                label = "Residencial",
                value = ticket.clientName
            )

            DetailField(
                label = "Estado",
                value = statusLabel(
                    ticket.status ?: "Sin estado"
                )
            )

            DetailField(
                label = "Prioridad",
                value = ticket.priority
            )

            DetailField(
                label = "Fecha de apertura",
                value = formatTicketDate(
                    ticket.openedAt
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DetailCard(
            title = "Reporte"
        ) {
            DetailField(
                label = "Reportó",
                value = ticket.reportedBy
            )

            DetailField(
                label = "Departamento",
                value = ticket.department
            )

            DetailField(
                label = "Puesto",
                value = ticket.jobTitle
            )

            DetailField(
                label = "Teléfono",
                value = ticket.reporterPhone
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DetailCard(
            title = "Descripción"
        ) {
            Text(
                text = ticket.description
                    ?: "Sin descripción"
            )
        }

        ticket.closedAt?.let { closedAt ->
            Spacer(modifier = Modifier.height(16.dp))

            DetailCard(
                title = "Cierre"
            ) {
                DetailField(
                    label = "Fecha de cierre",
                    value = formatTicketDate(closedAt)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        successMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onChangeStatus,
            enabled = !isUpdatingStatus,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isUpdatingStatus) {
                CircularProgressIndicator()
            } else {
                Text("Cambiar estado")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onOpenDeliveryReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar reporte de entrega")
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String?
) {
    if (!value.isNullOrBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = "$label: ",
                fontWeight = FontWeight.SemiBold
            )

            Text(text = value)
        }
    }
}

@Composable
private fun StatusDialog(
    currentStatus: String?,
    onDismiss: () -> Unit,
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf(
        "Abierta",
        "En Proceso",
        "Cerrada"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cambiar estado")
        },
        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { status ->
                    OutlinedButton(
                        onClick = {
                            onStatusSelected(status)
                        },
                        enabled = status != currentStatus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusLabel(status)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

private fun statusLabel(status: String): String {
    return when (status) {
        "Abierta" -> "Abierto"
        "En Proceso" -> "En progreso"
        "Cerrada" -> "Terminada"
        else -> status
    }
}

private fun formatTicketDate(
    date: String?
): String {
    if (date.isNullOrBlank()) {
        return "Sin fecha"
    }

    return try {
        val instant = java.time.Instant.parse(date)

        val formatter =
            java.time.format.DateTimeFormatter
                .ofPattern("dd MMM yyyy, HH:mm")
                .withLocale(
                    java.util.Locale("es", "MX")
                )
                .withZone(
                    java.time.ZoneId.systemDefault()
                )

        formatter.format(instant)
    } catch (_: Exception) {
        date
    }
}