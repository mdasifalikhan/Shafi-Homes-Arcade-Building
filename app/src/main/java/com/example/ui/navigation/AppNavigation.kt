package com.example.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.R
import com.example.data.model.MaintenanceDue
import com.example.data.model.NoticeCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.RepairTicket
import com.example.data.model.ScheduleEntry
import com.example.data.model.ScheduleSlot
import com.example.data.model.ScheduleStatus
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TradeCategory
import com.example.data.model.VisitorPurpose
import com.example.data.model.VisitorStatus
import com.example.ui.SocietyDashboardState
import com.example.ui.screens.AmenityBookingScreen
import com.example.ui.screens.EmergencySosScreen
import com.example.ui.screens.HelperStaffScreen
import com.example.ui.screens.MaintenanceDuesScreen
import com.example.ui.screens.NewMaintenanceRequestScreen
import com.example.ui.screens.NoticeBoardScreen
import com.example.ui.screens.RepairLogsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SecurityTerminalScreen
import com.example.ui.screens.ServicesHubScreen
import com.example.ui.screens.SocietyDirectoryScreen
import com.example.ui.screens.SocietyPollsScreen
import com.example.ui.screens.VisitorPassScreen

object MaintenanceRequestsRoutes {
    const val LIST = "maintenance_requests/list"
    const val NEW_REQUEST = "maintenance_requests/new"
}

object CommunityRoutes {
    const val HUB = "community_hub"
    const val AMENITIES = "community/amenities"
    const val EMERGENCY_SOS = "community/emergency_sos"
    const val DIRECTORY = "community/directory"
    const val POLLS = "community/polls"
    const val HELPERS = "community/helpers"
    const val CONTRACTORS = "community/contractors"
    const val SECURITY_TERMINAL = "community/security_terminal"
}

