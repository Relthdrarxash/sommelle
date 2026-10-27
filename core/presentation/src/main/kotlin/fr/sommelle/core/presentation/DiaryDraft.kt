package fr.sommelle.core.presentation

import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.PrefillSource
import fr.sommelle.core.model.SleepQuality
import fr.sommelle.core.protocol.DiaryIssue
import fr.sommelle.core.protocol.DiaryValidator
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Les questions de l'agenda guidé, dans l'ordre du Consensus Sleep Diary. */
enum class DiaryStep {
    BED_TIME,
    LIGHTS_OUT,
    SLEEP_ONSET,
    AWAKENINGS,
    FINAL_WAKE,
    OUT_OF_BED,
    QUALITY;

    val number: Int get() = ordinal + 1
    val next: DiaryStep? get() = entries.getOrNull(ordinal + 1)
    val previous: DiaryStep? get() = entries.getOrNull(ordinal - 1)

    companion object {
        val total: Int get() = entries.size
    }
}

enum class TimeField { BED_TIME, LIGHTS_OUT, FINAL_WAKE, OUT_OF_BED }

/**
 * Valeurs proposées sous forme de pastilles : des estimations, pas des chronométrages
 * (docs/design.md, section 5). La dernière valeur de chaque liste signifie « ou plus ».
 */
object DiaryChoices {
    val sleepOnsetMinutes: List<Long> = listOf(5, 15, 30, 45, 60, 90)
    val awakeningCounts: List<Int> = listOf(0, 1, 2, 3, 4, 5)
    val awakeMinutes: List<Long> = listOf(5, 15, 30, 45, 60, 90, 120)
    const val TIME_STEP_MINUTES: Long = 15
}

/**
 * Saisie en cours d'une nuit. Les heures sont manipulées sans date : une heure de l'après-midi
 * ou du soir (12 h et après) appartient à la veille de [nightOf], une heure du matin au jour même.
 *
 * Les décalages gardent l'ordre logique de la nuit : repousser le coucher après l'extinction
 * repousse aussi l'extinction, ce qui évite de présenter une erreur à quelqu'un de mal réveillé.
 */
data class DiaryDraft(
    val nightOf: LocalDate,
    val bedTime: LocalTime,
    val lightsOut: LocalTime,
    val sleepOnsetMinutes: Long,
    val awakeningCount: Int,
    val awakeMinutes: Long,
    val finalWake: LocalTime,
    val outOfBed: LocalTime,
    val quality: SleepQuality,
    val prefillSource: PrefillSource?,
) {

    fun time(field: TimeField): LocalTime = when (field) {
        TimeField.BED_TIME -> bedTime
        TimeField.LIGHTS_OUT -> lightsOut
        TimeField.FINAL_WAKE -> finalWake
        TimeField.OUT_OF_BED -> outOfBed
    }

    fun shift(field: TimeField, minutes: Long): DiaryDraft = withTime(field, time(field).plusMinutes(minutes))

    fun withTime(field: TimeField, value: LocalTime): DiaryDraft {
        var d = when (field) {
            TimeField.BED_TIME -> copy(bedTime = value)
            TimeField.LIGHTS_OUT -> copy(lightsOut = value)
            TimeField.FINAL_WAKE -> copy(finalWake = value)
            TimeField.OUT_OF_BED -> copy(outOfBed = value)
        }
        when (field) {
            TimeField.BED_TIME -> if (d.at(d.lightsOut) < d.at(d.bedTime)) d = d.copy(lightsOut = d.bedTime)
            TimeField.LIGHTS_OUT -> if (d.at(d.lightsOut) < d.at(d.bedTime)) d = d.copy(bedTime = d.lightsOut)
            TimeField.FINAL_WAKE -> if (d.at(d.outOfBed) < d.at(d.finalWake)) d = d.copy(outOfBed = d.finalWake)
            TimeField.OUT_OF_BED -> if (d.at(d.outOfBed) < d.at(d.finalWake)) d = d.copy(finalWake = d.outOfBed)
        }
        return d
    }

    fun withSleepOnset(minutes: Long) = copy(sleepOnsetMinutes = minutes)

    fun withAwakeningCount(count: Int): DiaryDraft = when {
        count <= 0 -> copy(awakeningCount = 0, awakeMinutes = 0)
        awakeMinutes == 0L -> copy(awakeningCount = count, awakeMinutes = DiaryChoices.awakeMinutes[1])
        else -> copy(awakeningCount = count)
    }

    fun withAwakeMinutes(minutes: Long) = if (awakeningCount == 0) this else copy(awakeMinutes = minutes)

    fun withQuality(value: SleepQuality) = copy(quality = value)

    fun toEntry(): DiaryEntry = DiaryEntry(
        nightOf = nightOf,
        bedTime = at(bedTime),
        lightsOutTime = at(lightsOut),
        sleepOnsetLatency = Duration.ofMinutes(sleepOnsetMinutes),
        awakeningCount = awakeningCount,
        wakeAfterSleepOnset = Duration.ofMinutes(awakeMinutes),
        finalWakeTime = at(finalWake),
        outOfBedTime = at(outOfBed),
        quality = quality,
        prefillSource = prefillSource,
    )

    fun issues(): Set<DiaryIssue> = DiaryValidator.validate(toEntry())

    val isSavable: Boolean get() = issues().none { it.blocking }

    private fun at(time: LocalTime): LocalDateTime =
        if (time >= LocalTime.NOON) nightOf.minusDays(1).atTime(time) else nightOf.atTime(time)

    companion object {
        /** Brouillon initial : la nuit précédente si elle existe, sinon des valeurs neutres. */
        fun prefilled(nightOf: LocalDate, previous: DiaryEntry?): DiaryDraft = if (previous != null) {
            DiaryDraft(
                nightOf = nightOf,
                bedTime = previous.bedTime.toLocalTime(),
                lightsOut = previous.lightsOutTime.toLocalTime(),
                sleepOnsetMinutes = previous.sleepOnsetLatency.toMinutes(),
                awakeningCount = previous.awakeningCount,
                awakeMinutes = previous.wakeAfterSleepOnset.toMinutes(),
                finalWake = previous.finalWakeTime.toLocalTime(),
                outOfBed = previous.outOfBedTime.toLocalTime(),
                quality = previous.quality,
                prefillSource = PrefillSource.PREVIOUS_NIGHT,
            )
        } else {
            DiaryDraft(
                nightOf = nightOf,
                bedTime = LocalTime.of(23, 0),
                lightsOut = LocalTime.of(23, 15),
                sleepOnsetMinutes = 30,
                awakeningCount = 1,
                awakeMinutes = 30,
                finalWake = LocalTime.of(6, 45),
                outOfBed = LocalTime.of(7, 0),
                quality = SleepQuality.FAIR,
                prefillSource = null,
            )
        }
    }
}

/**
 * Nuits que l'on peut encore remplir : celle qui vient de se terminer et la précédente
 * (tolérance de 48 h, docs/protocole.md, section 3), de la plus récente à la plus ancienne.
 */
fun nightsToFill(today: LocalDate, filledNights: Set<LocalDate>): List<LocalDate> =
    listOf(today, today.minusDays(1)).filter { it !in filledNights }
