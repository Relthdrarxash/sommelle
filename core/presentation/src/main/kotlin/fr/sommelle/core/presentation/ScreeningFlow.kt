package fr.sommelle.core.presentation

import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.ScreeningAnswers

/**
 * Questions oui/non du questionnaire d'entrée, posées une par écran après la tranche d'âge.
 * Les libellés sont dans les ressources du module d'interface ; chaque question sait
 * si elle s'applique et comment elle modifie les réponses.
 */
enum class ScreeningQuestion(
    private val appliesTo: (ScreeningAnswers) -> Boolean = { true },
    private val set: (ScreeningAnswers, Boolean) -> ScreeningAnswers,
) {
    SHIFT_WORK(set = { a, v -> a.copy(shiftOrNightWork = v) }),
    EXTERNAL_DISRUPTION(set = { a, v -> a.copy(externalNightDisruption = v) }),
    SNORING_PAUSES(set = { a, v -> a.copy(loudSnoringWithBreathingPauses = v) }),
    DAYTIME_SLEEPINESS(set = { a, v -> a.copy(markedDaytimeSleepiness = v) }),
    BIPOLAR(set = { a, v -> a.copy(bipolarDisorder = v) }),
    EPILEPSY(set = { a, v -> a.copy(epilepsy = v) }),
    PREGNANCY(set = { a, v -> a.copy(pregnancy = v) }),
    SAFETY_CRITICAL(set = { a, v -> a.copy(safetyCriticalActivity = v, prefersNoRestriction = if (v) a.prefersNoRestriction else false) }),
    PREFERS_NO_RESTRICTION(appliesTo = { it.safetyCriticalActivity }, set = { a, v -> a.copy(prefersNoRestriction = v) }),
    LATE_SCHEDULE(set = { a, v -> a.copy(lateScheduleSleepsWellOnceAsleep = v) }),
    DISTRESS(set = { a, v -> a.copy(expressedDistress = v) });

    fun applies(answers: ScreeningAnswers): Boolean = appliesTo(answers)
    fun answer(answers: ScreeningAnswers, yes: Boolean): ScreeningAnswers = set(answers, yes)
}

/** Progression dans le questionnaire : l'âge d'abord, puis les questions qui s'appliquent. */
data class ScreeningFlow(
    val answers: ScreeningAnswers? = null,
    val current: ScreeningQuestion? = null,
    val finished: Boolean = false,
) {
    /** Étape actuelle (1 = âge) et nombre total estimé, pour la barre de progression. */
    val stepNumber: Int get() = when {
        finished -> totalSteps
        current == null -> 1
        else -> 2 + current.ordinal
    }
    val totalSteps: Int get() = 1 + ScreeningQuestion.entries.size

    val isAgeStep: Boolean get() = !finished && current == null

    fun chooseAge(ageBand: AgeBand): ScreeningFlow {
        val a = answers?.copy(ageBand = ageBand) ?: ScreeningAnswers(ageBand = ageBand)
        return copy(answers = a).advanceFrom(null)
    }

    fun answer(yes: Boolean): ScreeningFlow {
        val q = checkNotNull(current) { "aucune question en cours" }
        val a = q.answer(checkNotNull(answers), yes)
        return copy(answers = a).advanceFrom(q)
    }

    /** Retour à la question précédente, ou à l'âge. Les réponses déjà données sont conservées. */
    fun back(): ScreeningFlow {
        if (isAgeStep) return this
        val a = checkNotNull(answers)
        val from = if (finished) ScreeningQuestion.entries.size else checkNotNull(current).ordinal
        val previous = ScreeningQuestion.entries.take(from).lastOrNull { it.applies(a) }
        return copy(current = previous, finished = false)
    }

    private fun advanceFrom(question: ScreeningQuestion?): ScreeningFlow {
        val a = checkNotNull(answers)
        val start = question?.ordinal?.plus(1) ?: 0
        val next = ScreeningQuestion.entries.drop(start).firstOrNull { it.applies(a) }
        return if (next == null) copy(current = null, finished = true) else copy(current = next, finished = false)
    }
}
