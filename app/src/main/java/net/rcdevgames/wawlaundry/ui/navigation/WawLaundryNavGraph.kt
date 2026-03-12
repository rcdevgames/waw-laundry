package net.rcdevgames.wawlaundry.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import net.rcdevgames.wawlaundry.ui.cashier.CashierHomeScreen
import net.rcdevgames.wawlaundry.ui.order.NewOrderViewModel
import net.rcdevgames.wawlaundry.ui.order.PosScreen
import net.rcdevgames.wawlaundry.ui.owner.OwnerDashboardScreen
import net.rcdevgames.wawlaundry.ui.owner.store.OwnerStoreScreen
import net.rcdevgames.wawlaundry.ui.owner.printer.OwnerPrinterScreen
import net.rcdevgames.wawlaundry.ui.owner.data.OwnerDataScreen
import net.rcdevgames.wawlaundry.ui.owner.expenses.OwnerExpensesScreen
import net.rcdevgames.wawlaundry.ui.owner.promos.OwnerPromosScreen
import net.rcdevgames.wawlaundry.ui.owner.reports.OwnerReportsScreen
import net.rcdevgames.wawlaundry.ui.owner.services.OwnerServicesScreen
import net.rcdevgames.wawlaundry.ui.printer.PrintPreviewScreen
import net.rcdevgames.wawlaundry.ui.queue.OrderQueueScreen
import net.rcdevgames.wawlaundry.ui.security.PinUnlockScreen
import net.rcdevgames.wawlaundry.ui.setup.BusinessSetupScreen
import net.rcdevgames.wawlaundry.ui.setup.CloudSyncSetupScreen
import net.rcdevgames.wawlaundry.ui.setup.SetupSecurityScreen

@Composable
fun WawLaundryNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.SetupSecurity.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        
        // --- SETUP AREA ---
        composable(route = Screen.SetupSecurity.route) {
            SetupSecurityScreen(
                onSetupComplete = {
                    navController.navigate(Screen.BusinessSetup.route) {
                        popUpTo(Screen.SetupSecurity.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.BusinessSetup.route) {
            BusinessSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.CloudSyncSetup.route) {
                        popUpTo(Screen.BusinessSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.CloudSyncSetup.route) {
            CloudSyncSetupScreen(
                onLoginSuccess = {
                    // Navigate to Home Dashboard after successful login
                    navController.navigate(Screen.CashierHome.route) {
                        popUpTo(Screen.CloudSyncSetup.route) { inclusive = true }
                    }
                },
                onSkip = {
                    // Navigate to Home Dashboard (Offline Mode)
                    navController.navigate(Screen.CashierHome.route) {
                        popUpTo(Screen.CloudSyncSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // --- SECURITY GATES ---
        composable(route = Screen.PinUnlock.route) {
            PinUnlockScreen(
                onUnlockSuccess = {
                    navController.navigate(Screen.OwnerDashboard.route) {
                        popUpTo(Screen.PinUnlock.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- MAIN APP ---
        composable(route = Screen.CashierHome.route) {
            CashierHomeScreen(
                onNavigateToNewOrder = {
                    navController.navigate("new_order")
                },
                onNavigateToQueue = {
                    navController.navigate("order_queue")
                },
                onNavigateToOwner = {
                    navController.navigate(Screen.PinUnlock.route)
                }
            )
        }

        composable(route = "order_queue") {
            OrderQueueScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- NEW TRANSACTION FLOW ---
        composable(route = "new_order") {
            PosScreen(
                onNavigateBack = { navController.popBackStack() },
                onFinishActivity = {
                    navController.navigate(Screen.CashierHome.route) {
                        popUpTo(Screen.CashierHome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.OwnerDashboard.route) {
            OwnerDashboardScreen(
                onNavigateBack = { navController.popBackStack(Screen.CashierHome.route, false) },
                onNavigateToServices = { navController.navigate("owner_services") },
                onNavigateToPromos = { navController.navigate("owner_promos") },
                onNavigateToExpenses = { navController.navigate("owner_expenses") },
                onNavigateToReports = { navController.navigate("owner_reports") },
                onNavigateToStore = { navController.navigate(Screen.OwnerStore.route) },
                onNavigateToPrinter = { navController.navigate(Screen.OwnerPrinter.route) },
                onNavigateToData = { navController.navigate(Screen.OwnerData.route) }
            )
        }

        composable(route = "owner_services") {
            OwnerServicesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = "owner_promos") {
            OwnerPromosScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = "owner_expenses") {
            OwnerExpensesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = "owner_reports") {
            OwnerReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.OwnerStore.route) {
            OwnerStoreScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.OwnerPrinter.route) {
            OwnerPrinterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.OwnerData.route) {
            OwnerDataScreen(
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(Screen.SetupSecurity.route) {
                        popUpTo(0) { inclusive = true } // Clear entire backstack
                    }
                }
            )
        }

        composable(
            route = "print_preview/{orderId}",
            arguments = listOf(androidx.navigation.navArgument("orderId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            PrintPreviewScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
