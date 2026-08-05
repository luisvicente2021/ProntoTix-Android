package com.luisvicente.prontotix.ui.deliveryreport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryReportScreen(
    ticketId: Long,
    onBack: () -> Unit
) {
    var material by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("") }
    var receiverName by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Asignación #$ticketId")

            OutlinedTextField(
                value = material,
                onValueChange = { material = it },
                label = {
                    Text("Material entregado")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = {
                    Text("Cantidad")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = {
                    Text("Precio unitario")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = provider,
                onValueChange = { provider = it },
                label = {
                    Text("Proveedor")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = receiverName,
                onValueChange = { receiverName = it },
                label = {
                    Text("Nombre de quien recibe")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = observations,
                onValueChange = { observations = it },
                label = {
                    Text("Observaciones")
                },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    // Después guardaremos el reporte.
                },
                enabled = material.isNotBlank() &&
                        quantity.isNotBlank() &&
                        receiverName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar reporte")
            }
        }
    }
}