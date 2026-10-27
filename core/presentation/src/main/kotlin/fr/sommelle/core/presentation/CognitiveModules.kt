package fr.sommelle.core.presentation

/**
 * Modules cognitifs de la TCC-I, proposés une fois la restriction et le contrôle du stimulus installés
 * (docs/protocole.html, section 2). Le contenu est dans les ressources du module d'interface.
 *
 * Sources : Morin et Espie, Insomnia: A Clinical Guide to Assessment and Treatment, 2003 ;
 * Harvey, A cognitive model of insomnia, Behav Res Ther, 2002 ; Riemann et al., J Sleep Res, 2023.
 */
enum class CognitiveModule(val week: Int, val exerciseSteps: Int) {
    /** Repérer et nuancer les croyances qui entretiennent l'insomnie. */
    BELIEFS(week = 2, exerciseSteps = 5),
    /** Mettre sa journée au repos en début de soirée, pour ne pas emporter ses soucis au lit. */
    WIND_DOWN(week = 2, exerciseSteps = 4),
    /** Intention paradoxale : au lit, cesser de chercher le sommeil. */
    PARADOXICAL_INTENTION(week = 3, exerciseSteps = 3),
    /** Décatastrophiser la mauvaise nuit et ses conséquences. */
    DECATASTROPHIZING(week = 4, exerciseSteps = 4),
    /** Relaxation musculaire progressive. */
    RELAXATION(week = 5, exerciseSteps = 6);

    companion object {
        /** Modules accessibles à une semaine donnée du programme. */
        fun available(programWeek: Int?): List<CognitiveModule> = entries.filter { (programWeek ?: 0) >= it.week }

        /** Prochain module à débloquer, s'il en reste. */
        fun nextLocked(programWeek: Int?): CognitiveModule? = entries.firstOrNull { (programWeek ?: 0) < it.week }
    }
}
