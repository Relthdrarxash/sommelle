package fr.sommelle.core.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Une nuit de l'agenda du sommeil, calquée sur la version Core du Consensus Sleep Diary
 * (Carney et al., Sleep 2012). Les numéros de question renvoient à ce questionnaire.
 *
 * Une entrée enregistrée est toujours une entrée validée par l'utilisateur : les valeurs
 * pré-remplies (montre, signaux d'écran, veille) ne deviennent des données qu'après validation.
 */
data class DiaryEntry(
    /** Date du matin où la nuit se termine. Sert d'identifiant de la nuit. */
    val nightOf: LocalDate,
    /** Q1. Heure à laquelle la personne s'est mise au lit. */
    val bedTime: LocalDateTime,
    /** Q2. Heure à laquelle elle a essayé de s'endormir (extinction). */
    val lightsOutTime: LocalDateTime,
    /** Q3. Temps estimé pour s'endormir. */
    val sleepOnsetLatency: Duration,
    /** Q4. Nombre de réveils dans la nuit, sans compter le réveil final. */
    val awakeningCount: Int,
    /** Q5. Durée totale estimée de ces réveils. */
    val wakeAfterSleepOnset: Duration,
    /** Q6. Heure du réveil final. */
    val finalWakeTime: LocalDateTime,
    /** Q7. Heure de sortie du lit. */
    val outOfBedTime: LocalDateTime,
    /** Q8. Qualité ressentie. */
    val quality: SleepQuality,
    /** Q9. Commentaire libre, facultatif. */
    val notes: String = "",
    /** Source utilisée pour pré-remplir le formulaire avant validation, s'il y en a une. */
    val prefillSource: PrefillSource? = null,
)

/** Q8 du Consensus Sleep Diary, échelle en 5 points. */
enum class SleepQuality(val score: Int) {
    VERY_POOR(1),
    POOR(2),
    FAIR(3),
    GOOD(4),
    VERY_GOOD(5),
}

/** Origine des valeurs proposées à l'utilisateur avant qu'il valide son agenda. */
enum class PrefillSource {
    HEALTH_CONNECT,
    SCREEN_EVENTS,
    PREVIOUS_NIGHT,
}
