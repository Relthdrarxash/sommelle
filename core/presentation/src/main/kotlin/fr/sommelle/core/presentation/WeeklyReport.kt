package fr.sommelle.core.presentation

import fr.sommelle.core.model.SleepWindow
import fr.sommelle.core.protocol.PeriodSummary
import fr.sommelle.core.protocol.ProtocolConfig
import fr.sommelle.core.protocol.WindowChange
import fr.sommelle.core.protocol.WindowDecision
import java.time.Duration

/** Ton du bilan hebdomadaire. Jamais d'échec : une semaine difficile est une information. */
enum class ReportTone { LONGER, SHORTER, STEADY, AT_FLOOR, AT_MAXIMUM, MISSING_NIGHTS }

/**
 * Contenu du bilan hebdomadaire, déjà mis en forme. L'interface choisit le titre et la phrase
 * selon [tone] et y insère ces valeurs (DESIGN.md, section 7 : une phrase avant un graphique).
 */
data class WeeklyReport(
    val tone: ReportTone,
    /** « 88 % », ou null si la semaine n'a pas assez de nuits. */
    val efficiency: String?,
    /** « 15 minutes », pour les allongements et réductions. */
    val change: String?,
    val bedTime: String,
    val wakeTime: String,
    val loggedNights: Int,
    val requiredNights: Int,
) {
    companion object {
        fun of(previous: SleepWindow, decision: WindowDecision, config: ProtocolConfig = ProtocolConfig.DEFAULT): WeeklyReport {
            val delta: Duration = decision.window.duration.minus(previous.duration)
            val tone = when (decision.change) {
                WindowChange.INCREASED -> ReportTone.LONGER
                WindowChange.DECREASED, WindowChange.DECREASED_TO_FLOOR -> ReportTone.SHORTER
                WindowChange.UNCHANGED_IN_TARGET_RANGE -> ReportTone.STEADY
                WindowChange.UNCHANGED_AT_FLOOR -> ReportTone.AT_FLOOR
                WindowChange.UNCHANGED_AT_MAXIMUM -> ReportTone.AT_MAXIMUM
                WindowChange.UNCHANGED_INSUFFICIENT_DATA -> ReportTone.MISSING_NIGHTS
            }
            val week = decision.week
            return WeeklyReport(
                tone = tone,
                efficiency = week?.takeIf { tone != ReportTone.MISSING_NIGHTS }?.let { FrenchFormat.percent(it.sleepEfficiency) },
                change = if (tone == ReportTone.LONGER || tone == ReportTone.SHORTER) FrenchFormat.durationInWords(delta) else null,
                bedTime = FrenchFormat.time(decision.window.bedTime),
                wakeTime = FrenchFormat.time(decision.window.wakeTime),
                loggedNights = week?.loggedNights ?: 0,
                requiredNights = config.minimumLoggedNightsPerWeek,
            )
        }
    }
}

/** Une ligne du graphique « temps au lit et temps dormi ». */
data class WeekBar(
    val label: String,
    val timeInBed: String,
    val totalSleep: String,
    /** Longueurs relatives, entre 0 et 1, par rapport à la plus longue nuit du graphique. */
    val timeInBedFraction: Float,
    val totalSleepFraction: Float,
)

object WeekChart {
    /** [periods] : la ligne de base puis chaque semaine, dans l'ordre. Les périodes vides sont ignorées. */
    fun bars(periods: List<PeriodSummary?>): List<WeekBar> {
        val labelled = periods.mapIndexedNotNull { index, summary ->
            summary?.let { (if (index == 0) "Au départ" else "Semaine $index") to it }
        }
        val max = labelled.maxOfOrNull { it.second.meanTimeInBed.toMinutes() }?.coerceAtLeast(1) ?: return emptyList()
        return labelled.map { (label, s) ->
            WeekBar(
                label = label,
                timeInBed = FrenchFormat.duration(s.meanTimeInBed),
                totalSleep = FrenchFormat.duration(s.meanTotalSleepTime),
                timeInBedFraction = s.meanTimeInBed.toMinutes().toFloat() / max,
                totalSleepFraction = s.meanTotalSleepTime.toMinutes().toFloat() / max,
            )
        }
    }

    /** Description complète lue par TalkBack à la place du graphique. */
    fun accessibilityDescription(bars: List<WeekBar>): String =
        "Évolution par semaine. " + bars.joinToString(" ") { "${it.label} : ${it.timeInBed} au lit, ${it.totalSleep} de sommeil." }
}
