package fr.sommelle.core.presentation

import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.Notice
import fr.sommelle.core.model.Pathway
import fr.sommelle.core.model.Referral
import fr.sommelle.core.model.ScreeningResult
import fr.sommelle.core.protocol.NightMetrics
import fr.sommelle.core.protocol.PeriodSummary
import fr.sommelle.core.protocol.ProgramPlanner
import fr.sommelle.core.protocol.ProtocolConfig
import java.time.LocalDate

/** Une ligne du tableau hebdomadaire du résumé. */
data class ReportWeek(
    val label: String,
    val nights: Int,
    val timeInBed: String,
    val totalSleep: String,
    val efficiency: String,
    val sleepOnset: String,
    val awake: String,
    val quality: String,
    /** Fenêtre de sommeil prescrite cette semaine-là, ou « Aucune ». */
    val window: String,
)

/** Une nuit du détail de l'agenda. */
data class ReportNight(
    val night: String,
    val bedTime: String,
    val outOfBed: String,
    val sleepOnset: String,
    val awake: String,
    val totalSleep: String,
    val efficiency: String,
    val quality: String,
)

/**
 * Résumé destiné à un professionnel de santé, entièrement mis en forme ici pour que le PDF,
 * l'écran et les tests partagent le même contenu. Le résumé ne contient ni nom ni identifiant :
 * la personne l'écrit à la main si elle le souhaite.
 */
data class ClinicianReport(
    val generatedOn: String,
    val programStart: String,
    val programWeek: String,
    val ageBand: String,
    val pathway: String,
    val safetyNotes: List<String>,
    val currentWindow: String?,
    val weeks: List<ReportWeek>,
    val outcome: List<OutcomeRow>,
    val nights: List<ReportNight>,
) {
    companion object {
        /** Nombre de nuits détaillées : les quatre dernières semaines. */
        const val DETAILED_NIGHTS = 28

        fun build(
            today: LocalDate,
            startDate: LocalDate,
            screening: ScreeningResult,
            entries: List<DiaryEntry>,
            windows: List<StoredWindow>,
            impacts: List<DaytimeImpact>,
            config: ProtocolConfig = ProtocolConfig.DEFAULT,
        ): ClinicianReport {
            val sortedWindows = windows.sortedBy { it.window.effectiveFrom }
            val week = ProgramPlanner(config = config).programWeek(startDate, today)
            val periods = weekPeriods(today, startDate, sortedWindows, config)
            val baseline = PeriodSummary.of(nightsBetween(entries, periods.first().first, periods.first().second))
            val recent = ProgramOutcome.recentPeriod(entries, today, config)
            val sortedImpacts = impacts.sortedBy { it.date }
            val showOutcome = (week ?: 0) >= ProgramOutcome.AVAILABLE_FROM_WEEK

            return ClinicianReport(
                generatedOn = FrenchFormat.longDate(today),
                programStart = FrenchFormat.longDate(startDate),
                programWeek = when (week) {
                    null, 0 -> "Semaine d'observation"
                    else -> "Semaine $week"
                },
                ageBand = ageLabel(screening.ageBand),
                pathway = pathwayLabel(screening.pathway),
                safetyNotes = screening.referrals.map(::referralLabel) + screening.notices.mapNotNull(::noticeLabel),
                currentWindow = sortedWindows.lastOrNull()?.window?.let {
                    "coucher ${FrenchFormat.time(it.bedTime)}, lever ${FrenchFormat.time(it.wakeTime)} (${FrenchFormat.duration(it.duration)})"
                },
                weeks = periods.mapIndexedNotNull { index, (from, to) ->
                    val summary = PeriodSummary.of(nightsBetween(entries, from, to)) ?: return@mapIndexedNotNull null
                    val window = sortedWindows.lastOrNull { it.window.effectiveFrom <= from }?.window
                    val label = if (index == 0) "Observation" else "Semaine $index" + if (to >= today) " (en cours)" else ""
                    ReportWeek(
                        label = label,
                        nights = summary.loggedNights,
                        timeInBed = FrenchFormat.duration(summary.meanTimeInBed),
                        totalSleep = FrenchFormat.duration(summary.meanTotalSleepTime),
                        efficiency = FrenchFormat.percent(summary.sleepEfficiency),
                        sleepOnset = FrenchFormat.duration(summary.meanSleepOnsetLatency),
                        awake = FrenchFormat.duration(summary.meanWakeAfterSleepOnset),
                        quality = ProgramOutcome.qualityText(summary.meanQualityScore),
                        window = window?.let { "${FrenchFormat.time(it.bedTime)} à ${FrenchFormat.time(it.wakeTime)}" } ?: "Aucune",
                    )
                },
                outcome = if (showOutcome) {
                    ProgramOutcome.compare(baseline, recent, sortedImpacts.firstOrNull(), sortedImpacts.lastOrNull())
                } else {
                    emptyList()
                },
                nights = entries.sortedByDescending { it.nightOf }.take(DETAILED_NIGHTS).map(::night),
            )
        }

        /** Périodes : la ligne de base, puis une semaine par fenêtre, jusqu'à aujourd'hui. */
        private fun weekPeriods(
            today: LocalDate,
            startDate: LocalDate,
            windows: List<StoredWindow>,
            config: ProtocolConfig,
        ): List<Pair<LocalDate, LocalDate>> {
            val starts = windows.map { it.window.effectiveFrom }
            val baselineEnd = starts.firstOrNull()?.minusDays(1) ?: startDate.plusDays(config.baselineDays - 1L)
            val weeks = starts.mapIndexed { i, from ->
                val to = starts.getOrNull(i + 1)?.minusDays(1) ?: minOf(from.plusDays(6), today)
                from to to
            }
            return listOf(startDate to baselineEnd) + weeks
        }

        private fun nightsBetween(entries: List<DiaryEntry>, from: LocalDate, to: LocalDate) =
            entries.filter { it.nightOf >= from && it.nightOf <= to }

        private fun night(entry: DiaryEntry): ReportNight {
            val m = NightMetrics.of(entry)
            return ReportNight(
                night = "${FrenchFormat.dayName(entry.nightOf.dayOfWeek).take(3)}. ${entry.nightOf.dayOfMonth}/${entry.nightOf.monthValue}",
                bedTime = FrenchFormat.time(entry.bedTime.toLocalTime()),
                outOfBed = FrenchFormat.time(entry.outOfBedTime.toLocalTime()),
                sleepOnset = FrenchFormat.duration(entry.sleepOnsetLatency),
                awake = if (entry.awakeningCount == 0) "Aucun" else "${entry.awakeningCount} × ${FrenchFormat.duration(entry.wakeAfterSleepOnset)}",
                totalSleep = FrenchFormat.duration(m.totalSleepTime),
                efficiency = FrenchFormat.percent(m.sleepEfficiency),
                quality = "${entry.quality.score} / 5",
            )
        }

        private fun ageLabel(age: AgeBand) = when (age) {
            AgeBand.UNDER_18 -> "Moins de 18 ans"
            AgeBand.FROM_18_TO_24 -> "18 à 24 ans"
            AgeBand.FROM_25_TO_44 -> "25 à 44 ans"
            AgeBand.FROM_45_TO_64 -> "45 à 64 ans"
            AgeBand.FROM_65 -> "65 ans et plus"
        }

        private fun pathwayLabel(pathway: Pathway) = when (pathway) {
            Pathway.FULL -> "Programme complet, avec restriction du temps au lit"
            Pathway.WITHOUT_RESTRICTION -> "Programme sans restriction du temps au lit"
            Pathway.OUT_OF_SCOPE -> "Hors du périmètre du programme"
        }

        private fun referralLabel(referral: Referral) = when (referral) {
            Referral.MINOR -> "Moins de 18 ans : orientation vers un médecin"
            Referral.SLEEP_APNEA_SUSPECTED -> "Ronflements avec pauses respiratoires déclarés : apnée du sommeil à explorer"
            Referral.EXCESSIVE_DAYTIME_SLEEPINESS -> "Endormissements involontaires en journée déclarés"
            Referral.BIPOLAR_DISORDER -> "Trouble bipolaire déclaré : pas de restriction du temps au lit"
            Referral.EPILEPSY -> "Épilepsie déclarée : pas de restriction du temps au lit"
            Referral.DELAYED_SLEEP_PHASE_SUSPECTED -> "Horaires très tardifs avec sommeil normal : retard de phase possible"
            Referral.SHIFT_WORK -> "Travail posté ou de nuit déclaré"
        }

        private fun noticeLabel(notice: Notice) = when (notice) {
            Notice.REINFORCED_DROWSINESS_WARNING -> "Activité à risque de somnolence : plancher de fenêtre relevé à 6 h"
            Notice.EXTERNAL_DISRUPTION -> "Nuits interrompues par un tiers déclarées"
            Notice.PREGNANCY -> "Grossesse déclarée : pas de restriction du temps au lit"
            Notice.CRISIS_RESOURCES -> "Détresse exprimée au questionnaire : ressources d'aide affichées"
        }
    }
}

