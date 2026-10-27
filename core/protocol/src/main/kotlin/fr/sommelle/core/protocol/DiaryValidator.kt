package fr.sommelle.core.protocol

import fr.sommelle.core.model.DiaryEntry
import java.time.Duration

/**
 * Problèmes détectables dans une saisie d'agenda. Les problèmes bloquants empêchent
 * l'enregistrement ; les autres donnent lieu à une question de confirmation.
 * Les libellés sont gérés par l'interface : ce module ne contient aucun texte affiché.
 */
enum class DiaryIssue(val blocking: Boolean) {
    LIGHTS_OUT_BEFORE_BEDTIME(blocking = true),
    FINAL_WAKE_BEFORE_LIGHTS_OUT(blocking = true),
    OUT_OF_BED_BEFORE_FINAL_WAKE(blocking = true),
    NEGATIVE_DURATION(blocking = true),
    NEGATIVE_AWAKENING_COUNT(blocking = true),
    AWAKE_TIME_EXCEEDS_SLEEP_PERIOD(blocking = true),
    TIME_IN_BED_UNUSUALLY_LONG(blocking = false),
    AWAKE_DURATION_WITHOUT_AWAKENINGS(blocking = false),
    AWAKENINGS_WITHOUT_DURATION(blocking = false),
    WAKE_DATE_DIFFERS_FROM_NIGHT(blocking = false),
}

object DiaryValidator {

    private val UNUSUALLY_LONG_TIME_IN_BED: Duration = Duration.ofHours(16)

    fun validate(entry: DiaryEntry): Set<DiaryIssue> = buildSet {
        with(entry) {
            if (lightsOutTime.isBefore(bedTime)) add(DiaryIssue.LIGHTS_OUT_BEFORE_BEDTIME)
            if (finalWakeTime.isBefore(lightsOutTime)) add(DiaryIssue.FINAL_WAKE_BEFORE_LIGHTS_OUT)
            if (outOfBedTime.isBefore(finalWakeTime)) add(DiaryIssue.OUT_OF_BED_BEFORE_FINAL_WAKE)
            if (sleepOnsetLatency.isNegative || wakeAfterSleepOnset.isNegative) add(DiaryIssue.NEGATIVE_DURATION)
            if (awakeningCount < 0) add(DiaryIssue.NEGATIVE_AWAKENING_COUNT)

            val sleepPeriod = Duration.between(lightsOutTime, finalWakeTime)
            if (!sleepPeriod.isNegative && sleepOnsetLatency.plus(wakeAfterSleepOnset) > sleepPeriod) {
                add(DiaryIssue.AWAKE_TIME_EXCEEDS_SLEEP_PERIOD)
            }
            if (Duration.between(bedTime, outOfBedTime) > UNUSUALLY_LONG_TIME_IN_BED) {
                add(DiaryIssue.TIME_IN_BED_UNUSUALLY_LONG)
            }
            if (awakeningCount == 0 && wakeAfterSleepOnset > Duration.ZERO) {
                add(DiaryIssue.AWAKE_DURATION_WITHOUT_AWAKENINGS)
            }
            if (awakeningCount > 0 && wakeAfterSleepOnset.isZero) {
                add(DiaryIssue.AWAKENINGS_WITHOUT_DURATION)
            }
            if (finalWakeTime.toLocalDate() != nightOf) add(DiaryIssue.WAKE_DATE_DIFFERS_FROM_NIGHT)
        }
    }

    fun isSavable(entry: DiaryEntry): Boolean = validate(entry).none { it.blocking }
}
