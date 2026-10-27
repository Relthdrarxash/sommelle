package fr.sommelle.core.presentation

import fr.sommelle.core.model.PrefillSource
import fr.sommelle.core.model.SleepQuality
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiaryDraftTest {

    private val tuesday = LocalDate.of(2026, 11, 3)
    private val draft = DiaryDraft.prefilled(tuesday, previous = null)

    @Test
    fun `brouillon par defaut valide et date correctement`() {
        assertTrue(draft.isSavable)
        val entry = draft.toEntry()
        assertEquals(tuesday.minusDays(1), entry.bedTime.toLocalDate())
        assertEquals(tuesday, entry.finalWakeTime.toLocalDate())
        assertNull(entry.prefillSource)
    }

    @Test
    fun `un coucher apres minuit reste dans la nuit`() {
        val late = draft.withTime(TimeField.BED_TIME, LocalTime.of(0, 30))
        val entry = late.toEntry()
        assertEquals(tuesday.atTime(0, 30), entry.bedTime)
        assertEquals(tuesday.atTime(0, 30), entry.lightsOutTime)
        assertTrue(late.isSavable)
    }

    @Test
    fun `les decalages preservent l ordre de la nuit`() {
        val later = draft.shift(TimeField.BED_TIME, 30)
        assertEquals(LocalTime.of(23, 30), later.bedTime)
        assertEquals(LocalTime.of(23, 30), later.lightsOut)

        val earlyOut = draft.withTime(TimeField.OUT_OF_BED, LocalTime.of(6, 30))
        assertEquals(LocalTime.of(6, 30), earlyOut.finalWake)

        val earlyLights = draft.withTime(TimeField.LIGHTS_OUT, LocalTime.of(22, 30))
        assertEquals(LocalTime.of(22, 30), earlyLights.bedTime)
    }

    @Test
    fun `les eveils et leur duree restent coherents`() {
        val none = draft.withAwakeningCount(0)
        assertEquals(0, none.awakeMinutes)
        assertEquals(none, none.withAwakeMinutes(45))
        val some = none.withAwakeningCount(2)
        assertEquals(15, some.awakeMinutes)
        assertTrue(some.issues().isEmpty())
    }

    @Test
    fun `pre-remplissage a partir de la nuit precedente`() {
        val previous = draft.withQuality(SleepQuality.GOOD).withSleepOnset(15).toEntry()
        val next = DiaryDraft.prefilled(tuesday.plusDays(1), previous)
        assertEquals(PrefillSource.PREVIOUS_NIGHT, next.prefillSource)
        assertEquals(SleepQuality.GOOD, next.quality)
        assertEquals(Duration.ofMinutes(15), next.toEntry().sleepOnsetLatency)
        assertEquals(tuesday, next.toEntry().bedTime.toLocalDate())
    }

    @Test
    fun `etapes de l agenda`() {
        assertEquals(7, DiaryStep.total)
        assertEquals(DiaryStep.LIGHTS_OUT, DiaryStep.BED_TIME.next)
        assertNull(DiaryStep.QUALITY.next)
        assertNull(DiaryStep.BED_TIME.previous)
    }

    @Test
    fun `nuits a remplir avec tolerance de 48 h`() {
        assertEquals(listOf(tuesday, tuesday.minusDays(1)), nightsToFill(tuesday, emptySet()))
        assertEquals(listOf(tuesday.minusDays(1)), nightsToFill(tuesday, setOf(tuesday)))
        assertEquals(emptyList(), nightsToFill(tuesday, setOf(tuesday, tuesday.minusDays(1))))
    }
}
