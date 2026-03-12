package net.rcdevgames.wawlaundry.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SetupSecurity : Screen("setup_security") // 1st Time Install
    object BusinessSetup : Screen("business_setup") // 1st Time Profiling
    object CloudSyncSetup : Screen("cloud_sync_setup") // 1st Time Login Cloud
    object PinUnlock : Screen("pin_unlock")         // Owner Gate
    object CashierHome : Screen("cashier_home")     // Main Dashboard
    object OwnerDashboard : Screen("owner_dashboard")
    object OwnerStore : Screen("owner_store")
    object OwnerPrinter : Screen("owner_printer")
    object OwnerData : Screen("owner_data")
}
