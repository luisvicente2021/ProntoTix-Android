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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.model.Ticket
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle del ticket")
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

            uiState.errorMessage != null -> {
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
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
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
                    ticket = uiState.ticket!!,
                    isUpdatingStatus = uiState.isUpdatingStatus,
                    successMessage = uiState.successMessage,
                    onChangeStatus = {
                        showStatusDialog = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun TicketDetailContent(
    ticket: Ticket,
    isUpdatingStatus: Boolean,
    successMessage: String?,
    onChangeStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = ticket.title ?: "Ticket sin título",
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

            Spacer(modifier = Modifier.height(16.dp))
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
            onClick = {
                // Después agregaremos el reporte de entrega.
            },
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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