package fr.sommelle.core.protocol

import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.protocol.ComponentId.COGNITIVE
import fr.sommelle.core.protocol.ComponentId.RELAPSE_PREVENTION
import fr.sommelle.core.protocol.ComponentId.SLEEP_DIARY
import fr.sommelle.core.protocol.ComponentId.SLEEP_EDUCATION
import fr.sommelle.core.protocol.ComponentId.SLEEP_RESTRICTION
import fr.sommelle.core.protocol.ComponentId.STIMULUS_CONTROL
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ProgramPlannerTest {

    private val planner = ProgramPlanner()
    private val evaluator = ScreeningEvaluator()

    @Test
    fun `calcul de la semaine du programme`() {
        assertNull(planner.programWeek(MONDAY, MONDAY.minusDays(1)))
        assertEquals(0, planner.programWeek(MONDAY, MONDAY))
        assertEquals(0, planner.programWeek(MONDAY, MONDAY.plusDays(6)))
        assertEquals(1, planner.programWeek(MONDAY, MONDAY.plusDays(7)))
        assertEquals(1, planner.programWeek(MONDAY, MONDAY.plusDays(13)))
        assertEquals(2, planner.programWeek(MONDAY, MONDAY.plusDays(14)))
    }

    @Test
    fun `introduction progressive des composantes`() {
        val full = evaluator.evaluate(answers())
        assertEquals(listOf(SLEEP_EDUCATION, SLEEP_DIARY), planner.activeComponents(0, full))
        assertEquals(listOf(SLEEP_EDUCATION, SLEEP_DIARY, SLEEP_RESTRICTION, STIMULUS_CONTROL), planner.activeComponents(1, full))
        assertEquals(
            listOf(SLEEP_EDUCATION, SLEEP_DIARY, SLEEP_RESTRICTION, STIMULUS_CONTROL, COGNITIVE, RELAPSE_PREVENTION),
            planner.activeComponents(6, full),
        )
    }

    @Test
    fun `sans restriction la composante est absente`() {
        val noRestriction = evaluator.evaluate(answers().copy(pregnancy = true))
        assertEquals(listOf(SLEEP_EDUCATION, SLEEP_DIARY, STIMULUS_CONTROL), planner.activeComponents(1, noRestriction))
    }

    @Test
    fun `hors perimetre seule l education reste`() {
        val outOfScope = evaluator.evaluate(answers(AgeBand.UNDER_18))
        assertEquals(listOf(SLEEP_EDUCATION), planner.activeComponents(6, outOfScope))
    }

    @Test
    fun `composantes en double refusees`() {
        val duplicated = DefaultComponents.ALL + DefaultComponents.ALL.first()
        assertFailsWith<IllegalArgumentException> { ProgramPlanner(duplicated) }
    }

    @Test
    fun `configuration incoherente refusee`() {
        assertFailsWith<IllegalArgumentException> {
            ProtocolConfig.DEFAULT.copy(defaultMinimumWindow = Duration.ofHours(4))
        }
        assertFailsWith<IllegalArgumentException> {
            ProtocolConfig.DEFAULT.copy(increaseThreshold = 0.75)
        }
    }
}
