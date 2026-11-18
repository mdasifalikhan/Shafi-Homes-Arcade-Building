package com.example.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.data.model.RepairTicket
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TradeCategory
import com.example.ui.SocietyDashboardState
import com.example.ui.theme.CivicNestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RepairLogsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleTickets = listOf(
        RepairTicket(
            id = "t1",
            unitLocation = "A-101",
            title = "Kitchen sink valve drip",
            description = "Leaking under sink cabinet",
            category = TradeCategory.PLUMBING.name,
            priority = TicketPriority.MEDIUM.name,
            status = TicketStatus.LOGGED.name
        ),
        RepairTicket(
            id = "t2",
            unitLocation = "Lobby B",
            title = "Lift fan noise",
            description = "Ventilation fan vibrating",
            category = TradeCategory.ELEVATOR.name,
            priority = TicketPriority.HIGH.name,
            status = TicketStatus.IN_PROGRESS.name
        ),
        RepairTicket(
            id = "t3",
            unitLocation = "C-302",
            title = "Gate intercom audio",
            description = "Replaced intercom speaker unit",
            category = TradeCategory.SECURITY.name,
            priority = TicketPriority.LOW.name,
            status = TicketStatus.COMPLETED.name
        )
    )

    @Test
    fun `top status filter chips filter maintenance requests by Pending, In Progress, Completed, and All`() {
        composeTestRule.setContent {
            CivicNestTheme {
                RepairLogsScreen(
                    state = SocietyDashboardState(tickets = sampleTickets),
                    onOpenLogRepair = {},
                    onAutoScheduleTicket = {},
                    onUpdateProgress = { _, _, _, _ -> },
                    onDeleteTicket = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        val listNode = composeTestRule.onNodeWithTag("repairs_lazy_column")
        composeTestRule.onNodeWithTag("requests_filter_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("requests_filter_chip_all").assertIsSelected()

        // Filter to Pending via top FilterChip
        composeTestRule.onNodeWithTag("requests_filter_chip_pending").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("requests_filter_chip_pending").assertIsSelected()
        listNode.performScrollToNode(hasText("Kitchen sink valve drip"))
        composeTestRule.onNodeWithText("Kitchen sink valve drip").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lift fan noise").assertDoesNotExist()
        composeTestRule.onNodeWithText("Gate intercom audio").assertDoesNotExist()

        // Filter to In Progress via top FilterChip
        composeTestRule.onNodeWithTag("requests_filter_chip_in_progress").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("requests_filter_chip_in_progress").assertIsSelected()
        listNode.performScrollToNode(hasText("Lift fan noise"))
        composeTestRule.onNodeWithText("Lift fan noise").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kitchen sink valve drip").assertDoesNotExist()

        // Filter to Completed via top FilterChip
        composeTestRule.onNodeWithTag("requests_filter_chip_completed").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("requests_filter_chip_completed").assertIsSelected()
        listNode.performScrollToNode(hasText("Gate intercom audio"))
        composeTestRule.onNodeWithText("Gate intercom audio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lift fan noise").assertDoesNotExist()

        // Reset to All via top FilterChip
        composeTestRule.onNodeWithTag("requests_filter_chip_all").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("requests_filter_chip_all").assertIsSelected()
        listNode.performScrollToNode(hasText("Kitchen sink valve drip"))
        composeTestRule.onNodeWithText("Kitchen sink valve drip").assertIsDisplayed()
    }

    @Test
    fun `request status section displays Pending, In Progress, and Completed requests and filters list`() {
        composeTestRule.setContent {
            CivicNestTheme {
                RepairLogsScreen(
                    state = SocietyDashboardState(tickets = sampleTickets),
                    onOpenLogRepair = {},
                    onAutoScheduleTicket = {},
                    onUpdateProgress = { _, _, _, _ -> },
                    onDeleteTicket = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        val listNode = composeTestRule.onNodeWithTag("repairs_lazy_column")
        listNode.performScrollToNode(hasTestTag("request_status_section"))
        composeTestRule.onNodeWithText("Request Status").assertIsDisplayed()
        composeTestRule.onNodeWithTag("request_status_filter_pending").assertIsDisplayed()
        composeTestRule.onNodeWithTag("request_status_filter_in_progress").assertIsDisplayed()
        composeTestRule.onNodeWithTag("request_status_filter_completed").assertIsDisplayed()

        // Filter to In Progress via Request Status section tile
        composeTestRule.onNodeWithTag("request_status_filter_in_progress").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("requests_filter_chip_in_progress").assertIsSelected()
        listNode.performScrollToNode(hasText("Lift fan noise"))
        composeTestRule.onNodeWithText("Lift fan noise").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kitchen sink valve drip").assertDoesNotExist()

        // Filter to Completed via Request Status section tile
        listNode.performScrollToNode(hasTestTag("request_status_section"))
        composeTestRule.onNodeWithTag("request_status_filter_completed").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("requests_filter_chip_completed").assertIsSelected()
        listNode.performScrollToNode(hasText("Gate intercom audio"))
        composeTestRule.onNodeWithText("Gate intercom audio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lift fan noise").assertDoesNotExist()

        // Reset to All via Request Status section
        listNode.performScrollToNode(hasTestTag("request_status_section"))
        composeTestRule.onNodeWithTag("request_status_filter_all").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("requests_filter_chip_all").assertIsSelected()
        listNode.performScrollToNode(hasText("Kitchen sink valve drip"))
        composeTestRule.onNodeWithText("Kitchen sink valve drip").assertIsDisplayed()
    }

    @Test
    fun `search bar filters maintenance requests by description and clears`() {
        composeTestRule.setContent {
            CivicNestTheme {
                RepairLogsScreen(
                    state = SocietyDashboardState(tickets = sampleTickets),
                    onOpenLogRepair = {},
                    onAutoScheduleTicket = {},
                    onUpdateProgress = { _, _, _, _ -> },
                    onDeleteTicket = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        val listNode = composeTestRule.onNodeWithTag("repairs_lazy_column")
        val searchBar = composeTestRule.onNodeWithTag("requests_search_bar")
        searchBar.assertIsDisplayed()

        // Search by phrase in ticket description ("speaker unit")
        searchBar.performTextInput("speaker unit")
        composeTestRule.waitForIdle()
        listNode.performScrollToNode(hasText("Gate intercom audio"))
        composeTestRule.onNodeWithText("Gate intercom audio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kitchen sink valve drip").assertDoesNotExist()
        composeTestRule.onNodeWithText("Lift fan noise").assertDoesNotExist()

        // Combining search with mismatched status filter shows empty state with reset
        composeTestRule.onNodeWithTag("requests_filter_chip_pending").performClick()
        composeTestRule.waitForIdle()
        listNode.performScrollToNode(hasText("No Matching Requests"))
        composeTestRule.onNodeWithText("No Matching Requests").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear Search & Filters").performClick()
        composeTestRule.waitForIdle()

        // Both search and status filter reset cleanly
        composeTestRule.onNodeWithTag("requests_filter_chip_all").assertIsSelected()
        listNode.performScrollToNode(hasText("Kitchen sink valve drip"))
        composeTestRule.onNodeWithText("Kitchen sink valve drip").assertIsDisplayed()

        // Re-search and clear via trailing close button
        searchBar.performTextInput("vibrating")
        composeTestRule.waitForIdle()
        listNode.performScrollToNode(hasText("Lift fan noise"))
        composeTestRule.onNodeWithText("Lift fan noise").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gate intercom audio").assertDoesNotExist()

        composeTestRule.onNodeWithTag("requests_search_clear_button").performClick()
        composeTestRule.waitForIdle()
        listNode.performScrollToNode(hasText("Gate intercom audio"))
        composeTestRule.onNodeWithText("Gate intercom audio").assertIsDisplayed()
    }
}
