package fr.sommelle.core.presentation

import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.ScreeningResult
import fr.sommelle.core.model.SleepWindow
import fr.sommelle.core.protocol.InitialWindowResult
import fr.sommelle.core.protocol.PeriodSummary
import fr.sommelle.core.protocol.ProtocolConfig
import fr.sommelle.core.protocol.RestrictionProfile
import fr.sommelle.core.protocol.SleepWindowCalculator
import fr.sommelle.core.protocol.WindowDecision
import java.time.LocalDate
import java.time.LocalTime

/** Ce que le programme attend à une date donnée, concernant la fenêtre de sommeil. */
sealed interface WindowStep {
    /** Restriction non proposée à cette personne. */
    data object NotApplicable : WindowStep

    /** Ligne de base en cours : agenda seul. */
    data class Baseline(val daysLeft: Int, val loggedNights: Int) : WindowStep

    /** Ligne de base terminée mais trop peu de nuits : on prolonge. */
    data class BaselineExtended(val loggedNights: Int, val requiredNights: Int) : WindowStep

    /** La personne doit choisir son heure de lever avant le calcul de la fenêtre. */
    data class NeedsWakeTime(val suggested: LocalTime?) : WindowStep

    /** Première fenêtre à enregistrer. */
    data class NewInitialWindow(val window: SleepWindow, val baseline: PeriodSummary) : WindowStep

    /** Ajustement hebdomadaire à enregistrer, et bilan à montrer. */
    data class NewWeeklyDecision(val previous: SleepWindow, val decision: WindowDecision) : WindowStep

    /** Rien à faire : la fenêtre en cours s'applique. */
    data class Current(val window: SleepWindow) : WindowStep
}

/**
 * Orchestration pure du programme, à partir de ce qui est enregistré. Le calendrier des
 * ajustements suit la dernière fenêtre : un nouveau bilan tous les 7 jours à partir de sa date
 * d'effet, calculé sur les 7 nuits qui précèdent.
 */
class ProgramEngine(private val config: ProtocolConfig = ProtocolConfig.DEFAULT) {

    private val calculator = SleepWindowCalculator(config)

    fun windowStep(
        today: LocalDate,
        startDate: LocalDate,
        screening: ScreeningResult,
        entries: List<DiaryEntry>,
        windows: List<SleepWindow>,
        wakeTime: LocalTime?,
    ): WindowStep {
        val profile = RestrictionProfile.from(config, screening) ?: return WindowStep.NotApplicable
        val latest = windows.maxByOrNull { it.effectiveFrom }

        if (latest == null) {
            val baselineEnd = startDate.plusDays(config.baselineDays.toLong())
            val baselineNights = nightsBetween(entries, startDate, baselineEnd.minusDays(1))
            if (today < baselineEnd) {
                val daysLeft = (baselineEnd.toEpochDay() - today.toEpochDay()).toInt()
                return WindowStep.Baseline(daysLeft = daysLeft, loggedNights = baselineNights.size)
            }
            val recent = nightsBetween(entries, today.minusDays(config.baselineDays - 1L), today)
            if (wakeTime == null) return WindowStep.NeedsWakeTime(calculator.suggestWakeTime(recent))
            return when (val result = calculator.initialWindow(recent, wakeTime, profile, today)) {
                is InitialWindowResult.Ready -> WindowStep.NewInitialWindow(result.window, result.baseline)
                is InitialWindowResult.InsufficientData ->
                    WindowStep.BaselineExtended(result.loggedNights, result.requiredNights)
            }
        }

        val nextReview = latest.effectiveFrom.plusDays(7)
        if (today < nextReview) return WindowStep.Current(latest)
        val week = nightsBetween(entries, latest.effectiveFrom, nextReview.minusDays(1))
        val decision = calculator.weeklyAdjustment(latest, week, profile, nextReview)
        return WindowStep.NewWeeklyDecision(latest, decision)
    }

    private fun nightsBetween(entries: List<DiaryEntry>, from: LocalDate, to: LocalDate): List<DiaryEntry> =
        entries.filter { it.nightOf >= from && it.nightOf <= to }
}
