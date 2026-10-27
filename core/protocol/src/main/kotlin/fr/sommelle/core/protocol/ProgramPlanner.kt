package fr.sommelle.core.protocol

import fr.sommelle.core.model.Pathway
import fr.sommelle.core.model.ScreeningResult
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Identifiants stables des composantes, utilisés par les modules de fonctionnalité. */
enum class ComponentId {
    SLEEP_DIARY,
    SLEEP_EDUCATION,
    SLEEP_RESTRICTION,
    STIMULUS_CONTROL,
    COGNITIVE,
    RELAPSE_PREVENTION,
}

/**
 * Une composante du programme. Ajouter, retirer ou réordonner une composante revient à modifier
 * la liste passée au [ProgramPlanner], sans toucher au reste du moteur.
 */
interface ProtocolComponent {
    val id: ComponentId
    /** Semaine du programme à partir de laquelle la composante est proposée (0 = ligne de base). */
    val activationWeek: Int
    fun isEligible(screening: ScreeningResult): Boolean
}

private data class Component(
    override val id: ComponentId,
    override val activationWeek: Int,
    private val eligibility: (ScreeningResult) -> Boolean,
) : ProtocolComponent {
    override fun isEligible(screening: ScreeningResult) = eligibility(screening)
}

object DefaultComponents {
    private val inProgram: (ScreeningResult) -> Boolean = { it.pathway != Pathway.OUT_OF_SCOPE }

    /** Ordre d'introduction d'ARCHITECTURE.md, section 4 (à valider). */
    val ALL: List<ProtocolComponent> = listOf(
        Component(ComponentId.SLEEP_EDUCATION, activationWeek = 0) { true },
        Component(ComponentId.SLEEP_DIARY, activationWeek = 0, eligibility = inProgram),
        Component(ComponentId.SLEEP_RESTRICTION, activationWeek = 1) { it.restrictionAllowed },
        Component(ComponentId.STIMULUS_CONTROL, activationWeek = 1, eligibility = inProgram),
        Component(ComponentId.COGNITIVE, activationWeek = 2, eligibility = inProgram),
        Component(ComponentId.RELAPSE_PREVENTION, activationWeek = 6, eligibility = inProgram),
    )
}

class ProgramPlanner(
    private val components: List<ProtocolComponent> = DefaultComponents.ALL,
    private val config: ProtocolConfig = ProtocolConfig.DEFAULT,
) {
    init {
        require(components.map { it.id }.toSet().size == components.size) { "composantes en double" }
    }

    /** Semaine du programme : 0 pendant la ligne de base, puis 1, 2, ... Null avant le début. */
    fun programWeek(startDate: LocalDate, today: LocalDate): Int? {
        val days = ChronoUnit.DAYS.between(startDate, today)
        if (days < 0) return null
        if (days < config.baselineDays) return 0
        return 1 + ((days - config.baselineDays) / 7).toInt()
    }

    fun activeComponents(week: Int, screening: ScreeningResult): List<ComponentId> =
        components
            .filter { week >= it.activationWeek && it.isEligible(screening) }
            .map { it.id }
}
