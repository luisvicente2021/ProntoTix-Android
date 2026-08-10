package com.luisvicente.prontotix.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luisvicente.prontotix.ui.login.LoginScreen
import com.luisvicente.prontotix.ui.ticketdetail.TicketDetailScreen
import com.luisvicente.prontotix.ui.tickets.TicketsListScreen
import com.luisvicente.prontotix.ui.deliveryreport.DeliveryReportScreen
import com.luisvicente.prontotix.ui.location.LocationTestScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.LOGIN
    ) {
        composable(AppRoute.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    //navController.navigate(AppRoute.TICKETS) {
                    navController.navigate(AppRoute.LOCATION_TEST) {
                        popUpTo(AppRoute.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(AppRoute.TICKETS) { backStackEntry ->

            val shouldRefresh by backStackEntry
                .savedStateHandle
                .getStateFlow("refresh_tickets", false)
                .collectAsStateWithLifecycle()

            TicketsListScreen(
                refreshTrigger = shouldRefresh,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle[
                        "refresh_tickets"
                    ] = false
                },
                onTicketClick = { ticketId ->
                    navController.navigate(
                        AppRoute.ticketDetail(ticketId)
                    )
                }
            )
        }

        composable(
            route = AppRoute.TICKET_DETAIL,
            arguments = listOf(
                navArgument("ticketId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

            val ticketId = backStackEntry.arguments
                ?.getLong("ticketId")
                ?: return@composable

            TicketDetailScreen(
                ticketId = ticketId,
                onBack = {
                    navController.popBackStack()
                },
                onStatusUpdated = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_tickets", true)
                },
                onOpenDeliveryReport = {
                    navController.navigate(
                        AppRoute.deliveryReport(ticketId)
                    )
                }
            )
        }

        composable(
            route = AppRoute.DELIVERY_REPORT,
            arguments = listOf(
                navArgument("ticketId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

            val ticketId = backStackEntry.arguments
                ?.getLong("ticketId")
                ?: return@composable

            DeliveryReportScreen(
                ticketId = ticketId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Prueba temporal de GPS
        composable(AppRoute.LOCATION_TEST) {
            LocationTestScreen()
        }
    }
}