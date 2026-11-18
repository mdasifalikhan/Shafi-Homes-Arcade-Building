package com.example.data.scheduling

import com.example.data.model.Contractor
import com.example.data.model.RepairTicket
import com.example.data.model.ScheduleEntry
import com.example.data.model.ScheduleSlot
import com.example.data.model.ScheduleStatus
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import java.util.concurrent.TimeUnit

data class ScheduleProposal(
    val ticket: RepairTicket,
    val contractor: Contractor,
    val epochDay: Long,
    val slot: ScheduleSlot,
    val matchReason: String
)

data class AutoSchedulePlan(
    val proposals: List<ScheduleProposal>,
    val unscheduledReasons: Map<String, String>
)

object AutoScheduler {

    fun currentEpochDay(nowMillis: Long = System.currentTimeMillis()): Long =
        TimeUnit.MILLISECONDS.toDays(nowMillis)

    /**
     * Deterministically assigns unscheduled LOGGED repair tickets to available contractors
     * by matching trade specialty, respecting contractor shift hours, avoiding slot double-bookings,
     * honoring daily capacity caps, and prioritizing EMERGENCY/HIGH tickets into earliest windows.
     */
    fun buildSchedulePlan(
        tickets: List<RepairTicket>,
        contractors: List<Contractor>,
        existingEntries: List<ScheduleEntry>,
        todayEpochDay: Long = currentEpochDay(),
        horizonDays: Int = 7
    ): AutoSchedulePlan {
        val activeTicketIds = existingEntries
            .filter { it.statusEnum != ScheduleStatus.CANCELLED && it.statusEnum != ScheduleStatus.DONE }
            .map { it.ticketId }
            .toSet()

        val pendingTickets = tickets
            .filter { it.statusEnum == TicketStatus.LOGGED && it.id.isNotBlank() && it.id !in activeTicketIds }
            .sortedWith(
                compareByDescending<RepairTicket> { it.priorityEnum.rank }
                    .thenBy { it.createdAtMillis }
            )

        if (pendingTickets.isEmpty()) {
            return AutoSchedulePlan(emptyList(), emptyMap())
        }

        // Track mutable simulated bookings so batch proposals never double-book
        val bookedSlotsByContractorDay = mutableMapOf<Pair<String, Long>, MutableSet<ScheduleSlot>>()
        val bookedCountByContractorDay = mutableMapOf<Pair<String, Long>, Int>()

        existingEntries
            .filter { it.statusEnum != ScheduleStatus.CANCELLED && it.statusEnum != ScheduleStatus.DONE }
            .forEach { entry ->
                val key = entry.contractorId to entry.scheduledDateEpochDay
                bookedSlotsByContractorDay.getOrPut(key) { mutableSetOf() }.add(entry.slotEnum)
                bookedCountByContractorDay[key] = (bookedCountByContractorDay[key] ?: 0) + 1
            }

        val proposals = mutableListOf<ScheduleProposal>()
        val unscheduledReasons = mutableMapOf<String, String>()

        for (ticket in pendingTickets) {
            val matchingContractors = contractors.filter {
                it.available && it.specialty == ticket.category && it.id.isNotBlank()
            }

            if (matchingContractors.isEmpty()) {
                val anyInTrade = contractors.any { it.specialty == ticket.category }
                unscheduledReasons[ticket.id] = if (anyInTrade) {
                    "All ${ticket.categoryEnum.label} contractors are currently paused/unavailable."
                } else {
                    "No ${ticket.categoryEnum.label} contractor registered yet."
                }
                continue
            }

            val isEmergency = ticket.priorityEnum == TicketPriority.EMERGENCY
            val dayOffsets = if (isEmergency) listOf(0L, 1L) else (0L until horizonDays).toList()
            val candidateSlots = if (isEmergency) {
                listOf(ScheduleSlot.EMERGENCY_NOW) + ScheduleSlot.standardSlots
            } else if (ticket.priorityEnum == TicketPriority.HIGH) {
                listOf(ScheduleSlot.MORNING_09_12, ScheduleSlot.AFTERNOON_13_16, ScheduleSlot.EVENING_17_20)
            } else {
                listOf(ScheduleSlot.AFTERNOON_13_16, ScheduleSlot.MORNING_09_12, ScheduleSlot.EVENING_17_20)
            }

            var bestMatch: ScheduleProposal? = null

            // Search chronologically from earliest day & preferred slot, picking least-loaded contractor
            outer@ for (offset in dayOffsets) {
                val targetDay = todayEpochDay + offset
                for (slot in candidateSlots) {
                    val eligibleForSlot = matchingContractors.filter { contractor ->
                        val key = contractor.id to targetDay
                        val usedCount = bookedCountByContractorDay[key] ?: 0
                        val usedSlots = bookedSlotsByContractorDay[key].orEmpty()
                        val withinCapacity = usedCount < contractor.dailyCapacity.toInt()
                        val withinShift = slot == ScheduleSlot.EMERGENCY_NOW ||
                            (contractor.shiftStartHour <= slot.startHour && contractor.shiftEndHour >= slot.endHour)
                        val slotFree = slot == ScheduleSlot.EMERGENCY_NOW || slot !in usedSlots
                        withinCapacity && withinShift && slotFree
                    }.sortedWith(
                        compareBy<Contractor> { bookedCountByContractorDay[it.id to targetDay] ?: 0 }
                            .thenBy { it.hourlyRate }
                            .thenByDescending { it.completedJobs }
                    )

                    val chosen = eligibleForSlot.firstOrNull()
                    if (chosen != null) {
                        val key = chosen.id to targetDay
                        val usedBefore = bookedCountByContractorDay[key] ?: 0
                        bookedSlotsByContractorDay.getOrPut(key) { mutableSetOf() }.add(slot)
                        bookedCountByContractorDay[key] = usedBefore + 1

                        val reason = buildString {
                            append(chosen.specialtyEnum.label)
                            append(" match • ")
                            append("${usedBefore}/${chosen.dailyCapacity} day slots")
                            if (isEmergency) {
                                append(" • Urgent dispatch")
                            } else if (offset == 0L) {
                                append(" • Same-day window")
                            } else {
                                append(" • +${offset}d earliest slot")
                            }
                        }.take(155)

                        bestMatch = ScheduleProposal(
                            ticket = ticket,
                            contractor = chosen,
                            epochDay = targetDay,
                            slot = slot,
                            matchReason = reason
                        )
                        break@outer
                    }
                }
            }

            if (bestMatch != null) {
                proposals.add(bestMatch)
            } else {
                unscheduledReasons[ticket.id] =
                    "No open ${ticket.categoryEnum.label} capacity in the next $horizonDays days."
            }
        }

        return AutoSchedulePlan(proposals = proposals, unscheduledReasons = unscheduledReasons)
    }
}