/**
 * Agenda complet au format CSV, lisible par un tableur français (séparateur point-virgule,
 * UTF-8 avec marque d'ordre des octets pour Excel).
 */
object DiaryCsv {
    private const val BOM = "﻿"

    val HEADER = listOf(
        "nuit", "coucher", "extinction", "endormissement_min", "reveils", "eveils_min",
        "reveil_final", "lever", "qualite_sur_5", "temps_au_lit_min", "sommeil_min", "efficacite_pct",
    )

    fun export(entries: List<DiaryEntry>): String = buildString {
        append(BOM)
        append(HEADER.joinToString(";")).append("\r\n")
        entries.sortedBy { it.nightOf }.forEach { e ->
            val m = NightMetrics.of(e)
            append(
                listOf(
                    e.nightOf.toString(),
                    e.bedTime.toString(),
                    e.lightsOutTime.toString(),
                    e.sleepOnsetLatency.toMinutes().toString(),
                    e.awakeningCount.toString(),
                    e.wakeAfterSleepOnset.toMinutes().toString(),
                    e.finalWakeTime.toString(),
                    e.outOfBedTime.toString(),
                    e.quality.score.toString(),
                    m.timeInBed.toMinutes().toString(),
                    m.totalSleepTime.toMinutes().toString(),
                    Math.round(m.sleepEfficiency * 100).toString(),
                ).joinToString(";"),
            ).append("\r\n")
        }
    }
}
