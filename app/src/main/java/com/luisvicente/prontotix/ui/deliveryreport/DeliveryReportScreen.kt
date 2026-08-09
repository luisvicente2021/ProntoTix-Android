package com.luisvicente.prontotix.ui.deliveryreport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.model.DeliveryItem
import com.luisvicente.prontotix.data.model.EvidencePhoto
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryReportScreen(
    ticketId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val deliveryReportViewModel: DeliveryReportViewModel = viewModel(
        factory = DeliveryReportViewModelFactory(
            sessionManager = SessionManager(
                context.applicationContext
            )
        )
    )

    val uiState by
    deliveryReportViewModel.uiState.collectAsStateWithLifecycle()

    val receiptPhotoLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                deliveryReportViewModel.updateReceiptPhoto(
                    EvidencePhoto(
                        uri = it.toString()
                    )
                )
            }
        }

    val evidencePhotoLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents()
        ) { uris ->
            uris.forEach { uri ->
                deliveryReportViewModel.addEvidence(
                    EvidencePhoto(
                        uri = uri.toString()
                    )
                )
            }
        }

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

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

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

            Text(
                text = "Recibo de compra",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = {
                    receiptPhotoLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (uiState.receiptPhoto == null) {
                        "Seleccionar foto del recibo"
                    } else {
                        "Cambiar foto del recibo"
                    }
                )
            }

            uiState.receiptPhoto?.let { photo ->
                Image(
                    painter = rememberAsyncImagePainter(
                        photo.uri
                    ),
                    contentDescription = "Foto del recibo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(
                            RoundedCornerShape(12.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = "Evidencias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = {
                    evidencePhotoLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar fotografías de evidencia")
            }

            uiState.evidencePhotos.forEachIndexed {
                    index,
                    photo ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Evidencia ${index + 1}",
                            fontWeight = FontWeight.SemiBold
                        )

                        Image(
                            painter = rememberAsyncImagePainter(
                                photo.uri
                            ),
                            contentDescription =
                                "Evidencia ${index + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(
                                    RoundedCornerShape(12.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )

                        TextButton(
                            onClick = {
                                deliveryReportViewModel
                                    .removeEvidence(
                                        photo.id
                                    )
                            }
                        ) {
                            Text("Eliminar evidencia")
                        }
                    }
                }
            }

            Text(
                text = "Firma de quien recibe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SignaturePad(
                onSignatureChanged = { signature ->
                    deliveryReportViewModel.updateSignature(
                        signature
                    )
                }
            )

            OutlinedButton(
                onClick = {
                    deliveryReportViewModel.clearSignature()
                },
                enabled = uiState.signature != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Borrar firma")
            }

            Button(
                onClick = {
                    deliveryReportViewModel.saveReport(
                        ticketId
                    )
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

            uiState.successMessage?.let { message ->
                Text(
                    text = message,
                    color =
                        MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color =
                        MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    deliveryReportViewModel.generatePdf(
                        context = context,
                        ticketId = ticketId
                    )
                },
                enabled =
                    uiState.savedReport != null &&
                            !uiState.isGeneratingPdf,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (uiState.isGeneratingPdf) {
                        "Generando PDF..."
                    } else {
                        "Generar PDF"
                    }
                )
            }

            uiState.generatedPdfPath?.let { path ->
                Text(
                    text = "PDF creado en:\n$path",
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            if (uiState.generatedPdfPath != null) {
                OutlinedButton(
                    onClick = {
                        deliveryReportViewModel
                            .openGeneratedPdf(context)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver PDF")
                }

                OutlinedButton(
                    onClick = {
                        deliveryReportViewModel
                            .shareGeneratedPdf(context)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Compartir PDF")
                }
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
                    keyboardType =
                        KeyboardType.Number
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
                    keyboardType =
                        KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text =
                    "Importe: ${formatCurrency(item.total)}",
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
                (item.quantity.toDoubleOrNull()
                    ?: 0.0) > 0 &&
                (item.unitPrice.toDoubleOrNull()
                    ?: 0.0) >= 0
    }

    return validItems &&
            uiState.receiverName.isNotBlank() &&
            uiState.signature != null
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