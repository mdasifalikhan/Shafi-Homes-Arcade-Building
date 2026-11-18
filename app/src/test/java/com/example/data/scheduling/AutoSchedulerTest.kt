package com.example.data.scheduling

import com.example.data.model.Contractor
import com.example.data.model.RepairTicket
import com.example.data.model.ScheduleEntry
import com.example.data.model.ScheduleSlot
import com.example.data.model.ScheduleStatus
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TradeCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoSchedulerTest {

    @Test
    fun `emergency tickets schedule first and avoid double-booking standard slots`() {
        val today = 20410L
        val contractors = listOf(
            Contractor(
                id = "c_plumb_1",
                name = "Ramesh",
                company = "AquaFix",
                specialty = TradeCategory.PLUMBING.name,
                shiftStartHour = 8L,
                shiftEndHour = 20L,
                dailyCapacity = 3L,
                hourlyRate = 40.0,
                available = true
            )
        )
        val tickets = listOf(
            RepairTicket(
                id = "t_low",
                unitLocation = "A-101",
                title = "Tap washer drip",
                description = "Kitchen sink slow drip",
                category = TradeCategory.PLUMBING.name,
                priority = TicketPriority.LOW.name,
                status = TicketStatus.LOGGED.name
            ),
            RepairTicket(
                id = "t_emerg",
                unitLocation = "B-Pump",
                title = "Main riser burst",
                description = "Water pressure spike in basement shaft",
                category = TradeCategory.PLUMBING.name,
                priority = TicketPriority.EMERGENCY.name,
                status = TicketStatus.LOGGED.name
            )
        )

        val plan = AutoScheduler.buildSchedulePlan(
            tickets = tickets,
            contractors = contractors,
            existingEntries = emptyList(),
            todayEpochDay = today
        )

        assertEquals(2, plan.proposals.size)
        assertEquals("t_emerg", plan.proposals[0].ticket.id)
        assertEquals(ScheduleSlot.EMERGENCY_NOW, plan.proposals[0].slot)
        assertEquals("t_low", plan.proposals[1].ticket.id)
        assertEquals(today, plan.proposals[1].epochDay)
    }

    @Test
    fun `respects daily capacity and existing bookings`() {
        val today = 20410L
        val contractors = listOf(
            Contractor(
                id = "c_elec_1",
                name = "Vikram",
                company = "VoltCare",
                specialty = TradeCategory.ELECTRICAL.name,
                shiftStartHour = 9L,
                shiftEndHour = 18L,
                dailyCapacity = 1L,
                hourlyRate = 50.0,
                available = true
            )
        )
        val existing = listOf(
            ScheduleEntry(
                id = "s1",
                ticketId = "t0",
                ticketTitle = "Existing job",
                unitLocation = "C-202",
                contractorId = "c_elec_1",
                contractorName = "Vikram",
                specialty = TradeCategory.ELECTRICAL.name,
                priority = TicketPriority.MEDIUM.name,
                scheduledDateEpochDay = today,
                timeSlot = ScheduleSlot.MORNING_09_12.name,
                status = ScheduleStatus.SCHEDULED.name
            )
        )
        val tickets = listOf(
            RepairTicket(
                id = "t1",
                unitLocation = "A-301",
                title = "Breaker trip",
                description = "Lobby breaker keeps tripping",
                category = TradeCategory.ELECTRICAL.name,
                priority = TicketPriority.HIGH.name,
                status = TicketStatus.LOGGED.name
            )
        )

        val plan = AutoScheduler.buildSchedulePlan(
            tickets = tickets,
            contractors = contractors,
            existingEntries = existing,
            todayEpochDay = today
        )

        assertEquals(1, plan.proposals.size)
        assertEquals(today + 1L, plan.proposals[0].epochDay)
        assertTrue(plan.unscheduledReasons.isEmpty())
    }
}
