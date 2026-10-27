package fr.sommelle.core.presentation

import fr.sommelle.core.protocol.PeriodSummary
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeekChartTest {

    private fun summary(inBed: Long, asleep: Long) = PeriodSummary(
        loggedNights = 7,
        meanTimeInBed = Duration.ofMinutes(inBed),
        meanTotalSleepTime = Duration.ofMinutes(asleep),
        sleepEfficiency = asleep.toDouble() / inBed,
        meanSleepOnsetLatency = Duration.ZERO,
        meanWakeAfterSleepOnset = Duration.ZERO,
        meanQualityScore = 3.0,
    )

    @Test
    fun `barres relatives a la plus longue nuit`() {
        val bars = WeekChart.bars(listOf(summary(510, 350), null, summary(375, 330)))
        assertEquals(listOf("Au départ", "Semaine 2"), bars.map { it.label })
        assertEquals(1f, bars[0].timeInBedFraction)
        assertEquals(375f / 510f, bars[1].timeInBedFraction)
        val description = WeekChart.accessibilityDescription(bars)
        assertTrue(description.startsWith("Évolution par semaine."))
        assertTrue("Semaine 2 : 6 h 15 au lit" in description)
    }

    @Test
    fun `aucune periode`() {
        assertEquals(emptyList(), WeekChart.bars(listOf(null)))
    }
}
