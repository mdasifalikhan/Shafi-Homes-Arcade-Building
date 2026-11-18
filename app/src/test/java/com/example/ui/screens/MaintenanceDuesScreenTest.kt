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
import com.example.data.model.DueStatus
import com.example.data.model.MaintenanceDue
import com.example.data.model.PaymentMethod
import com.example.ui.SocietyDashboardState
import com.example.ui.SocietyMetrics
import com.example.ui.theme.CivicNestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MaintenanceDuesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleDues = listOf(
        MaintenanceDue(
            id = "d_paid",
            unitNumber = "A-101",
            residentName = "Aarav Mehta",
            billingPeriod = "Sep 2026",
            baseAmount = 3000.0,
            sinkingFund = 500.0,
            utilityAmount = 450.0,
            lateFee = 0.0,
            totalAmount = 3950.0,
            status = DueStatus.PAID.name,
            paymentMethod = PaymentMethod.UPI.name,
            receiptNumber = "RCP-A101-8421",
            dueDateEpochDay = 20700L,
            paidAtEpochMillis = 1789200000000L,
            createdAtMillis = 1789000000000L
        ),
        MaintenanceDue(
            id = "d_overdue",
            unitNumber = "B-204",
            residentName = "Nadia Khan",
            billingPeriod = "Oct 2026",
            baseAmount = 3200.0,
            sinkingFund = 500.0,
            utilityAmount = 400.0,
            lateFee = 250.0,
            totalAmount = 4350.0,
            status = DueStatus.OVERDUE.name,
            dueDateEpochDay = 20730L,
            createdAtMillis = 1791500000000L
        ),
        MaintenanceDue(
            id = "d_pending",
            unitNumber = "C-302",
            residentName = "Rohan Das",
            billingPeriod = "Nov 2026",
            baseAmount = 3500.0,
            sinkingFund = 500.0,
            utilityAmount = 450.0,
            lateFee = 0.0,
            totalAmount = 4450.0,
            status = DueStatus.PENDING.name,
            dueDateEpochDay = 20760L,
            createdAtMillis = 1794000000000L
        )
    )

    @Test
    fun `payment history view displays past maintenance dues, transaction statuses, and filters`() {
        var recordedDueId: String? = null
        var recordedMethod: PaymentMethod? = null

        composeTestRule.setContent {
            CivicNestTheme {
                MaintenanceDuesScreen(
                    state = SocietyDashboardState(
                        dues = sampleDues,
                        metrics = SocietyMetrics(
                            collectedAmount = 3950.0,
                            pendingAmount = 4450.0,
                            overdueAmount = 4350.0,
                            totalBilledAmount = 12750.0,
                            collectionRatePercent = 31
                        )
                    ),
                    onOpenNewDue = {},
                    onRecordPayment = { due, method, _, _ ->
                        recordedDueId = due.id
                        recordedMethod = method
                    },
                    onMarkOverdue = { _, _ -> },
                    onDeleteDue = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("payment_collection_tabs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("payment_tab_dues").assertIsSelected()

        // Switch to Payment History tab within Payment Collection
        composeTestRule.onNodeWithTag("payment_tab_history").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("payment_tab_history").assertIsSelected()

        val historyList = composeTestRule.onNodeWithTag("payment_history_list")
        historyList.assertIsDisplayed()
        composeTestRule.onNodeWithTag("payment_history_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Payment History").assertIsDisplayed()

        // Verify all 3 past dues & transaction statuses appear in Payment History
        historyList.performScrollToNode(hasTestTag("history_item_d_pending"))
        composeTestRule.onNodeWithText("Nov 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rohan Das").assertIsDisplayed()

        historyList.performScrollToNode(hasTestTag("history_item_d_overdue"))
        composeTestRule.onNodeWithText("Oct 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nadia Khan").assertIsDisplayed()

        historyList.performScrollToNode(hasTestTag("history_item_d_paid"))
        composeTestRule.onNodeWithText("Sep 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("RCP-A101-8421").assertIsDisplayed()

        // Filter Payment History by Paid
        composeTestRule.onNodeWithTag("history_filter_chip_paid").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("history_filter_chip_paid").assertIsSelected()
        historyList.performScrollToNode(hasTestTag("history_item_d_paid"))
        composeTestRule.onNodeWithText("Sep 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("Oct 2026").assertDoesNotExist()
        composeTestRule.onNodeWithText("Nov 2026").assertDoesNotExist()

        // Tap paid history item to inspect transaction details
        composeTestRule.onNodeWithTag("history_item_d_paid").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Transaction Completed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Method: UPI Instant").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.waitForIdle()

        // Filter Payment History by Overdue and record payment directly from history
        composeTestRule.onNodeWithTag("history_filter_chip_overdue").performClick()
        composeTestRule.waitForIdle()
        historyList.performScrollToNode(hasTestTag("history_item_d_overdue"))
        composeTestRule.onNodeWithText("Oct 2026").assertIsDisplayed()
        composeTestRule.onNodeWithTag("history_pay_d_overdue").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Confirm Paid").performClick()
        composeTestRule.waitForIdle()

        assertEquals("d_overdue", recordedDueId)
        assertEquals(PaymentMethod.UPI, recordedMethod)

        // Reset filter to All
        composeTestRule.onNodeWithTag("history_filter_chip_all").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("history_filter_chip_all").assertIsSelected()
        historyList.performScrollToNode(hasText("Nov 2026"))
        composeTestRule.onNodeWithText("Nov 2026").assertIsDisplayed()
    }
}
