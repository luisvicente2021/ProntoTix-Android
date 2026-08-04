package com.luisvicente.prontotix.ui.tickets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsListScreen(
    onTicketClick: (Long) -> Unit = {},
    onCreateTicket: () -> Unit = {}
) {
    val context = LocalContext.current

    val ticketsViewModel: TicketsViewModel = viewModel(
        factory = TicketsViewModelFactory(
            sessionManager = SessionManager(
                context.applicationContext
            )
        )
    )

    val uiState by ticketsViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis tickets")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTicket
            ) {
                Text("+")
            }
        }
    ) { paddingValues ->

        when {
            uiState.isLoading -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.errorMessage != null -> {
                ErrorContent(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = ticketsViewModel::loadTickets,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.tickets.isEmpty() -> {
                EmptyContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.tickets,
                        key = { ticket ->
                            ticket.id ?: ticket.hashCode().toLong()
                        }
                    ) { ticket ->
                        TicketCard(
                            ticket = ticket,
                            onClick = {
                                ticket.id?.let(onTicketClick)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(
    ticket: Ticket,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = ticket.title ?: "Ticket sin título",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            ticket.clientName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            ticket.reportedBy?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reportó: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            ticket.department?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Departamento: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Estado: ${ticket.status ?: "Sin estado"}",
                color = statusColor(ticket.status),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Prioridad: ${ticket.priority ?: "Sin prioridad"}",
                color = priorityColor(ticket.priority),
                fontWeight = FontWeight.SemiBold
            )

            ticket.openedAt?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fecha: ${formatTicketDate(it)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetry
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text("No hay tickets disponibles")
    }
}


@Composable
private fun statusColor(status: String?) =
    when (status?.lowercase()) {
        "cerrada", "cerrado", "resuelto" ->
            MaterialTheme.colorScheme.primary

        "en proceso" ->
            MaterialTheme.colorScheme.tertiary

        "abierta", "abierto" ->
            MaterialTheme.colorScheme.error

        else ->
            MaterialTheme.colorScheme.onSurface
    }

@Composable
private fun priorityColor(priority: String?) =
    when (priority?.lowercase()) {
        "alta" ->
            MaterialTheme.colorScheme.error

        "media" ->
            MaterialTheme.colorScheme.tertiary

        "baja" ->
            MaterialTheme.colorScheme.primary

        else ->
            MaterialTheme.colorScheme.onSurface
    }

private fun formatTicketDate(date: String): String {
    return try {
        val instant = java.time.Instant.parse(date)

        val formatter = java.time.format.DateTimeFormatter
            .ofPattern("dd MMM yyyy")
            .withLocale(java.util.Locale("es", "MX"))
            .withZone(java.time.ZoneId.systemDefault())

        formatter.format(instant)
    } catch (_: Exception) {
        date
    }
}