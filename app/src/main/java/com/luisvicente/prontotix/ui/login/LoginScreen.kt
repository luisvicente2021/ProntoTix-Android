package com.luisvicente.prontotix.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.luisvicente.prontotix.data.local.SessionManager

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(
            sessionManager = SessionManager(
                context.applicationContext
            )
        )
    )

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    val uiState by
    loginViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ProntoTix",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Gestión de diligencias",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Ingresa con tu cuenta para consultar tus diligencias.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        label = {
                            Text("Correo electrónico")
                        },
                        placeholder = {
                            Text("usuario@prontotix.com")
                        },
                        enabled = !uiState.isLoading,
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Email
                            ),
                        shape =
                            RoundedCornerShape(14.dp),
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        label = {
                            Text("Contraseña")
                        },
                        enabled = !uiState.isLoading,
                        singleLine = true,
                        visualTransformation =
                            PasswordVisualTransformation(),
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Password
                            ),
                        shape =
                            RoundedCornerShape(14.dp),
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    uiState.errorMessage?.let { message ->

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Card(
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme
                                            .colorScheme
                                            .errorContainer
                                ),
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = message,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onErrorContainer,
                                modifier =
                                    Modifier.padding(12.dp),
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Button(
                        onClick = {
                            loginViewModel.login(
                                email = email.trim(),
                                password = password
                            )
                        },
                        enabled =
                            !uiState.isLoading &&
                                    email.isNotBlank() &&
                                    password.isNotBlank(),
                        shape =
                            RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier.height(24.dp),
                                strokeWidth = 2.dp,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimary
                            )
                        } else {
                            Text(
                                text = "Iniciar sesión",
                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "ProntoTix · Operación y seguimiento",
                style = MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}