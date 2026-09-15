package com.luisvicente.prontotix.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luisvicente.prontotix.admin.DevicePolicyManagerHelper
import com.luisvicente.prontotix.scheduler.ShiftAutomationManager
import kotlinx.coroutines.launch
import com.luisvicente.prontotix.admin.MaintenanceModeManager

private val ProntoNavy =
    Color(0xFF0B1930)

private val ProntoBlue =
    Color(0xFF1677FF)

private val ProntoGreen =
    Color(0xFF10B981)

private val ProntoBackground =
    Color(0xFFF4F7FB)

private val ProntoDanger =
    Color(0xFFE05252)

private val ProntoText =
    Color(0xFF12233D)

private val ProntoMuted =
    Color(0xFF64748B)

@Composable
fun AdminScreen(
    onBack: () -> Unit
) {

    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    val isDeviceOwner =
        remember {
            DevicePolicyManagerHelper
                .isDeviceOwner(context)
        }

    var maintenanceMode by remember {
        mutableStateOf(
            MaintenanceModeManager
                .isEnabled(context)
        )
    }

    var actionMessage by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                ProntoBackground
            )
    ) {

        /*
         * HEADER
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    ProntoNavy
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "‹",
                color = Color.White,
                style =
                    MaterialTheme
                        .typography
                        .headlineMedium,
                modifier =
                    Modifier.padding(
                        end = 14.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "Panel administrador",
                    color =
                        Color.White,
                    fontWeight =
                        FontWeight.Bold,
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge
                )

                Text(
                    text =
                        "ProntoTix",
                    color =
                        Color(0xFF69D8E8),
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }

            OutlinedButton(
                onClick =
                    onBack,
                colors =
                    ButtonDefaults
                        .outlinedButtonColors(
                            contentColor =
                                Color.White
                        )
            ) {

                Text(
                    "Salir"
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            /*
             * ESTADO ADMINISTRACIÓN
             */
            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            16.dp
                        )
                ) {

                    Text(
                        text =
                            "Administración del dispositivo",
                        color =
                            ProntoText,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )

                    Text(
                        text =
                            if (isDeviceOwner) {
                                "● Dispositivo administrado"
                            } else {
                                "● Administración limitada"
                            },
                        color =
                            if (isDeviceOwner) {
                                ProntoGreen
                            } else {
                                ProntoDanger
                            },
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )

                    if (!isDeviceOwner) {

                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )

                        Text(
                            text =
                                "ProntoTix todavía no es Device Owner en este teléfono.",
                            color =
                                ProntoMuted,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Text(
                text =
                    "CONTROL DE JORNADA",
                color =
                    ProntoMuted,
                fontWeight =
                    FontWeight.Bold,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            /*
             * INICIAR JORNADA
             */
            Button(
                onClick = {

                    scope.launch {

                        actionMessage =
                            "Iniciando jornada..."

                        ShiftAutomationManager
                            .startAutomaticShift(
                                context
                            )

                        actionMessage =
                            "Proceso de inicio ejecutado"
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                shape =
                    RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults
                        .buttonColors(
                            containerColor =
                                ProntoBlue
                        )
            ) {

                Text(
                    text =
                        "▶  Iniciar jornada",
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            /*
             * FINALIZAR JORNADA
             */
            OutlinedButton(
                onClick = {

                    scope.launch {

                        actionMessage =
                            "Finalizando jornada..."

                        ShiftAutomationManager
                            .endAutomaticShift(
                                context
                            )

                        actionMessage =
                            "Proceso de finalización ejecutado"
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                shape =
                    RoundedCornerShape(14.dp)
            ) {

                Text(
                    text =
                        "■  Finalizar jornada",
                    color =
                        ProntoDanger,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )

            Text(
                text =
                    "ADMINISTRACIÓN DEL EQUIPO",
                color =
                    ProntoMuted,
                fontWeight =
                    FontWeight.Bold,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            /*
             * MODO MANTENIMIENTO
             */
            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                                "Modo mantenimiento",
                            color =
                                ProntoText,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                if (maintenanceMode) {
                                    "Restricciones liberadas temporalmente"
                                } else {
                                    "Restricciones normales del equipo"
                                },
                            color =
                                ProntoMuted,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }

                    Switch(
                        checked =
                            maintenanceMode,
                        enabled =
                            isDeviceOwner,
                        onCheckedChange = {
                                enabled ->

                            val success =
                                if (enabled) {

                                    DevicePolicyManagerHelper
                                        .enableMaintenanceMode(
                                            context
                                        )

                                } else {

                                    DevicePolicyManagerHelper
                                        .disableMaintenanceMode(
                                            context
                                        )
                                }

                            if (success) {

                                MaintenanceModeManager
                                    .setEnabled(
                                        context,
                                        enabled
                                    )

                                maintenanceMode =
                                    enabled

                                actionMessage =
                                    if (enabled) {
                                        "Modo mantenimiento activado"
                                    } else {
                                        "Restricciones restauradas"
                                    }

                            } else {

                                actionMessage =
                                    "No se pudo cambiar el modo mantenimiento"
                            }
                        }
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )


            actionMessage?.let {
                    message ->

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Text(
                    text =
                        message,
                    modifier =
                        Modifier.fillMaxWidth(),
                    color =
                        ProntoMuted,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }

            Spacer(
                modifier =
                    Modifier.weight(1f)
            )

            Text(
                text =
                    "⚠ Acceso exclusivo para personal autorizado",
                modifier =
                    Modifier.fillMaxWidth(),
                color =
                    ProntoMuted,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )
        }
    }
}