enum class AppDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val navTestTag: String
) {
    NOTICE_BOARD(
        route = "notice_board",
        labelRes = R.string.nav_notice_board,
        selectedIcon = Icons.Rounded.Campaign,
        unselectedIcon = Icons.Outlined.Campaign,
        navTestTag = "nav_notice_board"
    ),
    VISITOR_PASS(
        route = "visitor_pass",
        labelRes = R.string.nav_visitor_pass,
        selectedIcon = Icons.Rounded.Shield,
        unselectedIcon = Icons.Outlined.Shield,
        navTestTag = "nav_visitor_pass"
    ),
    COMMUNITY_HUB(
        route = CommunityRoutes.HUB,
        labelRes = R.string.nav_community_hub,
        selectedIcon = Icons.Rounded.GridView,
        unselectedIcon = Icons.Outlined.GridView,
        navTestTag = "nav_community_hub"
    ),
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
    );

    companion object {
        fun fromRoute(route: String?): AppDestination = when {
            route == null -> NOTICE_BOARD
            route == NOTICE_BOARD.route -> NOTICE_BOARD
            route == VISITOR_PASS.route -> VISITOR_PASS
            route == COMMUNITY_HUB.route || (route.startsWith("community/")) -> COMMUNITY_HUB
            route == MAINTENANCE_REQUESTS.route || route.startsWith("${MAINTENANCE_REQUESTS.route}/") ->
                MAINTENANCE_REQUESTS
            route == PAYMENT_COLLECTION.route -> PAYMENT_COLLECTION
            else -> NOTICE_BOARD
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
    onPostNotice: (title: String, content: String, category: NoticeCategory, author: String, isPinned: Boolean) -> Unit,
    onDeleteNotice: (String) -> Unit,
    onCreateVisitorPass: (visitorName: String, phone: String?, purpose: VisitorPurpose, unitNumber: String, vehicleNumber: String?) -> Unit,
    onUpdateVisitorStatus: (passId: String, nextStatus: VisitorStatus) -> Unit,
    onDeleteVisitorPass: (String) -> Unit,
    onBookAmenity: (
        amenityId: String,
        amenityName: String,
        residentName: String,
        unitLabel: String,
        dateEpochDay: Long,
        timeSlot: String,
        purpose: String
    ) -> Unit,
    onCancelAmenityBooking: (bookingId: String) -> Unit,
    onCastPollVote: (pollId: String, optionIndex: Int) -> Unit,
    onCreatePoll: (question: String, description: String, options: List<String>) -> Unit,
    onToggleHelperStatus: (helperId: String, nextInPremises: Boolean) -> Unit,
    onAddDomesticHelper: (name: String, serviceType: String, phone: String, assignedUnits: String) -> Unit,
    onAddDirectoryEntry: (name: String, unitNumber: String, role: String, category: String, phone: String, email: String?) -> Unit,
    modifier: Modifier = Modifier,
    startDestination: AppDestination = AppDestination.NOTICE_BOARD
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        // 1. Notice Board
        composable(AppDestination.NOTICE_BOARD.route) {
            NoticeBoardScreen(
                state = state,
                onPostNotice = onPostNotice,
                onDeleteNotice = onDeleteNotice,
                onNavigateToAmenities = { navController.navigate(CommunityRoutes.AMENITIES) },
                onNavigateToEmergencySos = { navController.navigate(CommunityRoutes.EMERGENCY_SOS) },
                onNavigateToDirectory = { navController.navigate(CommunityRoutes.DIRECTORY) },
                onNavigateToPolls = { navController.navigate(CommunityRoutes.POLLS) },
                onNavigateToHelpers = { navController.navigate(CommunityRoutes.HELPERS) },
                onNavigateToHub = { navController.navigate(AppDestination.COMMUNITY_HUB.route) },
                onNavigateToSecurityTerminal = { navController.navigate(CommunityRoutes.SECURITY_TERMINAL) }
            )
        }

        // 2. Visitor Pass
        composable(AppDestination.VISITOR_PASS.route) {
            VisitorPassScreen(
                state = state,
                onCreatePass = onCreateVisitorPass,
                onUpdateStatus = onUpdateVisitorStatus,
                onDeletePass = onDeleteVisitorPass
            )
        }

        // 3. Central Services Hub
        composable(AppDestination.COMMUNITY_HUB.route) {
            ServicesHubScreen(
                state = state,
                onNavigateToAmenities = { navController.navigate(CommunityRoutes.AMENITIES) },
                onNavigateToEmergencySos = { navController.navigate(CommunityRoutes.EMERGENCY_SOS) },
                onNavigateToDirectory = { navController.navigate(CommunityRoutes.DIRECTORY) },
                onNavigateToPolls = { navController.navigate(CommunityRoutes.POLLS) },
                onNavigateToHelpers = { navController.navigate(CommunityRoutes.HELPERS) },
                onNavigateToContractors = { navController.navigate(CommunityRoutes.CONTRACTORS) },
                onNavigateToVisitorPass = { navController.navigate(AppDestination.VISITOR_PASS.route) },
                onNavigateToSecurityTerminal = { navController.navigate(CommunityRoutes.SECURITY_TERMINAL) }
            )
        }

        // 3a. Amenity Booking
        composable(CommunityRoutes.AMENITIES) {
            AmenityBookingScreen(
                amenities = state.amenities,
                bookings = state.amenityBookings,
                defaultResidentName = state.profile?.residentName ?: "Resident",
                defaultUnitLabel = state.profile?.unitLabel ?: "Tower A - 101",
                onBookAmenity = onBookAmenity,
                onCancelBooking = onCancelAmenityBooking
            )
        }

        // 3b. Emergency SOS Hub
        composable(CommunityRoutes.EMERGENCY_SOS) {
            EmergencySosScreen(
                contacts = state.emergencyContacts,
                societyName = state.profile?.societyName ?: "Shafi Homes Arcade",
                unitLabel = state.profile?.unitLabel ?: "Tower A - 101",
                residentName = state.profile?.residentName ?: "Resident"
            )
        }

        // 3c. Society Directory
        composable(CommunityRoutes.DIRECTORY) {
            SocietyDirectoryScreen(
                directory = state.directory,
                onAddEntry = onAddDirectoryEntry
            )
        }

        // 3d. Society Polls
        composable(CommunityRoutes.POLLS) {
            SocietyPollsScreen(
                polls = state.polls,
                onCastVote = onCastPollVote,
                onCreatePoll = onCreatePoll
            )
        }

        // 3e. Domestic Staff & Helpers
        composable(CommunityRoutes.HELPERS) {
            HelperStaffScreen(
                helpers = state.helpers,
                onToggleStatus = onToggleHelperStatus,
                onAddHelper = onAddDomesticHelper
            )
        }

        // 3f. Contractor Dashboard & Schedule
        composable(CommunityRoutes.CONTRACTORS) {
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

        // 3g. Security Guard Gate Terminal
        composable(CommunityRoutes.SECURITY_TERMINAL) {
            SecurityTerminalScreen(
                state = state,
                onUpdateVisitorStatus = onUpdateVisitorStatus,
                onCreateWalkInPass = onCreateVisitorPass,
                onToggleHelperStatus = onToggleHelperStatus
            )
        }

        // 4. Maintenance Requests
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

        // 5. Payment Collection
        composable(AppDestination.PAYMENT_COLLECTION.route) {
            MaintenanceDuesScreen(
                state = state,
                onOpenNewDue = onOpenNewDue,
                onRecordPayment = onRecordPayment,
                onMarkOverdue = onMarkOverdue,
                onDeleteDue = onDeleteDue
            )
        }
    }
}
