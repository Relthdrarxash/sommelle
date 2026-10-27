package fr.sommelle.core.presentation

import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.SleepWindow
import fr.sommelle.core.protocol.PeriodSummary
import fr.sommelle.core.protocol.ProtocolConfig
import fr.sommelle.core.protocol.WindowChange
import fr.sommelle.core.protocol.WindowDecision
import java.time.LocalDate

/** Une fenêtre enregistrée, avec la décision qui l'a produite (null pour la fenêtre initiale). */
data class StoredWindow(val window: SleepWindow, val change: WindowChange?)

/** Reconstitution, à partir des données enregistrées, de ce que montrent l'accueil et le bilan. */
object ProgramHistory {

    /** Bilan de la dernière décision hebdomadaire, ou null tant qu'il n'y en a pas eu. */
    fun latestReport(
        windows: List<StoredWindow>,
        entries: List<DiaryEntry>,
        config: ProtocolConfig = ProtocolConfig.DEFAULT,
    ): WeeklyReport? {
        val sorted = windows.sortedBy { it.window.effectiveFrom }
        if (sorted.size < 2) return null
        val latest = sorted.last()
        val change = latest.change ?: return null
        val previous = sorted[sorted.size - 2]
        val week = PeriodSummary.of(nights(entries, previous.window.effectiveFrom, latest.window.effectiveFrom.minusDays(1)))
        return WeeklyReport.of(previous.window, WindowDecision(latest.window, change, week), config)
    }

    /**
     * Périodes du graphique : la ligne de base, puis chaque semaine de fenêtre terminée.
     * La semaine en cours n'apparaît qu'une fois terminée.
     */
    fun periods(
        startDate: LocalDate,
        windows: List<StoredWindow>,
        entries: List<DiaryEntry>,
        config: ProtocolConfig = ProtocolConfig.DEFAULT,
    ): List<PeriodSummary?> {
        val sorted = windows.map { it.window }.sortedBy { it.effectiveFrom }
        val baselineEnd = sorted.firstOrNull()?.effectiveFrom?.minusDays(1)
            ?: startDate.plusDays(config.baselineDays - 1L)
        val baseline = PeriodSummary.of(nights(entries, startDate, baselineEnd))
        val weeks = sorted.zipWithNext { current, next ->
            PeriodSummary.of(nights(entries, current.effectiveFrom, next.effectiveFrom.minusDays(1)))
        }
        return listOf(baseline) + weeks
    }

    private fun nights(entries: List<DiaryEntry>, from: LocalDate, to: LocalDate) =
        entries.filter { it.nightOf >= from && it.nightOf <= to }
}

/** Libellé de la semaine du programme, pour la pastille de l'accueil. */
enum class WeekBadge { OBSERVATION, PROGRAM_WEEK, MAINTENANCE }

fun weekBadge(programWeek: Int?, programLengthWeeks: Int = 6): Pair<WeekBadge, Int> = when {
    programWeek == null || programWeek == 0 -> WeekBadge.OBSERVATION to 0
    programWeek > programLengthWeeks -> WeekBadge.MAINTENANCE to programWeek
    else -> WeekBadge.PROGRAM_WEEK to programWeek
}
