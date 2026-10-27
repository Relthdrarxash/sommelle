package fr.sommelle.core.protocol

import java.time.Duration

/**
 * Paramètres cliniques du protocole, regroupés en un seul endroit pour être relus et validés
 * par un professionnel du sommeil. Toute modification d'une valeur doit incrémenter [version].
 *
 * Sources : Spielman et al., Sleep 1987 ; Edinger et al., JCSM 2021 ; Riemann et al., J Sleep Res 2023 ;
 * Kyle et al., Sleep 2014. Les valeurs marquées « à valider » sont des choix de conception.
 */
data class ProtocolConfig(
    val version: Int,
    /** Durée de la ligne de base avant toute restriction. */
    val baselineDays: Int,
    /** Nombre minimum de nuits renseignées sur 7 pour calculer ou ajuster une fenêtre. */
    val minimumLoggedNightsPerWeek: Int,
    /** Plancher par défaut de la fenêtre de sommeil (à valider). */
    val defaultMinimumWindow: Duration,
    /** Plancher pour les activités à risque de somnolence (à valider). */
    val safetyCriticalMinimumWindow: Duration,
    /** Limite absolue : aucun réglage ne peut descendre en dessous. */
    val absoluteMinimumWindow: Duration,
    /** Durée maximale de la fenêtre, au-delà de laquelle on cesse d'allonger. */
    val maximumWindow: Duration,
    /** Pas d'arrondi de la fenêtre initiale (arrondi au pas supérieur). */
    val roundingStep: Duration,
    val increaseStep: Duration,
    val decreaseStep: Duration,
    /** Efficacité à partir de laquelle on allonge la fenêtre. */
    val increaseThreshold: Double,
    /** Efficacité en dessous de laquelle on raccourcit la fenêtre. */
    val decreaseThreshold: Double,
    /** Abaissement des deux seuils pour les 65 ans et plus (à valider). */
    val seniorThresholdOffset: Double,
) {
    init {
        require(version > 0) { "version doit être positive" }
        require(baselineDays in 7..14) { "la ligne de base dure entre 7 et 14 jours" }
        require(minimumLoggedNightsPerWeek in 1..7)
        require(defaultMinimumWindow >= absoluteMinimumWindow) { "plancher sous la limite absolue" }
        require(safetyCriticalMinimumWindow >= defaultMinimumWindow) { "plancher de sécurité trop bas" }
        require(maximumWindow > safetyCriticalMinimumWindow)
        require(!roundingStep.isNegative && !roundingStep.isZero)
        require(!increaseStep.isNegative && !increaseStep.isZero)
        require(!decreaseStep.isNegative && !decreaseStep.isZero)
        require(decreaseThreshold < increaseThreshold) { "seuils incohérents" }
        require(increaseThreshold <= 1.0 && decreaseThreshold > 0.0)
        require(seniorThresholdOffset in 0.0..0.1)
        require(decreaseThreshold - seniorThresholdOffset > 0.0)
    }

    companion object {
        val DEFAULT = ProtocolConfig(
            version = 1,
            baselineDays = 7,
            minimumLoggedNightsPerWeek = 5,
            defaultMinimumWindow = Duration.ofMinutes(5 * 60 + 30),
            safetyCriticalMinimumWindow = Duration.ofHours(6),
            absoluteMinimumWindow = Duration.ofHours(5),
            maximumWindow = Duration.ofHours(9),
            roundingStep = Duration.ofMinutes(15),
            increaseStep = Duration.ofMinutes(15),
            decreaseStep = Duration.ofMinutes(15),
            increaseThreshold = 0.85,
            decreaseThreshold = 0.80,
            seniorThresholdOffset = 0.05,
        )
    }
}
