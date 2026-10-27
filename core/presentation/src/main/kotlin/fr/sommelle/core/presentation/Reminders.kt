package fr.sommelle.core.presentation

import fr.sommelle.core.model.SleepWindow
import java.time.LocalDateTime
import java.time.LocalTime

/** Réglages des rappels, choisis par la personne. Désactivés tant qu'elle ne les a pas activés. */
data class ReminderSettings(
    val morningEnabled: Boolean = false,
    /** Heure du rappel du matin ; par défaut, 30 minutes après l'heure de lever. */
    val morningTime: LocalTime? = null,
    val eveningEnabled: Boolean = false,
)

enum class ReminderKind { MORNING, EVENING }

/** Calcul des heures de rappel, sans dépendance Android. */
object ReminderPlanner {

    private val DEFAULT_MORNING: LocalTime = LocalTime.of(8, 0)
    private const val MORNING_DELAY_MINUTES = 30L
    private const val EVENING_ADVANCE_MINUTES = 30L

    fun morningTime(settings: ReminderSettings, wakeTime: LocalTime?): LocalTime =
        settings.morningTime ?: wakeTime?.plusMinutes(MORNING_DELAY_MINUTES) ?: DEFAULT_MORNING

    /** Rappel du soir : 30 minutes avant le début de la fenêtre, seulement quand il y en a une. */
    fun eveningTime(window: SleepWindow?): LocalTime? = window?.bedTime?.minusMinutes(EVENING_ADVANCE_MINUTES)

    /** Prochaine occurrence de [time] strictement après [now]. */
    fun next(now: LocalDateTime, time: LocalTime): LocalDateTime {
        val today = now.toLocalDate().atTime(time)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }
}
