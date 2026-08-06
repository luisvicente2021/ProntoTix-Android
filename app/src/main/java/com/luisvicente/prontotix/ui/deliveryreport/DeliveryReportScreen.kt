package com.luisvicente.prontotix.ui.deliveryreport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisvicente.prontotix.data.model.DeliveryItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryReportScreen(
    ticketId: Long,
    onBack: () -> Unit,
    deliveryReportViewModel: DeliveryReportViewModel = viewModel()
) {
    val uiState by
    deliveryReportViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Reporte de entrega")
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Asignación #$ticketId",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Materiales entregados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            uiState.items.forEachIndexed { index, item ->
                DeliveryItemCard(
                    itemNumber = index + 1,
                    item = item,
                    canRemove = uiState.items.size > 1,
                    onMaterialChange = { material ->
                        deliveryReportViewModel.updateMaterial(
                            itemId = item.id,
                            material = material
                        )
                    },
                    onQuantityChange = { quantity ->
                        deliveryReportViewModel.updateQuantity(
                            itemId = item.id,
                            quantity = quantity
                        )
                    },
                    onUnitPriceChange = { price ->
                        deliveryReportViewModel.updateUnitPrice(
                            itemId = item.id,
                            unitPrice = price
                        )
                    },
                    onRemove = {
                        deliveryReportViewModel.removeItem(
                            itemId = item.id
                        )
                    }
                )
            }

            OutlinedButton(
                onClick = deliveryReportViewModel::addItem,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar material")
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Total de la compra",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatCurrency(
                            uiState.grandTotal
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedTextField(
                value = uiState.provider,
                onValueChange =
                    deliveryReportViewModel::updateProvider,
                label = {
                    Text("Proveedor")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.receiverName,
                onValueChange =
                    deliveryReportViewModel::updateReceiverName,
                label = {
                    Text("Nombre de quien recibe")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.observations,
                onValueChange =
                    deliveryReportViewModel::updateObservations,
                label = {
                    Text("Observaciones")
                },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            uiState.successMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    deliveryReportViewModel.saveReport(ticketId)
                },
                enabled = isReportValid(uiState) &&
                        !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (uiState.isSaving) {
                        "Guardando..."
                    } else {
                        "Guardar reporte"
                    }
                )
            }
        }
    }
}

@Composable
private fun DeliveryItemCard(
    itemNumber: Int,
    item: DeliveryItem,
    canRemove: Boolean,
    onMaterialChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitPriceChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Material $itemNumber",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (canRemove) {
                    TextButton(
                        onClick = onRemove
                    ) {
                        Text("Eliminar")
                    }
                }
            }

            OutlinedTextField(
                value = item.material,
                onValueChange = onMaterialChange,
                label = {
                    Text("Descripción del material")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = item.quantity,
                onValueChange = { value ->
                    if (
                        value.isEmpty() ||
                        value.all { character ->
                            character.isDigit()
                        }
                    ) {
                        onQuantityChange(value)
                    }
                },
                label = {
                    Text("Cantidad")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = item.unitPrice,
                onValueChange = { value ->
                    if (isValidDecimalInput(value)) {
                        onUnitPriceChange(value)
                    }
                },
                label = {
                    Text("Precio unitario")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Importe: ${formatCurrency(item.total)}",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun isValidDecimalInput(
    value: String
): Boolean {
    if (value.isEmpty()) {
        return true
    }

    return value.matches(
        Regex("""^\d{0,8}([.]\d{0,2})?$""")
    )
}

private fun isReportValid(
    uiState: DeliveryReportUiState
): Boolean {
    val validItems = uiState.items.all { item ->
        item.material.isNotBlank() &&
                (item.quantity.toDoubleOrNull() ?: 0.0) > 0 &&
                (item.unitPrice.toDoubleOrNull() ?: 0.0) >= 0
    }

    return validItems &&
            uiState.receiverName.isNotBlank()
}

private fun formatCurrency(
    amount: Double
): String {
    return NumberFormat
        .getCurrencyInstance(
            Locale("es", "MX")
        )
        .format(amount)
}