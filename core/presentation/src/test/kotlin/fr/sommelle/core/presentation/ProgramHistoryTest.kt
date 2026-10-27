package fr.sommelle.core.presentation

import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.SleepQuality
import fr.sommelle.core.model.SleepWindow
import fr.sommelle.core.protocol.WindowChange
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProgramHistoryTest {

    private val start = LocalDate.of(2026, 11, 2)
    private val wake = LocalTime.of(6, 45)

    private fun night(nightOf: LocalDate, inBed: Long, asleep: Long): DiaryEntry {
        val out = nightOf.atTime(wake)
        val bed = out.minusMinutes(inBed)
        return DiaryEntry(
            nightOf = nightOf, bedTime = bed, lightsOutTime = bed,
            sleepOnsetLatency = Duration.ofMinutes(inBed - asleep), awakeningCount = 0,
            wakeAfterSleepOnset = Duration.ZERO, finalWakeTime = out, outOfBedTime = out,
            quality = SleepQuality.FAIR,
        )
    }

    private fun window(from: LocalDate, minutes: Long, change: WindowChange?) = StoredWindow(
        SleepWindow(wake.minusMinutes(minutes), wake, Duration.ofMinutes(minutes), from, 1),
        change,
    )

    private val baseline = (0L until 7L).map { night(start.plusDays(it), 510, 370) }
    private val week1 = (7L until 14L).map { night(start.plusDays(it), 375, 340) }
    private val initial = window(start.plusDays(7), 375, null)
    private val adjusted = window(start.plusDays(14), 390, WindowChange.INCREASED)

    @Test
    fun `pas de bilan avant le premier ajustement`() {
        assertNull(ProgramHistory.latestReport(listOf(initial), baseline + week1))
    }

    @Test
    fun `bilan reconstitue a partir des donnees enregistrees`() {
        val report = ProgramHistory.latestReport(listOf(adjusted, initial), baseline + week1)!!
        assertEquals(ReportTone.LONGER, report.tone)
        assertEquals("91 %", report.efficiency)
        assertEquals("15 minutes", report.change)
        assertEquals("0 h 15", report.bedTime)
        assertEquals(7, report.loggedNights)
    }

    @Test
    fun `periodes du graphique`() {
        assertEquals(1, ProgramHistory.periods(start, emptyList(), baseline).size)
        val periods = ProgramHistory.periods(start, listOf(initial, adjusted), baseline + week1)
        assertEquals(2, periods.size)
        assertEquals(Duration.ofMinutes(510), periods[0]!!.meanTimeInBed)
        assertEquals(Duration.ofMinutes(340), periods[1]!!.meanTotalSleepTime)
    }

    @Test
    fun `pastille de semaine`() {
        assertEquals(WeekBadge.OBSERVATION to 0, weekBadge(0))
        assertEquals(WeekBadge.PROGRAM_WEEK to 3, weekBadge(3))
        assertEquals(WeekBadge.MAINTENANCE to 7, weekBadge(7))
    }
}
