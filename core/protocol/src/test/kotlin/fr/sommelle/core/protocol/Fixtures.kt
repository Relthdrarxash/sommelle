package fr.sommelle.core.protocol

import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.DiaryEntry
import fr.sommelle.core.model.ScreeningAnswers
import fr.sommelle.core.model.SleepQuality
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal val MONDAY: LocalDate = LocalDate.of(2026, 11, 2)
internal val WAKE: LocalTime = LocalTime.of(6, 45)

/** Construit une heure de la nuit se terminant le matin de [nightOf] (après midi = la veille). */
internal fun at(nightOf: LocalDate, time: String): LocalDateTime {
    val t = LocalTime.parse(time)
    return if (t >= LocalTime.NOON) nightOf.minusDays(1).atTime(t) else nightOf.atTime(t)
}

internal fun entry(
    nightOf: LocalDate = MONDAY,
    bed: String = "23:00",
    lightsOut: String = "23:15",
    latencyMin: Long = 20,
    awakenings: Int = 2,
    wasoMin: Long = 30,
    finalWake: String = "06:30",
    outOfBed: String = "06:45",
) = DiaryEntry(
    nightOf = nightOf,
    bedTime = at(nightOf, bed),
    lightsOutTime = at(nightOf, lightsOut),
    sleepOnsetLatency = Duration.ofMinutes(latencyMin),
    awakeningCount = awakenings,
    wakeAfterSleepOnset = Duration.ofMinutes(wasoMin),
    finalWakeTime = at(nightOf, finalWake),
    outOfBedTime = at(nightOf, outOfBed),
    quality = SleepQuality.FAIR,
)

/** Nuit synthétique avec un temps au lit et un temps de sommeil donnés, réveil à [WAKE]. */
internal fun night(nightOf: LocalDate, timeInBedMin: Long, totalSleepMin: Long): DiaryEntry {
    val out = nightOf.atTime(WAKE)
    val bed = out.minusMinutes(timeInBedMin)
    return DiaryEntry(
        nightOf = nightOf,
        bedTime = bed,
        lightsOutTime = bed,
        sleepOnsetLatency = Duration.ofMinutes(timeInBedMin - totalSleepMin),
        awakeningCount = 0,
        wakeAfterSleepOnset = Duration.ZERO,
        finalWakeTime = out,
        outOfBedTime = out,
        quality = SleepQuality.POOR,
    )
}

/** Une semaine de nuits identiques, la dernière se terminant le matin de [lastNight]. */
internal fun week(lastNight: LocalDate, nights: Int, timeInBedMin: Long, totalSleepMin: Long) =
    (0 until nights).map { night(lastNight.minusDays(it.toLong()), timeInBedMin, totalSleepMin) }

internal fun answers(ageBand: AgeBand = AgeBand.FROM_25_TO_44) = ScreeningAnswers(ageBand = ageBand)
