package fr.sommelle.core.protocol

import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.ScreeningResult
import fr.sommelle.core.model.SleepWindow
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/** Paramètres de restriction propres à une personne, dérivés de la configuration et du questionnaire. */
data class RestrictionProfile(
    val minimumWindow: Duration,
    val increaseThreshold: Double,
    val decreaseThreshold: Double,
) {
    companion object {
        /** Renvoie null si la restriction n'est pas proposée à cette personne. */
        fun from(config: ProtocolConfig, screening: ScreeningResult): RestrictionProfile? {
            if (!screening.restrictionAllowed) return null
            val offset = if (screening.ageBand == AgeBand.FROM_65) config.seniorThresholdOffset else 0.0
            return RestrictionProfile(
                minimumWindow = screening.minimumSleepWindow!!.coerceAtLeast(config.absoluteMinimumWindow),
                increaseThreshold = config.increaseThreshold - offset,
                decreaseThreshold = config.decreaseThreshold - offset,
            )
        }
    }
}

sealed interface InitialWindowResult {
    data class Ready(val window: SleepWindow, val baseline: PeriodSummary) : InitialWindowResult
    data class InsufficientData(val loggedNights: Int, val requiredNights: Int) : InitialWindowResult
}

enum class WindowChange {
    INCREASED,
    DECREASED,
    /** Réduction limitée par le plancher. */
    DECREASED_TO_FLOOR,
    UNCHANGED_IN_TARGET_RANGE,
    UNCHANGED_AT_FLOOR,
    UNCHANGED_AT_MAXIMUM,
    /** Pas assez de nuits renseignées : la fenêtre est maintenue, avec un message explicatif. */
    UNCHANGED_INSUFFICIENT_DATA,
}

data class WindowDecision(
    val window: SleepWindow,
    val change: WindowChange,
    val week: PeriodSummary?,
)

/**
 * Restriction du temps au lit (Spielman et al., Sleep 1987).
 * - Fenêtre initiale : temps de sommeil total moyen de la ligne de base, arrondi au pas supérieur,
 *   borné entre le plancher de la personne et le maximum.
 * - Ajustement hebdomadaire selon l'efficacité de la semaine écoulée.
 * - L'heure de lever reste fixe ; l'heure de coucher en découle.
 */
class SleepWindowCalculator(private val config: ProtocolConfig = ProtocolConfig.DEFAULT) {

    fun initialWindow(
        baseline: Collection<DiaryEntry>,
        wakeTime: LocalTime,
        profile: RestrictionProfile,
        effectiveFrom: LocalDate,
    ): InitialWindowResult {
        val summary = PeriodSummary.of(baseline)
        val logged = summary?.loggedNights ?: 0
        if (summary == null || logged < config.minimumLoggedNightsPerWeek) {
            return InitialWindowResult.InsufficientData(logged, config.minimumLoggedNightsPerWeek)
        }
        val duration = roundUp(summary.meanTotalSleepTime)
            .coerceAtLeast(profile.minimumWindow)
            .coerceAtMost(config.maximumWindow)
        return InitialWindowResult.Ready(window(wakeTime, duration, effectiveFrom), summary)
    }

    fun weeklyAdjustment(
        current: SleepWindow,
        week: Collection<DiaryEntry>,
        profile: RestrictionProfile,
        effectiveFrom: LocalDate,
    ): WindowDecision {
        val summary = PeriodSummary.of(week)
        if (summary == null || summary.loggedNights < config.minimumLoggedNightsPerWeek) {
            return WindowDecision(current.copy(effectiveFrom = effectiveFrom), WindowChange.UNCHANGED_INSUFFICIENT_DATA, summary)
        }

        val efficiency = summary.sleepEfficiency
        val (duration, change) = when {
            efficiency >= profile.increaseThreshold -> {
                if (current.duration >= config.maximumWindow) {
                    current.duration to WindowChange.UNCHANGED_AT_MAXIMUM
                } else {
                    current.duration.plus(config.increaseStep).coerceAtMost(config.maximumWindow) to WindowChange.INCREASED
                }
            }
            efficiency < profile.decreaseThreshold -> {
                val target = current.duration.minus(config.decreaseStep)
                when {
                    current.duration <= profile.minimumWindow -> profile.minimumWindow to WindowChange.UNCHANGED_AT_FLOOR
                    target < profile.minimumWindow -> profile.minimumWindow to WindowChange.DECREASED_TO_FLOOR
                    else -> target to WindowChange.DECREASED
                }
            }
            else -> current.duration.coerceAtLeast(profile.minimumWindow) to WindowChange.UNCHANGED_IN_TARGET_RANGE
        }
        return WindowDecision(window(current.wakeTime, duration, effectiveFrom), change, summary)
    }

    /**
     * Heure de lever suggérée : médiane des réveils finaux de la ligne de base, arrondie au quart d'heure.
     * La personne choisit ensuite librement son heure, qui doit rester tenable 7 jours sur 7.
     */
    fun suggestWakeTime(baseline: Collection<DiaryEntry>): LocalTime? {
        if (baseline.isEmpty()) return null
        val minutes = baseline.map { it.finalWakeTime.toLocalTime().toSecondOfDay() / 60 }.sorted()
        val median = if (minutes.size % 2 == 1) {
            minutes[minutes.size / 2]
        } else {
            (minutes[minutes.size / 2 - 1] + minutes[minutes.size / 2]) / 2
        }
        val step = config.roundingStep.toMinutes().toInt()
        val rounded = ((median + step / 2) / step) * step
        return LocalTime.MIDNIGHT.plusMinutes(rounded.toLong())
    }

    private fun window(wakeTime: LocalTime, duration: Duration, effectiveFrom: LocalDate) = SleepWindow(
        bedTime = wakeTime.minus(duration),
        wakeTime = wakeTime,
        duration = duration,
        effectiveFrom = effectiveFrom,
        protocolVersion = config.version,
    )

    private fun roundUp(duration: Duration): Duration {
        val step = config.roundingStep.toMinutes()
        val minutes = duration.toMinutes()
        return Duration.ofMinutes(((minutes + step - 1) / step) * step)
    }
}
