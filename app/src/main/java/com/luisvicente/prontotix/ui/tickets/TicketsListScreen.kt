package com.luisvicente.prontotix.ui.tickets

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.model.Ticket

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

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            ticketsViewModel.loadTickets()
            onRefreshHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
                    message =
                        uiState.errorMessage
                            .orEmpty(),
                    onRetry =
                        ticketsViewModel::loadTickets,
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
                    contentPadding =
                        PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = uiState.tickets,
                        key = { ticket ->
                            ticket.id
                                ?: ticket
                                    .hashCode()
                                    .toLong()
                        }
                    ) { ticket ->
                        DiligenceCard(
                            ticket = ticket,
                            onClick = {
                                ticket.id?.let(
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

@Composable
private fun DiligenceCard(
    ticket: Ticket,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
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
            modifier = Modifier.padding(18.dp)
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
                    status = ticket.status
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text =
                    ticket.title
                        ?: "Diligencia sin título",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight = FontWeight.Bold
            )

            ticket.clientName
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { client ->
                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
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
                modifier = Modifier.height(16.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                PriorityBadge(
                    priority =
                        ticket.priority
                )

                ticket.department
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { department ->
                        Surface(
                            shape =
                                RoundedCornerShape(
                                    50
                                ),
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                        ) {
                            Text(
                                text = department,
                                modifier =
                                    Modifier.padding(
                                        horizontal =
                                            10.dp,
                                        vertical =
                                            5.dp
                                    ),
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelMedium
                            )
                        }
                    }
            }

            ticket.reportedBy
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { reportedBy ->

                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )

                    Text(
                        text =
                            "Solicitante: $reportedBy",
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

            ticket.openedAt?.let { date ->

                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                Text(
                    text =
                        "Asignada: ${
                            formatTicketDate(
                                date
                            )
                        }",
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
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Ver detalle →",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
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
                status ?: "Sin estado"
        }

    val containerColor =
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

    val contentColor =
        when (normalized) {
            "abierta",
            "abierto" ->
                MaterialTheme
                    .colorScheme
                    .onErrorContainer

            "en proceso" ->
                MaterialTheme
                    .colorScheme
                    .onTertiaryContainer

            "cerrada",
            "cerrado",
            "terminada" ->
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        }

    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Text(
            text = label,
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp
                ),
            color = contentColor,
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
private fun PriorityBadge(
    priority: String?
) {
    val normalized =
        priority
            ?.trim()
            ?.lowercase()
            .orEmpty()

    val containerColor =
        when (normalized) {
            "alta",
            "crítica",
            "critica" ->
                MaterialTheme
                    .colorScheme
                    .errorContainer

            "media" ->
                MaterialTheme
                    .colorScheme
                    .tertiaryContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .primaryContainer
        }

    val contentColor =
        when (normalized) {
            "alta",
            "crítica",
            "critica" ->
                MaterialTheme
                    .colorScheme
                    .onErrorContainer

            "media" ->
                MaterialTheme
                    .colorScheme
                    .onTertiaryContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer
        }

    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Text(
            text =
                "Prioridad ${
                    priority
                        ?: "Sin prioridad"
                }",
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp
                ),
            color = contentColor,
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.padding(24.dp),
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
            modifier = Modifier.height(8.dp)
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
                Modifier.height(20.dp)
        )

        Button(
            onClick = onRetry
        ) {
            Text("Intentar nuevamente")
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.padding(24.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sin diligencias pendientes",
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                "Cuando te asignen una nueva diligencia aparecerá aquí.",
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
            java.time.Instant.parse(date)

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

    } catch (_: Exception) {
        date
    }
}