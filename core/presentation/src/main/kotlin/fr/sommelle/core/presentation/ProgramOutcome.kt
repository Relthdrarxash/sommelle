package fr.sommelle.core.presentation

import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.protocol.PeriodSummary
import fr.sommelle.core.protocol.ProtocolConfig
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Ressenti de la journée, demandé au début puis à la fin du programme :
 * « À quel point vos nuits gênent-elles vos journées ? », de 0 (pas du tout) à 4 (énormément).
 * Question propre à Sommelle, non validée : elle complète les mesures de l'agenda, sans les remplacer.
 */
data class DaytimeImpact(val date: LocalDate, val score: Int) {
    init { require(score in SCALE) { "score hors échelle" } }

    companion object {
        val SCALE = 0..4
    }
}

enum class OutcomeMetric(val label: String) {
    SLEEP_ONSET("Temps d'endormissement"),
    AWAKE_AT_NIGHT("Éveils nocturnes"),
    TOTAL_SLEEP("Temps de sommeil"),
    EFFICIENCY("Efficacité du sommeil"),
    QUALITY("Qualité ressentie"),
    DAYTIME_IMPACT("Gêne dans la journée, de 0 à 4"),
}

/** Sens de l'évolution, du point de vue du sommeil. Jamais présenté comme un échec. */
enum class Trend(val word: String) {
    BETTER("En amélioration"),
    STABLE("Stable"),
    WORSE("En recul"),
}

data class OutcomeRow(val metric: OutcomeMetric, val before: String, val after: String, val trend: Trend)

/**
 * Bilan avant/après : la semaine d'observation comparée aux 7 dernières nuits, à partir des mesures
 * de l'agenda du sommeil (Consensus Sleep Diary), complétées par le ressenti de la journée.
 */
object ProgramOutcome {

    /** Le bilan est proposé à partir de cette semaine du programme. */
    const val AVAILABLE_FROM_WEEK = 6

    /** Les 7 nuits qui précèdent [today] (incluse), si au moins le minimum requis est renseigné. */
    fun recentPeriod(entries: List<DiaryEntry>, today: LocalDate, config: ProtocolConfig = ProtocolConfig.DEFAULT): PeriodSummary? {
        val recent = entries.filter { it.nightOf > today.minusDays(7) && it.nightOf <= today }
        return PeriodSummary.of(recent)?.takeIf { it.loggedNights >= config.minimumLoggedNightsPerWeek }
    }

    fun compare(
        baseline: PeriodSummary?,
        recent: PeriodSummary?,
        impactBefore: DaytimeImpact?,
        impactAfter: DaytimeImpact?,
    ): List<OutcomeRow> {
        val rows = mutableListOf<OutcomeRow>()
        if (baseline != null && recent != null) {
            rows += minutesRow(OutcomeMetric.SLEEP_ONSET, baseline.meanSleepOnsetLatency.toMinutes(), recent.meanSleepOnsetLatency.toMinutes(), lowerIsBetter = true)
            rows += minutesRow(OutcomeMetric.AWAKE_AT_NIGHT, baseline.meanWakeAfterSleepOnset.toMinutes(), recent.meanWakeAfterSleepOnset.toMinutes(), lowerIsBetter = true)
            rows += minutesRow(OutcomeMetric.TOTAL_SLEEP, baseline.meanTotalSleepTime.toMinutes(), recent.meanTotalSleepTime.toMinutes(), lowerIsBetter = false)
            rows += OutcomeRow(
                OutcomeMetric.EFFICIENCY,
                FrenchFormat.percent(baseline.sleepEfficiency),
                FrenchFormat.percent(recent.sleepEfficiency),
                trend(recent.sleepEfficiency - baseline.sleepEfficiency, threshold = 0.02, lowerIsBetter = false),
            )
            rows += OutcomeRow(
                OutcomeMetric.QUALITY,
                qualityText(baseline.meanQualityScore),
                qualityText(recent.meanQualityScore),
                trend(recent.meanQualityScore - baseline.meanQualityScore, threshold = 0.3, lowerIsBetter = false),
            )
        }
        if (impactBefore != null && impactAfter != null && impactAfter.date > impactBefore.date) {
            rows += OutcomeRow(
                OutcomeMetric.DAYTIME_IMPACT,
                "${impactBefore.score} / 4",
                "${impactAfter.score} / 4",
                trend((impactAfter.score - impactBefore.score).toDouble(), threshold = 0.5, lowerIsBetter = true),
            )
        }
        return rows
    }

    private fun minutesRow(metric: OutcomeMetric, before: Long, after: Long, lowerIsBetter: Boolean) = OutcomeRow(
        metric,
        FrenchFormat.duration(java.time.Duration.ofMinutes(before)),
        FrenchFormat.duration(java.time.Duration.ofMinutes(after)),
        trend((after - before).toDouble(), threshold = 5.0, lowerIsBetter = lowerIsBetter),
    )

    private fun trend(delta: Double, threshold: Double, lowerIsBetter: Boolean): Trend = when {
        abs(delta) < threshold -> Trend.STABLE
        (delta < 0) == lowerIsBetter -> Trend.BETTER
        else -> Trend.WORSE
    }

    /** Moyenne de la qualité ressentie, sur 5, avec une décimale à la française. */
    fun qualityText(score: Double): String {
        val tenths = (score * 10).roundToInt()
        return "${tenths / 10},${tenths % 10} / 5"
    }
}
