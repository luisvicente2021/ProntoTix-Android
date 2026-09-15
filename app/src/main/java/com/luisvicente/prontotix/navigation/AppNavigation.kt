package com.luisvicente.prontotix.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luisvicente.prontotix.data.local.SessionManager
import com.luisvicente.prontotix.data.repository.AuthRepository
import com.luisvicente.prontotix.ui.deliveryreport.DeliveryReportScreen
import com.luisvicente.prontotix.ui.location.LocationTestScreen
import com.luisvicente.prontotix.ui.login.LoginScreen
import com.luisvicente.prontotix.ui.ticketdetail.TicketDetailScreen
import com.luisvicente.prontotix.ui.tickets.TicketsListScreen
import kotlinx.coroutines.flow.first
import com.luisvicente.prontotix.scheduler.ShiftScheduler
import com.luisvicente.prontotix.ui.driverhome.DriverHomeScreen
import com.luisvicente.prontotix.BuildConfig

private sealed interface StartupState {

    data object Loading : StartupState

    data object LoggedOut : StartupState

    data object LoggedIn : StartupState
}

@Composable
fun AppNavigation(
    navController: NavHostController =
        rememberNavController()
) {
    val context =
        LocalContext.current

    LaunchedEffect(Unit) {

        ShiftScheduler.scheduleDailyShift(
            context.applicationContext
        )
    }

    val sessionManager =
        remember {
            SessionManager(
                context.applicationContext
            )
        }

    val authRepository =
        remember {
            AuthRepository()
        }

    var startupState by remember {
        mutableStateOf<StartupState>(
            StartupState.Loading
        )
    }

    LaunchedEffect(Unit) {

        val accessToken =
            sessionManager
                .accessToken
                .first()

        val refreshToken =
            sessionManager
                .refreshToken
                .first()

        /*
         * Si no existe ninguna sesión guardada,
         * mostramos Login.
         */
        if (
            accessToken.isNullOrBlank() &&
            refreshToken.isNullOrBlank()
        ) {
            startupState =
                StartupState.LoggedOut

            return@LaunchedEffect
        }

        /*
         * Si tenemos refresh token,
         * intentamos renovar la sesión.
         *
         * Así evitamos entrar a la aplicación
         * con un access token ya vencido.
         */
        if (
            !refreshToken.isNullOrBlank()
        ) {
            authRepository
                .refreshSession(
                    refreshToken
                )
                .onSuccess { response ->

                    val newAccessToken =
                        response.access_token

                    if (
                        newAccessToken
                            .isNullOrBlank()
                    ) {
                        sessionManager
                            .clearSession()

                        startupState =
                            StartupState.LoggedOut
                    } else {

                        sessionManager
                            .saveSession(
                                accessToken =
                                    newAccessToken,
                                refreshToken =
                                    response.refresh_token
                                        ?: refreshToken
                            )

                        startupState =
                            StartupState.LoggedIn
                    }
                }
                .onFailure {

                    sessionManager
                        .clearSession()

                    startupState =
                        StartupState.LoggedOut
                }

            return@LaunchedEffect
        }

        /*
         * Compatibilidad temporal con sesiones
         * antiguas que solamente tenían
         * access token guardado.
         */
        if (
            !accessToken.isNullOrBlank()
        ) {
            startupState =
                StartupState.LoggedIn
        } else {
            startupState =
                StartupState.LoggedOut
        }
    }

    /*
     * Mientras revisamos / renovamos
     * la sesión no mostramos Login.
     */
    if (
        startupState ==
        StartupState.Loading
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        return
    }

    val startDestination =
        when (startupState) {

            StartupState.LoggedIn ->
                AppRoute.DRIVER_HOME

            else ->
                AppRoute.LOGIN
        }


    NavHost(
        navController =
            navController,
        startDestination =
            startDestination
    ) {

        composable(
            AppRoute.LOGIN
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(
                        AppRoute.DRIVER_HOME
                    ) {
                        popUpTo(
                            AppRoute.LOGIN
                        ) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            AppRoute.DRIVER_HOME
        ) {
            DriverHomeScreen(
                adminPassword = BuildConfig.ADMIN_PASSWORD,
                onAdminClick = {
                    navController.navigate(
                        AppRoute.TICKETS
                    )
                }
            )
        }

        composable(
            AppRoute.TICKETS
        ) { backStackEntry ->

            val shouldRefresh by
            backStackEntry
                .savedStateHandle
                .getStateFlow(
                    "refresh_tickets",
                    false
                )
                .collectAsStateWithLifecycle()

            TicketsListScreen(
                refreshTrigger =
                    shouldRefresh,

                onRefreshHandled = {
                    backStackEntry
                        .savedStateHandle[
                        "refresh_tickets"
                    ] = false
                },

                onTicketClick = {
                        ticketId ->

                    navController.navigate(
                        AppRoute.ticketDetail(
                            ticketId
                        )
                    )
                },
                        onLogout = {
                    navController.navigate(
                        AppRoute.LOGIN
                    ) {
                        popUpTo(
                            AppRoute.TICKETS
                        ) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route =
                AppRoute.TICKET_DETAIL,

            arguments =
                listOf(
                    navArgument(
                        "ticketId"
                    ) {
                        type =
                            NavType.LongType
                    }
                )
        ) { backStackEntry ->

            val ticketId =
                backStackEntry
                    .arguments
                    ?.getLong(
                        "ticketId"
                    )
                    ?: return@composable

            TicketDetailScreen(
                ticketId =
                    ticketId,

                onBack = {
                    navController
                        .popBackStack()
                },

                onStatusUpdated = {
                    navController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            "refresh_tickets",
                            true
                        )
                },

                onOpenDeliveryReport = {
                    navController.navigate(
                        AppRoute
                            .deliveryReport(
                                ticketId
                            )
                    )
                }
            )
        }

        composable(
            route =
                AppRoute.DELIVERY_REPORT,

            arguments =
                listOf(
                    navArgument(
                        "ticketId"
                    ) {
                        type =
                            NavType.LongType
                    }
                )
        ) { backStackEntry ->

            val ticketId =
                backStackEntry
                    .arguments
                    ?.getLong(
                        "ticketId"
                    )
                    ?: return@composable

            DeliveryReportScreen(
                ticketId =
                    ticketId,

                onBack = {
                    navController
                        .popBackStack()
                }
            )
        }

        composable(
            AppRoute.LOCATION_TEST
        ) {
            LocationTestScreen()
        }
    }
}