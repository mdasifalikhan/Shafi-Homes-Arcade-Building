package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SocietyDatabase
import com.example.data.local.SocietyRepository
import com.example.data.model.PaymentMethod
import com.example.data.model.SocietyRole
import com.example.data.model.TicketPriority
import com.example.data.model.TradeCategory
import com.example.data.scheduling.AutoScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Shafi Homes Arcade", appName)
  }

  @Test
  fun `local society workflow persists dues repairs and auto-schedule without sign-in`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, SocietyDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    try {
      val repo = SocietyRepository(db.societyDao())
      repo.saveUserProfile(
        societyName = "Shafi Homes Arcade",
        residentName = "Amina Khan",
        unitLabel = "Tower B - 204",
        defaultMonthlyFee = 4000.0,
        activeRole = SocietyRole.MANAGER
      ).getOrThrow()

      repo.createMaintenanceDue(
        unitNumber = "Tower B - 204",
        residentName = "Amina Khan",
        billingPeriod = "November 2026",
        baseAmount = 4000.0,
        sinkingFund = 500.0,
        utilityAmount = 300.0,
        dueDateEpochDay = 20770L
      ).getOrThrow()

      val due = repo.observeMaintenanceDues().first().single()
      repo.recordDuePayment(due, PaymentMethod.UPI, "REC-101", "Paid on time").getOrThrow()

      repo.createContractor(
        name = "Imran Ali",
        company = "Arcade Plumbing Co",
        specialty = TradeCategory.PLUMBING,
        phoneSuffix = "4412",
        shiftStartHour = 9L,
        shiftEndHour = 18L,
        dailyCapacity = 3L,
        hourlyRate = 650.0
      ).getOrThrow()

      val ticket = repo.createRepairTicket(
        unitLocation = "Tower B - 204",
        title = "Riser valve leak",
        description = "Steady drip near main shaft riser valve",
        category = TradeCategory.PLUMBING,
        priority = TicketPriority.HIGH,
        estimatedCost = 1200.0
      ).getOrThrow()

      val contractors = repo.observeContractors().first()
      val plan = AutoScheduler.buildSchedulePlan(
        tickets = listOf(ticket),
        contractors = contractors,
        existingEntries = emptyList(),
        todayEpochDay = 20775L
      )
      assertEquals(1, plan.proposals.size)
      repo.applyScheduleProposals(plan.proposals).getOrThrow()

      val schedules = repo.observeScheduleEntries().first()
      assertEquals(1, schedules.size)
      assertEquals("Imran Ali", schedules.first().contractorName)
      assertTrue(repo.observeUserProfile().first()?.societyName == "Shafi Homes Arcade")
    } finally {
      db.close()
    }
  }
}
