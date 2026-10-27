package fr.sommelle.core.presentation

import fr.sommelle.core.presentation.FrenchFormat.NBSP
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class FrenchFormatTest {

    private fun nb(s: String) = s.replace(' ', NBSP)

    @Test
    fun `heures a la francaise`() {
        assertEquals(nb("23 h 15"), FrenchFormat.time(LocalTime.of(23, 15)))
        assertEquals(nb("0 h 30"), FrenchFormat.time(LocalTime.of(0, 30)))
        assertEquals(nb("6 h 05"), FrenchFormat.time(LocalTime.of(6, 5)))
        assertEquals(nb("7 h"), FrenchFormat.time(LocalTime.of(7, 0)))
    }

    @Test
    fun `durees`() {
        assertEquals(nb("45 min"), FrenchFormat.duration(Duration.ofMinutes(45)))
        assertEquals(nb("6 h 15"), FrenchFormat.duration(Duration.ofMinutes(375)))
        assertEquals(nb("1 h"), FrenchFormat.duration(Duration.ofMinutes(60)))
        assertEquals(nb("15 minutes"), FrenchFormat.durationInWords(Duration.ofMinutes(-15)))
        assertEquals(nb("1 minute"), FrenchFormat.durationInWords(Duration.ofMinutes(1)))
        assertEquals(nb("1 heure"), FrenchFormat.durationInWords(Duration.ofMinutes(60)))
    }

    @Test
    fun `pourcentage et dates`() {
        assertEquals(nb("88 %"), FrenchFormat.percent(0.8812))
        assertEquals("Mardi 3 novembre", FrenchFormat.longDate(LocalDate.of(2026, 11, 3)))
        assertEquals("Nuit de lundi à mardi", FrenchFormat.nightLabel(LocalDate.of(2026, 11, 3)))
    }
}
