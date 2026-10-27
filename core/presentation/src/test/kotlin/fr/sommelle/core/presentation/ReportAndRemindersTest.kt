package fr.sommelle.core.presentation

import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.ScreeningAnswers
import fr.sommelle.core.model.SleepQuality
import fr.sommelle.core.model.SleepWindow
import fr.sommelle.core.protocol.PeriodSummary
import fr.sommelle.core.protocol.ScreeningEvaluator
import fr.sommelle.core.protocol.WindowChange
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReportAndRemindersTest {

    private val start = LocalDate.of(2026, 11, 2)
    private val wake = LocalTime.of(6, 45)

    private fun night(nightOf: LocalDate, inBed: Long, asleep: Long, quality: SleepQuality = SleepQuality.POOR): DiaryEntry {
        val out = nightOf.atTime(wake)
        val bed = out.minusMinutes(inBed)
        return DiaryEntry(
            nightOf = nightOf, bedTime = bed, lightsOutTime = bed,
            sleepOnsetLatency = Duration.ofMinutes(inBed - asleep), awakeningCount = 0,
            wakeAfterSleepOnset = Duration.ZERO, finalWakeTime = out, outOfBedTime = out,
            quality = quality,
        )
    }

    private fun summary(inBed: Long, asleep: Long, onset: Long, quality: Double) = PeriodSummary(
        loggedNights = 7,
        meanTimeInBed = Duration.ofMinutes(inBed),
        meanTotalSleepTime = Duration.ofMinutes(asleep),
        sleepEfficiency = asleep.toDouble() / inBed,
        meanSleepOnsetLatency = Duration.ofMinutes(onset),
        meanWakeAfterSleepOnset = Duration.ofMinutes(40),
        meanQualityScore = quality,
    )

    @Test
    fun `bilan avant apres`() {
        val rows = ProgramOutcome.compare(
            baseline = summary(510, 350, 60, 2.0),
            recent = summary(400, 360, 20, 3.4),
            impactBefore = DaytimeImpact(start, 3),
            impactAfter = DaytimeImpact(start.plusDays(42), 1),
        )
        assertEquals(OutcomeMetric.entries, rows.map { it.metric })
        val byMetric = rows.associateBy { it.metric }
        assertEquals(Trend.BETTER, byMetric.getValue(OutcomeMetric.SLEEP_ONSET).trend)
        assertEquals(Trend.STABLE, byMetric.getValue(OutcomeMetric.AWAKE_AT_NIGHT).trend)
        assertEquals(Trend.BETTER, byMetric.getValue(OutcomeMetric.TOTAL_SLEEP).trend)
        assertEquals(Trend.BETTER, byMetric.getValue(OutcomeMetric.EFFICIENCY).trend)
        assertEquals("2,0 / 5", byMetric.getValue(OutcomeMetric.QUALITY).before)
        assertEquals("3,4 / 5", byMetric.getValue(OutcomeMetric.QUALITY).after)
        assertEquals(Trend.BETTER, byMetric.getValue(OutcomeMetric.DAYTIME_IMPACT).trend)
    }

    @Test
    fun `bilan sans ressenti ni donnees`() {
        assertEquals(emptyList(), ProgramOutcome.compare(null, null, null, null))
        val sameDay = DaytimeImpact(start, 2)
        assertEquals(emptyList(), ProgramOutcome.compare(null, null, sameDay, sameDay))
    }

    @Test
    fun `periode recente exige assez de nuits`() {
        val today = start.plusDays(50)
        assertNull(ProgramOutcome.recentPeriod((0L until 4L).map { night(today.minusDays(it), 400, 360) }, today))
        assertEquals(6, ProgramOutcome.recentPeriod((0L until 6L).map { night(today.minusDays(it), 400, 360) }, today)!!.loggedNights)
    }

    @Test
    fun `heures de rappel`() {
        val settings = ReminderSettings(morningEnabled = true)
        assertEquals(LocalTime.of(7, 15), ReminderPlanner.morningTime(settings, wake))
        assertEquals(LocalTime.of(8, 0), ReminderPlanner.morningTime(settings, null))
        assertEquals(LocalTime.of(9, 0), ReminderPlanner.morningTime(settings.copy(morningTime = LocalTime.of(9, 0)), wake))

        val window = SleepWindow(LocalTime.of(0, 15), wake, Duration.ofMinutes(390), start, 1)
        assertEquals(LocalTime.of(23, 45), ReminderPlanner.eveningTime(window))
        assertNull(ReminderPlanner.eveningTime(null))

        val now = LocalDateTime.of(2026, 11, 3, 22, 0)
        assertEquals(LocalDateTime.of(2026, 11, 3, 23, 45), ReminderPlanner.next(now, LocalTime.of(23, 45)))
        assertEquals(LocalDateTime.of(2026, 11, 4, 7, 15), ReminderPlanner.next(now, LocalTime.of(7, 15)))
    }

    @Test
    fun `modules cognitifs debloques par semaine`() {
        assertEquals(emptyList(), CognitiveModule.available(1))
        assertEquals(listOf(CognitiveModule.BELIEFS, CognitiveModule.WIND_DOWN), CognitiveModule.available(2))
        assertEquals(CognitiveModule.PARADOXICAL_INTENTION, CognitiveModule.nextLocked(2))
        assertEquals(CognitiveModule.entries, CognitiveModule.available(8))
        assertNull(CognitiveModule.nextLocked(8))
    }

    @Test
    fun `resume pour le soignant`() {
        val screening = ScreeningEvaluator().evaluate(ScreeningAnswers(ageBand = AgeBand.FROM_65, safetyCriticalActivity = true))
        val entries = (0L until 7L).map { night(start.plusDays(it), 510, 350) } +
            (7L until 14L).map { night(start.plusDays(it), 375, 340) } +
            (14L until 19L).map { night(start.plusDays(it), 390, 365, SleepQuality.GOOD) }
        val windows = listOf(
            StoredWindow(SleepWindow(LocalTime.of(0, 30), wake, Duration.ofMinutes(375), start.plusDays(7), 1), null),
            StoredWindow(SleepWindow(LocalTime.of(0, 15), wake, Duration.ofMinutes(390), start.plusDays(14), 1), WindowChange.INCREASED),
        )
        val today = start.plusDays(18)
        val report = ClinicianReport.build(
            today = today,
            startDate = start,
            screening = screening,
            entries = entries,
            windows = windows,
            impacts = listOf(DaytimeImpact(start, 3), DaytimeImpact(today, 1)),
        )
        assertEquals("65 ans et plus", report.ageBand)
        assertTrue(report.safetyNotes.any { "plancher" in it })
        assertEquals("coucher 0 h 15, lever 6 h 45 (6 h 30)", report.currentWindow)
        assertEquals(listOf("Observation", "Semaine 1", "Semaine 2 (en cours)"), report.weeks.map { it.label })
        assertEquals("Aucune", report.weeks[0].window)
        assertEquals(7, report.weeks[1].nights)
        assertEquals(19, report.nights.size)
        assertTrue(report.outcome.isEmpty(), "le bilan avant/après n'apparaît qu'en fin de programme")
        assertEquals("Semaine 2", report.programWeek)
    }

    @Test
    fun `export csv`() {
        val csv = DiaryCsv.export(listOf(night(start.plusDays(1), 480, 420), night(start, 480, 360)))
        val lines = csv.removePrefix("﻿").trim().split("\r\n")
        assertEquals(3, lines.size)
        assertEquals(DiaryCsv.HEADER.joinToString(";"), lines[0])
        assertTrue(lines[1].startsWith("2026-11-02;"))
        assertTrue(lines[1].endsWith(";480;360;75"))
    }
}
