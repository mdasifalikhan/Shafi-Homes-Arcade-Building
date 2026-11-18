package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.SocietyBottomNavigation
import com.example.data.model.TicketPriority
import com.example.ui.SocietyDashboardState
import com.example.ui.theme.CivicNestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `bottomNavigation switches NavHost and opens New Maintenance Request form screen`() {
        lateinit var navController: NavHostController
        var submittedDescription = ""
        var submittedPriority: TicketPriority? = null

        composeTestRule.setContent {
            navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val navDestination = backStackEntry?.destination
            CivicNestTheme {
                Scaffold(
                    bottomBar = {
                        SocietyBottomNavigation(
                            isSelected = { destination ->
                                navDestination?.hierarchy?.any { it.route == destination.route } == true ||
                                    (navDestination == null && destination == AppDestination.MAINTENANCE_REQUESTS)
                            },
                            onSelectDestination = { destination ->
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    ShafiHomesNavHost(
                        navController = navController,
                        state = SocietyDashboardState(),
                        onSubmitNewRequest = { _, _, desc, _, priority, _, _, _, _ ->
                            submittedDescription = desc
                            submittedPriority = priority
                        },
                        onAutoScheduleTicket = {},
                        onAutoScheduleAll = {},
                        onUpdateRepairProgress = { _, _, _, _ -> },
                        onDeleteTicket = {},
                        onOpenNewDue = {},
                        onRecordPayment = { _, _, _, _ -> },
                        onMarkOverdue = { _, _ -> },
                        onDeleteDue = {},
                        onAdvanceScheduleEntry = { _, _, _, _ -> },
                        onRescheduleEntry = { _, _, _ -> },
                        onOpenAddContractor = {},
                        onToggleContractorAvailable = { _, _ -> },
                        onDeleteContractor = {},
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("society_bottom_navigation").assertIsDisplayed()
        composeTestRule.onNodeWithTag(AppDestination.MAINTENANCE_REQUESTS.navTestTag).assertIsSelected()
        assertEquals(
            MaintenanceRequestsRoutes.LIST,
            navController.currentBackStackEntry?.destination?.route
        )

        // Open the 'New Maintenance Request' form screen within 'Maintenance Requests'
        composeTestRule.onNodeWithTag("repairs_header_log_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(
            MaintenanceRequestsRoutes.NEW_REQUEST,
            navController.currentBackStackEntry?.destination?.route
        )
        // Parent Maintenance Requests tab remains selected in BottomNavigation
        composeTestRule.onNodeWithTag(AppDestination.MAINTENANCE_REQUESTS.navTestTag).assertIsSelected()

        // Verify form fields: issue description, priority level, and image attachment placeholder
        composeTestRule.onNodeWithText("New Maintenance Request").assertIsDisplayed()
        composeTestRule.onNodeWithTag("new_request_title_input").performTextInput("Corridor light flicker")
        composeTestRule.onNodeWithTag("new_request_description_input")
            .performTextInput("Third floor lobby ceiling fixture flickers continuously")
        composeTestRule.onNodeWithTag("priority_chip_high").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("image_attachment_placeholder").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("submit_new_request_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals("Third floor lobby ceiling fixture flickers continuously", submittedDescription)
        assertEquals(TicketPriority.HIGH, submittedPriority)
        assertEquals(
            MaintenanceRequestsRoutes.LIST,
            navController.currentBackStackEntry?.destination?.route
        )

        // Switch via BottomNavigation to Payment Collection & Contractor Dashboard
        composeTestRule.onNodeWithTag(AppDestination.PAYMENT_COLLECTION.navTestTag).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AppDestination.PAYMENT_COLLECTION.navTestTag).assertIsSelected()
        assertEquals(
            AppDestination.PAYMENT_COLLECTION.route,
            navController.currentBackStackEntry?.destination?.route
        )
        composeTestRule.onNodeWithTag("dues_header_new_bill_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag(AppDestination.CONTRACTOR_DASHBOARD.navTestTag).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AppDestination.CONTRACTOR_DASHBOARD.navTestTag).assertIsSelected()
        assertEquals(
            AppDestination.CONTRACTOR_DASHBOARD.route,
            navController.currentBackStackEntry?.destination?.route
        )
        composeTestRule.onNodeWithTag("schedule_tab_dispatch").assertIsDisplayed()
    }
}
