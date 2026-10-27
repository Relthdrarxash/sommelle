package fr.sommelle.core.presentation

import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.ScreeningAnswers
import fr.sommelle.core.model.SleepQuality
import fr.sommelle.core.protocol.ScreeningEvaluator
import fr.sommelle.core.protocol.WindowChange
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ProgramEngineTest {

    private val engine = ProgramEngine()
    private val start = LocalDate.of(2026, 11, 2)
    private val wake = LocalTime.of(6, 45)
    private val full = ScreeningEvaluator().evaluate(ScreeningAnswers(ageBand = AgeBand.FROM_25_TO_44))

    private fun night(nightOf: LocalDate, inBed: Long, asleep: Long): DiaryEntry {
        val out = nightOf.atTime(wake)
        val bed = out.minusMinutes(inBed)
        return DiaryEntry(
            nightOf = nightOf, bedTime = bed, lightsOutTime = bed,
            sleepOnsetLatency = Duration.ofMinutes(inBed - asleep), awakeningCount = 0,
            wakeAfterSleepOnset = Duration.ZERO, finalWakeTime = out, outOfBedTime = out,
            quality = SleepQuality.POOR,
        )
    }

    private fun nights(from: LocalDate, count: Int, inBed: Long, asleep: Long) =
        (0 until count).map { night(from.plusDays(it.toLong()), inBed, asleep) }

    @Test
    fun `sans restriction pas de fenetre`() {
        val noRestriction = ScreeningEvaluator().evaluate(ScreeningAnswers(ageBand = AgeBand.FROM_25_TO_44, epilepsy = true))
        assertEquals(WindowStep.NotApplicable, engine.windowStep(start, start, noRestriction, emptyList(), emptyList(), wake))
    }

    @Test
    fun `ligne de base puis choix de l heure de lever puis premiere fenetre`() {
        val baseline = nights(start, 7, inBed = 510, asleep = 370)
        val during = engine.windowStep(start.plusDays(3), start, full, baseline.take(4), emptyList(), null)
        assertEquals(WindowStep.Baseline(daysLeft = 4, loggedNights = 4), during)

        val day7 = start.plusDays(7)
        val needs = engine.windowStep(day7, start, full, baseline, emptyList(), null)
        assertIs<WindowStep.NeedsWakeTime>(needs)
        assertEquals(wake, needs.suggested)

        val initial = engine.windowStep(day7, start, full, baseline + night(day7, 510, 370), emptyList(), wake)
        assertIs<WindowStep.NewInitialWindow>(initial)
        assertEquals(Duration.ofMinutes(375), initial.window.duration)
        assertEquals(day7, initial.window.effectiveFrom)
    }

    @Test
    fun `ligne de base prolongee si trop peu de nuits`() {
        val step = engine.windowStep(start.plusDays(7), start, full, nights(start.plusDays(1), 3, 480, 360), emptyList(), wake)
        assertEquals(WindowStep.BaselineExtended(loggedNights = 3, requiredNights = 5), step)
    }

    @Test
    fun `bilan hebdomadaire tous les 7 jours`() {
        val day7 = start.plusDays(7)
        val initial = engine.windowStep(day7, start, full, nights(start.plusDays(1), 7, 510, 370), emptyList(), wake)
        val window = (initial as WindowStep.NewInitialWindow).window

        val week1 = nights(day7, 7, 375, 340)
        assertEquals(WindowStep.Current(window), engine.windowStep(day7.plusDays(6), start, full, week1, listOf(window), wake))

        val review = engine.windowStep(day7.plusDays(7), start, full, week1, listOf(window), wake)
        assertIs<WindowStep.NewWeeklyDecision>(review)
        assertEquals(WindowChange.INCREASED, review.decision.change)
        assertEquals(day7.plusDays(7), review.decision.window.effectiveFrom)

        val report = WeeklyReport.of(review.previous, review.decision)
        assertEquals(ReportTone.LONGER, report.tone)
        assertNotNull(report.efficiency)
        assertEquals("15 minutes", report.change)
    }
}
