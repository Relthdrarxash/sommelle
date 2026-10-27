package fr.sommelle.core.protocol

import fr.sommelle.core.model.DiaryEntry
import java.time.Duration

/**
 * Indicateurs d'une nuit.
 * - Temps au lit (TIB) : de la mise au lit (Q1) à la sortie du lit (Q7).
 * - Temps de sommeil total (TST) : de l'extinction (Q2) au réveil final (Q6),
 *   moins la latence d'endormissement (Q3) et les éveils nocturnes (Q5).
 * - Efficacité du sommeil (SE) : TST / TIB.
 */
data class NightMetrics(
    val timeInBed: Duration,
    val totalSleepTime: Duration,
    val sleepEfficiency: Double,
) {
    companion object {
        fun of(entry: DiaryEntry): NightMetrics {
            require(DiaryValidator.isSavable(entry)) { "entrée d'agenda invalide : ${DiaryValidator.validate(entry)}" }
            val timeInBed = Duration.between(entry.bedTime, entry.outOfBedTime)
            val sleepPeriod = Duration.between(entry.lightsOutTime, entry.finalWakeTime)
            val totalSleep = sleepPeriod.minus(entry.sleepOnsetLatency).minus(entry.wakeAfterSleepOnset)
                .coerceAtLeast(Duration.ZERO)
            val efficiency = if (timeInBed.isZero) 0.0 else totalSleep.toMinutes().toDouble() / timeInBed.toMinutes()
            return NightMetrics(timeInBed, totalSleep, efficiency.coerceIn(0.0, 1.0))
        }
    }
}

/**
 * Synthèse d'une période (ligne de base ou semaine de restriction).
 * L'efficacité de la période est calculée sur les totaux (somme des TST / somme des TIB),
 * ce qui pondère chaque nuit par sa durée. Choix à faire valider.
 */
data class PeriodSummary(
    val loggedNights: Int,
    val meanTimeInBed: Duration,
    val meanTotalSleepTime: Duration,
    val sleepEfficiency: Double,
    val meanSleepOnsetLatency: Duration,
    val meanWakeAfterSleepOnset: Duration,
    val meanQualityScore: Double,
) {
    companion object {
        fun of(entries: Collection<DiaryEntry>): PeriodSummary? {
            if (entries.isEmpty()) return null
            val nights = entries.distinctBy { it.nightOf }
            val metrics = nights.map(NightMetrics::of)
            val totalInBed = metrics.sumOf { it.timeInBed.toMinutes() }
            val totalSleep = metrics.sumOf { it.totalSleepTime.toMinutes() }
            val count = nights.size.toLong()
            return PeriodSummary(
                loggedNights = nights.size,
                meanTimeInBed = Duration.ofMinutes(totalInBed / count),
                meanTotalSleepTime = Duration.ofMinutes(totalSleep / count),
                sleepEfficiency = if (totalInBed == 0L) 0.0 else totalSleep.toDouble() / totalInBed,
                meanSleepOnsetLatency = Duration.ofMinutes(nights.sumOf { it.sleepOnsetLatency.toMinutes() } / count),
                meanWakeAfterSleepOnset = Duration.ofMinutes(nights.sumOf { it.wakeAfterSleepOnset.toMinutes() } / count),
                meanQualityScore = nights.map { it.quality.score }.average(),
            )
        }
    }
}

internal fun Duration.coerceAtLeast(minimum: Duration): Duration = if (this < minimum) minimum else this
internal fun Duration.coerceAtMost(maximum: Duration): Duration = if (this > maximum) maximum else this
