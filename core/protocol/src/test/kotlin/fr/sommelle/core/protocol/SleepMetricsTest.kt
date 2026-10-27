package fr.sommelle.core.protocol

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SleepMetricsTest {

    @Test
    fun `calcul des indicateurs d une nuit`() {
        // Coucher 23:00, extinction 23:15, latence 20 min, éveils 30 min, réveil 06:30, lever 06:45.
        val m = NightMetrics.of(entry())
        assertEquals(Duration.ofMinutes(465), m.timeInBed)
        assertEquals(Duration.ofMinutes(385), m.totalSleepTime)
        assertEquals(385.0 / 465.0, m.sleepEfficiency, 1e-9)
    }

    @Test
    fun `le temps passe au lit avant l extinction compte dans le temps au lit mais pas dans le sommeil`() {
        val reading = NightMetrics.of(entry(bed = "22:15", lightsOut = "23:15"))
        assertEquals(Duration.ofMinutes(510), reading.timeInBed)
        assertEquals(Duration.ofMinutes(385), reading.totalSleepTime)
    }

    @Test
    fun `une entree invalide est refusee`() {
        assertFailsWith<IllegalArgumentException> { NightMetrics.of(entry(outOfBed = "06:00")) }
    }

    @Test
    fun `synthese d une periode ponderee par la duree des nuits`() {
        val nights = listOf(
            night(MONDAY, timeInBedMin = 480, totalSleepMin = 360),
            night(MONDAY.minusDays(1), timeInBedMin = 360, totalSleepMin = 330),
        )
        val s = PeriodSummary.of(nights)!!
        assertEquals(2, s.loggedNights)
        assertEquals(Duration.ofMinutes(420), s.meanTimeInBed)
        assertEquals(Duration.ofMinutes(345), s.meanTotalSleepTime)
        assertEquals(690.0 / 840.0, s.sleepEfficiency, 1e-9)
    }

    @Test
    fun `une nuit saisie deux fois n est comptee qu une fois`() {
        val dup = listOf(night(MONDAY, 480, 360), night(MONDAY, 480, 360))
        assertEquals(1, PeriodSummary.of(dup)!!.loggedNights)
    }

    @Test
    fun `periode vide`() {
        assertNull(PeriodSummary.of(emptyList()))
    }
}
