package fr.sommelle.core.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiaryValidatorTest {

    @Test
    fun `une nuit ordinaire ne pose aucun probleme`() {
        assertEquals(emptySet(), DiaryValidator.validate(entry()))
    }

    @Test
    fun `l ordre des heures est verifie`() {
        val issues = DiaryValidator.validate(entry(bed = "23:30", lightsOut = "23:00"))
        assertTrue(DiaryIssue.LIGHTS_OUT_BEFORE_BEDTIME in issues)
        assertFalse(DiaryValidator.isSavable(entry(bed = "23:30", lightsOut = "23:00")))

        assertTrue(DiaryIssue.OUT_OF_BED_BEFORE_FINAL_WAKE in DiaryValidator.validate(entry(outOfBed = "06:00")))
    }

    @Test
    fun `les eveils ne peuvent pas depasser la periode de sommeil`() {
        val issues = DiaryValidator.validate(entry(latencyMin = 300, wasoMin = 200))
        assertTrue(DiaryIssue.AWAKE_TIME_EXCEEDS_SLEEP_PERIOD in issues)
    }

    @Test
    fun `les incoherences mineures sont signalees sans bloquer`() {
        val noAwakenings = entry(awakenings = 0, wasoMin = 30)
        assertEquals(setOf(DiaryIssue.AWAKE_DURATION_WITHOUT_AWAKENINGS), DiaryValidator.validate(noAwakenings))
        assertTrue(DiaryValidator.isSavable(noAwakenings))

        val noDuration = entry(awakenings = 3, wasoMin = 0)
        assertEquals(setOf(DiaryIssue.AWAKENINGS_WITHOUT_DURATION), DiaryValidator.validate(noDuration))
    }

    @Test
    fun `une valeur negative est bloquante`() {
        assertTrue(DiaryIssue.NEGATIVE_DURATION in DiaryValidator.validate(entry(latencyMin = -5)))
        assertTrue(DiaryIssue.NEGATIVE_AWAKENING_COUNT in DiaryValidator.validate(entry(awakenings = -1)))
    }
}
