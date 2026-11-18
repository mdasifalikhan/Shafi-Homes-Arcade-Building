package com.example.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.R
import com.example.data.model.MaintenanceDue
import com.example.data.model.PaymentMethod
import com.example.data.model.RepairTicket
import com.example.data.model.ScheduleEntry
import com.example.data.model.ScheduleSlot
import com.example.data.model.ScheduleStatus
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TradeCategory
import com.example.ui.SocietyDashboardState
import com.example.ui.screens.MaintenanceDuesScreen
import com.example.ui.screens.NewMaintenanceRequestScreen
import com.example.ui.screens.RepairLogsScreen
import com.example.ui.screens.ScheduleScreen

object MaintenanceRequestsRoutes {
    const val LIST = "maintenance_requests/list"
    const val NEW_REQUEST = "maintenance_requests/new"
}

enum class AppDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val navTestTag: String
) {
    MAINTENANCE_REQUESTS(
        route = "maintenance_requests",
        labelRes = R.string.nav_maintenance_requests,
        selectedIcon = Icons.Rounded.Build,
        unselectedIcon = Icons.Outlined.Build,
        navTestTag = "nav_maintenance_requests"
    ),
    PAYMENT_COLLECTION(
        route = "payment_collection",
        labelRes = R.string.nav_payment_collection,
        selectedIcon = Icons.Rounded.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
        navTestTag = "nav_payment_collection"
    ),
    CONTRACTOR_DASHBOARD(
        route = "contractor_dashboard",
        labelRes = R.string.nav_contractor_dashboard,
        selectedIcon = Icons.Rounded.Engineering,
        unselectedIcon = Icons.Outlined.Engineering,
        navTestTag = "nav_contractor_dashboard"
    );

    companion object {
        fun fromRoute(route: String?): AppDestination = when {
            route == null -> MAINTENANCE_REQUESTS
            route == MAINTENANCE_REQUESTS.route || route.startsWith("${MAINTENANCE_REQUESTS.route}/") ->
                MAINTENANCE_REQUESTS
            route == PAYMENT_COLLECTION.route -> PAYMENT_COLLECTION
            route == CONTRACTOR_DASHBOARD.route -> CONTRACTOR_DASHBOARD
            else -> MAINTENANCE_REQUESTS
        }
    }
}

@Composable
fun ShafiHomesNavHost(
    navController: NavHostController,
    state: SocietyDashboardState,
    onSubmitNewRequest: (
        unitLocation: String,
        title: String,
        description: String,
        category: TradeCategory,
        priority: TicketPriority,
        estimatedCost: Double,
        photoLabel: String?,
        photoUri: String?,
        autoScheduleNow: Boolean
    ) -> Unit,
    onAutoScheduleTicket: (RepairTicket) -> Unit,
    onAutoScheduleAll: () -> Unit,
    onUpdateRepairProgress: (RepairTicket, TicketStatus, Double, String?) -> Unit,
    onDeleteTicket: (String) -> Unit,
    onOpenNewDue: () -> Unit,
    onRecordPayment: (MaintenanceDue, PaymentMethod, String, String?) -> Unit,
    onMarkOverdue: (MaintenanceDue, Double) -> Unit,
    onDeleteDue: (String) -> Unit,
    onAdvanceScheduleEntry: (ScheduleEntry, ScheduleStatus, Double?, String?) -> Unit,
    onRescheduleEntry: (ScheduleEntry, Long, ScheduleSlot) -> Unit,
    onOpenAddContractor: () -> Unit,
    onToggleContractorAvailable: (String, Boolean) -> Unit,
    onDeleteContractor: (String) -> Unit,
    modifier: Modifier = Modifier,
    startDestination: AppDestination = AppDestination.MAINTENANCE_REQUESTS
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        navigation(
            startDestination = MaintenanceRequestsRoutes.LIST,
            route = AppDestination.MAINTENANCE_REQUESTS.route
        ) {
            composable(MaintenanceRequestsRoutes.LIST) {
                RepairLogsScreen(
                    state = state,
                    onOpenLogRepair = {
                        navController.navigate(MaintenanceRequestsRoutes.NEW_REQUEST) {
                            launchSingleTop = true
                        }
                    },
                    onAutoScheduleTicket = onAutoScheduleTicket,
                    onAutoScheduleAll = onAutoScheduleAll,
                    onUpdateProgress = onUpdateRepairProgress,
                    onDeleteTicket = onDeleteTicket
                )
            }
            composable(MaintenanceRequestsRoutes.NEW_REQUEST) {
                NewMaintenanceRequestScreen(
                    defaultUnit = state.profile?.unitLabel ?: "Tower A - 101",
                    onCancel = { navController.popBackStack() },
                    onSubmit = { unit, title, desc, cat, priority, est, photoLabel, photoUri, autoSched ->
                        onSubmitNewRequest(unit, title, desc, cat, priority, est, photoLabel, photoUri, autoSched)
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(AppDestination.PAYMENT_COLLECTION.route) {
            MaintenanceDuesScreen(
                state = state,
                onOpenNewDue = onOpenNewDue,
                onRecordPayment = onRecordPayment,
                onMarkOverdue = onMarkOverdue,
                onDeleteDue = onDeleteDue
            )
        }
        composable(AppDestination.CONTRACTOR_DASHBOARD.route) {
            ScheduleScreen(
                state = state,
                onRunAutoScheduler = onAutoScheduleAll,
                onAdvanceEntry = onAdvanceScheduleEntry,
                onRescheduleEntry = onRescheduleEntry,
                onOpenAddContractor = onOpenAddContractor,
                onToggleContractorAvailable = onToggleContractorAvailable,
                onDeleteContractor = onDeleteContractor
            )
        }
    }
}
