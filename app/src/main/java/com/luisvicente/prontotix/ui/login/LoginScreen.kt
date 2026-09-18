package com.luisvicente.prontotix.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisvicente.prontotix.R
import com.luisvicente.prontotix.data.local.SessionManager
import kotlinx.coroutines.launch
import com.luisvicente.prontotix.scheduler.ShiftAutomationManager
import com.luisvicente.prontotix.scheduler.WorkSchedule

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context =
        LocalContext.current

    val coroutineScope =
        rememberCoroutineScope()

    val credentialManager =
        remember {
            CredentialManager.create(
                context
            )
        }

    val loginViewModel: LoginViewModel =
        viewModel(
            factory =
                LoginViewModelFactory(
                    sessionManager =
                        SessionManager(
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

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var credentialMessage by remember {
        mutableStateOf<String?>(null)
    }

    /*
     * Indica que usuario + contraseña
     * fueron recuperados desde
     * Credential Manager.
     *
     * Si es true NO volvemos a pedir
     * guardar la misma contraseña.
     */
    var credentialWasLoaded by remember {
        mutableStateOf(false)
    }

    val uiState by
    loginViewModel
        .uiState
        .collectAsStateWithLifecycle()

    /*
     * LOGIN EXITOSO
     *
     * Solo solicitamos guardar la contraseña
     * cuando fue escrita manualmente.
     *
     * Si vino de Google Password Manager,
     * ya está guardada y no preguntamos
     * nuevamente.
     */
    LaunchedEffect(
        uiState.isSuccess
    ) {
        if (
            uiState.isSuccess
        ) {
            if (
                !credentialWasLoaded &&
                email.isNotBlank() &&
                password.isNotBlank()
            ) {
                try {
                    val request =
                        CreatePasswordRequest(
                            id =
                                email.trim(),
                            password =
                                password
                        )

                    credentialManager
                        .createCredential(
                            context =
                                context,
                            request =
                                request
                        )

                } catch (
                    error:
                    CreateCredentialException
                ) {
                    /*
                     * Si cancela el guardado
                     * seguimos entrando normalmente.
                     */
                }
            }
            if (
                WorkSchedule.isWithinWorkingHours()
            ) {
                ShiftAutomationManager.startAutomaticShift(
                    context.applicationContext
                )
            }
            onLoginSuccess()
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .background(
                            color =
                                androidx.compose.ui.graphics.Color(
                                    0xFF071A2B
                                ),
                            shape =
                                RoundedCornerShape(24.dp)
                        )
                        .padding(12.dp),
                contentAlignment =
                    Alignment.Center
            ) {
                Image(
                    painter =
                        painterResource(
                            id =
                                R.drawable.prontotix_logo
                        ),
                    contentDescription =
                        "Logo ProntoTix",
                    modifier =
                        Modifier.fillMaxSize()
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text =
                    "ProntoTix",
                style =
                    MaterialTheme
                        .typography
                        .headlineLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Text(
                text =
                    "Gestión de diligencias",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(
                        32.dp
                    )
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(
                        24.dp
                    ),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surface
                    ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation =
                            4.dp
                    )
            ) {
                Column(
                    modifier =
                        Modifier.padding(
                            24.dp
                        )
                ) {

                    Text(
                        text =
                            "Bienvenido",
                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                6.dp
                            )
                    )

                    Text(
                        text =
                            "Ingresa con tu cuenta para consultar tus diligencias.",
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                24.dp
                            )
                    )

                    /*
                     * CORREO
                     */
                    OutlinedTextField(
                        value =
                            email,
                        onValueChange = {
                            email = it

                            /*
                             * Si modifica manualmente
                             * el usuario, ya no consideramos
                             * que sea exactamente la
                             * credencial recuperada.
                             */
                            credentialWasLoaded =
                                false
                        },
                        label = {
                            Text(
                                "Correo electrónico"
                            )
                        },
                        placeholder = {
                            Text(
                                "usuario@prontotix.com"
                            )
                        },
                        enabled =
                            !uiState.isLoading,
                        singleLine =
                            true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Email
                            ),
                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            credentialWasLoaded = false
                        },
                        label = {
                            Text("Contraseña")
                        },
                        enabled = !uiState.isLoading,
                        singleLine = true,

                        visualTransformation =
                            if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },

                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    passwordVisible = !passwordVisible
                                }
                            ) {
                                Icon(
                                    imageVector =
                                        if (passwordVisible) {
                                            Icons.Default.VisibilityOff
                                        } else {
                                            Icons.Default.Visibility
                                        },
                                    contentDescription =
                                        if (passwordVisible) {
                                            "Ocultar contraseña"
                                        } else {
                                            "Mostrar contraseña"
                                        }
                                )
                            }
                        },

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

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )

                    /*
                     * USAR CONTRASEÑA GUARDADA
                     */
                    OutlinedButton(
                        onClick = {
                            credentialMessage =
                                null

                            coroutineScope.launch {
                                try {
                                    val getPasswordOption =
                                        GetPasswordOption()

                                    val request =
                                        GetCredentialRequest(
                                            listOf(
                                                getPasswordOption
                                            )
                                        )

                                    val result =
                                        credentialManager
                                            .getCredential(
                                                context =
                                                    context,
                                                request =
                                                    request
                                            )

                                    val credential =
                                        result.credential

                                    if (
                                        credential
                                                is PasswordCredential
                                    ) {
                                        email =
                                            credential.id

                                        password =
                                            credential.password

                                        /*
                                         * MUY IMPORTANTE:
                                         * esta contraseña ya
                                         * estaba guardada.
                                         */
                                        credentialWasLoaded =
                                            true

                                        credentialMessage =
                                            "Credencial cargada correctamente."
                                    } else {
                                        credentialWasLoaded =
                                            false

                                        credentialMessage =
                                            "No se recibió una contraseña guardada."
                                    }

                                } catch (
                                    error:
                                    GetCredentialException
                                ) {
                                    credentialWasLoaded =
                                        false

                                    credentialMessage =
                                        "No se encontró una contraseña guardada."
                                }
                            }
                        },
                        enabled =
                            !uiState.isLoading,
                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    48.dp
                                )
                    ) {
                        Text(
                            text =
                                "🔑 Usar contraseña guardada",
                            fontWeight =
                                FontWeight.Medium
                        )
                    }

                    credentialMessage
                        ?.let { message ->

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        10.dp
                                    )
                            )

                            Text(
                                text =
                                    message,
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

                    /*
                     * ERROR LOGIN
                     */
                    uiState
                        .errorMessage
                        ?.let { message ->

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        16.dp
                                    )
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
                                    text =
                                        message,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onErrorContainer,
                                    modifier =
                                        Modifier.padding(
                                            12.dp
                                        ),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium
                                )
                            }
                        }

                    Spacer(
                        modifier =
                            Modifier.height(
                                24.dp
                            )
                    )

                    /*
                     * INICIAR SESIÓN
                     */
                    Button(
                        onClick = {
                            credentialMessage =
                                null

                            loginViewModel.login(
                                email =
                                    email.trim(),
                                password =
                                    password
                            )
                        },
                        enabled =
                            !uiState.isLoading &&
                                    email.isNotBlank() &&
                                    password.isNotBlank(),
                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    52.dp
                                )
                    ) {
                        if (
                            uiState.isLoading
                        ) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier.height(
                                        24.dp
                                    ),
                                strokeWidth =
                                    2.dp,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimary
                            )
                        } else {
                            Text(
                                text =
                                    "Iniciar sesión",
                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )

            Text(
                text =
                    "ProntoTix · Operación y seguimiento",
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                textAlign =
                    TextAlign.Center
            )
        }
    }
}