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
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.CreateCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisvicente.prontotix.R
import com.luisvicente.prontotix.data.local.SessionManager

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context =
        LocalContext.current

    /*
     * Administrador de credenciales de Android.
     *
     * ProntoTix NO almacena aquí la contraseña.
     * La contraseña puede guardarse mediante
     * Google Password Manager u otro proveedor
     * configurado en el teléfono.
     */
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

    val uiState by
    loginViewModel
        .uiState
        .collectAsStateWithLifecycle()

    /*
     * Cuando Supabase confirma que
     * las credenciales son correctas:
     *
     * 1. Solicitamos a Android guardar
     *    correo + contraseña.
     *
     * 2. Si el usuario cancela o el teléfono
     *    no tiene un proveedor disponible,
     *    NO bloqueamos el inicio de sesión.
     *
     * 3. Entramos normalmente a ProntoTix.
     */
    LaunchedEffect(
        uiState.isSuccess
    ) {
        if (
            uiState.isSuccess
        ) {

            if (
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
                     * El usuario puede seleccionar
                     * "Ahora no", cancelar el aviso
                     * o no tener un administrador
                     * de contraseñas configurado.
                     *
                     * El login continúa normalmente.
                     */
                }
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

            /*
             * LOGO
             */
            Image(
                painter =
                    painterResource(
                        id =
                            R.drawable
                                .prontotix_logo
                    ),
                contentDescription =
                    "Logo ProntoTix",
                modifier =
                    Modifier.size(
                        110.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text = "ProntoTix",
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

            /*
             * TARJETA DE LOGIN
             */
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                shape =
                    RoundedCornerShape(
                        24.dp
                    ),
                colors =
                    CardDefaults
                        .cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .surface
                        ),
                elevation =
                    CardDefaults
                        .cardElevation(
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
                            !uiState
                                .isLoading,

                        singleLine =
                            true,

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType
                                        .Email
                            ),

                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),

                        modifier =
                            Modifier
                                .fillMaxWidth()
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )

                    /*
                     * CONTRASEÑA
                     */
                    OutlinedTextField(
                        value =
                            password,

                        onValueChange = {
                            password = it
                        },

                        label = {
                            Text(
                                "Contraseña"
                            )
                        },

                        enabled =
                            !uiState
                                .isLoading,

                        singleLine =
                            true,

                        visualTransformation =
                            PasswordVisualTransformation(),

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType
                                        .Password
                            ),

                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),

                        modifier =
                            Modifier
                                .fillMaxWidth()
                    )

                    /*
                     * ERROR DE LOGIN
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
                                    CardDefaults
                                        .cardColors(
                                            containerColor =
                                                MaterialTheme
                                                    .colorScheme
                                                    .errorContainer
                                        ),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                            ) {

                                Text(
                                    text =
                                        message,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onErrorContainer,

                                    modifier =
                                        Modifier
                                            .padding(
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
                            loginViewModel
                                .login(
                                    email =
                                        email.trim(),
                                    password =
                                        password
                                )
                        },

                        enabled =
                            !uiState
                                .isLoading &&
                                    email
                                        .isNotBlank() &&
                                    password
                                        .isNotBlank(),

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
                            uiState
                                .isLoading
                        ) {

                            CircularProgressIndicator(
                                modifier =
                                    Modifier
                                        .height(
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
                                    FontWeight
                                        .SemiBold
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