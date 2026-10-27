package fr.sommelle.core.presentation

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import kotlin.math.roundToInt

/**
 * Mise en forme française des heures, durées et dates, telle qu'affichée et lue par TalkBack.
 * Les heures s'écrivent « 23 h 15 » et non « 23:15 » : plus lisible, et correctement prononcé
 * par les synthèses vocales.
 */
object FrenchFormat {

    /** Espace insécable, pour qu'un nombre ne soit jamais séparé de son unité. */
    const val NBSP = ' '

    fun time(time: LocalTime): String {
        val minutes = time.minute
        return if (minutes == 0) "${time.hour}${NBSP}h" else "${time.hour}${NBSP}h${NBSP}${minutes.twoDigits()}"
    }

    fun duration(duration: Duration): String {
        val total = duration.toMinutes()
        val hours = total / 60
        val minutes = total % 60
        return when {
            hours == 0L -> "$minutes${NBSP}min"
            minutes == 0L -> "$hours${NBSP}h"
            else -> "$hours${NBSP}h${NBSP}${minutes.toInt().twoDigits()}"
        }
    }

    /** Écart de durée, toujours positif : « 15 minutes », « 1 heure », « 1 h 15 ». */
    fun durationInWords(duration: Duration): String {
        val total = duration.abs().toMinutes()
        return when {
            total < 60 -> "$total${NBSP}minute${if (total > 1) "s" else ""}"
            total % 60 == 0L -> "${total / 60}${NBSP}heure${if (total >= 120) "s" else ""}"
            else -> duration(duration.abs())
        }
    }

    fun percent(ratio: Double): String = "${(ratio * 100).roundToInt()}${NBSP}%"

    fun dayName(day: DayOfWeek): String = DAYS[day.value - 1]

    /** « Mardi 3 novembre » */
    fun longDate(date: LocalDate): String =
        "${dayName(date.dayOfWeek).replaceFirstChar { it.uppercase() }} ${date.dayOfMonth} ${monthName(date.month)}"

    /** « Nuit de lundi à mardi », pour la nuit qui se termine le matin de [nightOf]. */
    fun nightLabel(nightOf: LocalDate): String =
        "Nuit de ${dayName(nightOf.minusDays(1).dayOfWeek)} à ${dayName(nightOf.dayOfWeek)}"

    private fun monthName(month: Month): String = MONTHS[month.value - 1]

    private fun Int.twoDigits(): String = if (this < 10) "0$this" else "$this"

    private val DAYS = listOf("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche")
    private val MONTHS = listOf(
        "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre",
    )
}
