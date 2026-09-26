package com.luisvicente.prontotix.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.scheduler.ShiftAutomationManager
import kotlinx.coroutines.launch

private val ProntoNavy =
    Color(0xFF0B1930)

private val ProntoBlue =
    Color(0xFF1677FF)

private val ProntoBackground =
    Color(0xFFF4F7FB)

private val ProntoDanger =
    Color(0xFFE05252)

private val ProntoMuted =
    Color(0xFF64748B)

@Composable
fun AdminScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onDiagnosticsClick: () -> Unit
) {

    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    var actionMessage by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    ProntoBackground
                )
    ) {

        /*
         * HEADER
         */
        Row(
            modifier =
                Modifier
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
                    Modifier
                        .clickable {
                            onBack()
                        }
                        .padding(
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
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
        ) {

            /*
             * CONTROL DE JORNADA
             */
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

            /*
             * DIAGNÓSTICO
             */
            Text(
                text =
                    "DIAGNÓSTICO",
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

            OutlinedButton(
                onClick =
                    onDiagnosticsClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                shape =
                    RoundedCornerShape(14.dp)
            ) {

                Text(
                    text =
                        "Diagnóstico del dispositivo",
                    color =
                        ProntoBlue,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            /*
             * CERRAR SESIÓN
             */
            OutlinedButton(
                onClick = {

                    scope.launch {

                        actionMessage =
                            "Cerrando sesión..."

                        SessionManager(
                            context.applicationContext
                        ).clearSession()

                        onLogout()
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
                        "Cerrar sesión",
                    color =
                        ProntoDanger,
                    fontWeight =
                        FontWeight.Bold
                )
            }

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