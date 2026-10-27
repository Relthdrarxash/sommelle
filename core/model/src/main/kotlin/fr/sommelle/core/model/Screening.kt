package fr.sommelle.core.model

import java.time.Duration

/** Tranches d'âge demandées au questionnaire d'entrée. L'âge exact n'est jamais demandé. */
enum class AgeBand {
    UNDER_18,
    FROM_18_TO_24,
    FROM_25_TO_44,
    FROM_45_TO_64,
    FROM_65,
}

/**
 * Réponses au questionnaire d'entrée. Chaque champ correspond à une ligne du tableau
 * « Sécurité » de docs/protocole.md.
 */
data class ScreeningAnswers(
    val ageBand: AgeBand,
    /** Ronflements forts avec pauses respiratoires constatées par l'entourage. */
    val loudSnoringWithBreathingPauses: Boolean = false,
    /** Endormissements involontaires dans la journée (réunion, transport, volant). */
    val markedDaytimeSleepiness: Boolean = false,
    val bipolarDisorder: Boolean = false,
    val epilepsy: Boolean = false,
    val pregnancy: Boolean = false,
    /** Conduite professionnelle, machines dangereuses ou métier de sécurité. */
    val safetyCriticalActivity: Boolean = false,
    /** Choix de la personne concernée par l'activité ci-dessus de ne pas restreindre son temps au lit. */
    val prefersNoRestriction: Boolean = false,
    val shiftOrNightWork: Boolean = false,
    /** Nuits interrompues par une cause externe : nourrisson, soins à un proche, etc. */
    val externalNightDisruption: Boolean = false,
    /** Coucher et lever très tardifs, mais sommeil normal une fois endormi. */
    val lateScheduleSleepsWellOnceAsleep: Boolean = false,
    /** Détresse psychique exprimée pendant le questionnaire. */
    val expressedDistress: Boolean = false,
)

/** Parcours proposé à l'issue du questionnaire. */
enum class Pathway {
    /** Programme complet, restriction du temps au lit comprise. */
    FULL,
    /** Programme sans restriction : contrôle du stimulus, modules cognitifs, éducation. */
    WITHOUT_RESTRICTION,
    /** Hors du périmètre de l'appli : orientation et contenu éducatif seulement. */
    OUT_OF_SCOPE,
}

/** Raisons d'orienter vers un professionnel de santé. */
enum class Referral {
    MINOR,
    SLEEP_APNEA_SUSPECTED,
    EXCESSIVE_DAYTIME_SLEEPINESS,
    BIPOLAR_DISORDER,
    EPILEPSY,
    DELAYED_SLEEP_PHASE_SUSPECTED,
    SHIFT_WORK,
}

/** Messages à afficher selon les réponses, sans valeur de diagnostic. */
enum class Notice {
    /** Avertissement somnolence renforcé pour les activités à risque. */
    REINFORCED_DROWSINESS_WARNING,
    /** La TCC-I cible l'insomnie, pas la privation de sommeil liée à une cause externe. */
    EXTERNAL_DISRUPTION,
    PREGNANCY,
    /** Mise en avant des ressources d'aide (3114), en plus de leur présence permanente. */
    CRISIS_RESOURCES,
}

data class ScreeningResult(
    val pathway: Pathway,
    val ageBand: AgeBand,
    /** Plancher de la fenêtre de sommeil, ou null si la restriction n'est pas proposée. */
    val minimumSleepWindow: Duration?,
    val referrals: Set<Referral>,
    val notices: Set<Notice>,
) {
    val restrictionAllowed: Boolean get() = pathway == Pathway.FULL && minimumSleepWindow != null
}
