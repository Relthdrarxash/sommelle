package fr.sommelle.core.presentation

import fr.sommelle.core.model.AgeBand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreeningFlowTest {

    private fun answerAll(flow: ScreeningFlow, yes: (ScreeningQuestion) -> Boolean): ScreeningFlow {
        var f = flow
        while (!f.finished) f = f.answer(yes(f.current!!))
        return f
    }

    @Test
    fun `l age vient en premier puis toutes les questions`() {
        val start = ScreeningFlow()
        assertTrue(start.isAgeStep)
        assertEquals(1, start.stepNumber)
        val afterAge = start.chooseAge(AgeBand.FROM_25_TO_44)
        assertEquals(ScreeningQuestion.SHIFT_WORK, afterAge.current)
        assertEquals(2, afterAge.stepNumber)
        val done = answerAll(afterAge) { false }
        assertTrue(done.finished)
        assertFalse(done.answers!!.safetyCriticalActivity)
    }

    @Test
    fun `la question de preference n apparait que pour les activites a risque`() {
        val asked = mutableListOf<ScreeningQuestion>()
        answerAll(ScreeningFlow().chooseAge(AgeBand.FROM_45_TO_64)) { asked += it; false }
        assertFalse(ScreeningQuestion.PREFERS_NO_RESTRICTION in asked)

        asked.clear()
        val done = answerAll(ScreeningFlow().chooseAge(AgeBand.FROM_45_TO_64)) {
            asked += it
            it == ScreeningQuestion.SAFETY_CRITICAL || it == ScreeningQuestion.PREFERS_NO_RESTRICTION
        }
        assertTrue(ScreeningQuestion.PREFERS_NO_RESTRICTION in asked)
        assertTrue(done.answers!!.prefersNoRestriction)
    }

    @Test
    fun `retour en arriere`() {
        val q2 = ScreeningFlow().chooseAge(AgeBand.FROM_65).answer(true)
        assertEquals(ScreeningQuestion.EXTERNAL_DISRUPTION, q2.current)
        val back = q2.back()
        assertEquals(ScreeningQuestion.SHIFT_WORK, back.current)
        assertTrue(back.answers!!.shiftOrNightWork)
        assertTrue(back.back().isAgeStep)

        val done = answerAll(ScreeningFlow().chooseAge(AgeBand.FROM_65)) { false }
        assertEquals(ScreeningQuestion.DISTRESS, done.back().current)
    }
}
