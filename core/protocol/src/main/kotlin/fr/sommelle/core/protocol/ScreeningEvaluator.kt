package fr.sommelle.core.protocol

import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.Notice
import fr.sommelle.core.model.Pathway
import fr.sommelle.core.model.Referral
import fr.sommelle.core.model.ScreeningAnswers
import fr.sommelle.core.model.ScreeningResult
import java.time.Duration

/**
 * Traduit les réponses au questionnaire d'entrée en parcours, plancher de fenêtre, orientations et messages.
 * Règles : tableau « Sécurité » de documentation/protocole.md. Contre-indications de la restriction
 * du temps au lit d'après Kyle et al., Sleep 2014, et Riemann et al., J Sleep Res 2023 (à valider).
 *
 * Ordre de priorité : hors périmètre, puis contre-indications à la restriction, puis programme complet.
 */
class ScreeningEvaluator(private val config: ProtocolConfig = ProtocolConfig.DEFAULT) {

    fun evaluate(answers: ScreeningAnswers): ScreeningResult {
        val referrals = mutableSetOf<Referral>()
        val notices = mutableSetOf<Notice>()

        if (answers.expressedDistress) notices += Notice.CRISIS_RESOURCES
        if (answers.pregnancy) notices += Notice.PREGNANCY

        if (answers.loudSnoringWithBreathingPauses) referrals += Referral.SLEEP_APNEA_SUSPECTED
        if (answers.markedDaytimeSleepiness) referrals += Referral.EXCESSIVE_DAYTIME_SLEEPINESS
        if (answers.bipolarDisorder) referrals += Referral.BIPOLAR_DISORDER
        if (answers.epilepsy) referrals += Referral.EPILEPSY
        if (answers.lateScheduleSleepsWellOnceAsleep) referrals += Referral.DELAYED_SLEEP_PHASE_SUSPECTED

        val outOfScope = when {
            answers.ageBand == AgeBand.UNDER_18 -> {
                referrals += Referral.MINOR
                true
            }
            answers.shiftOrNightWork -> {
                referrals += Referral.SHIFT_WORK
                true
            }
            answers.externalNightDisruption -> {
                notices += Notice.EXTERNAL_DISRUPTION
                true
            }
            else -> false
        }
        if (outOfScope) return result(Pathway.OUT_OF_SCOPE, answers, null, referrals, notices)

        val restrictionContraindicated = answers.loudSnoringWithBreathingPauses ||
            answers.markedDaytimeSleepiness ||
            answers.bipolarDisorder ||
            answers.epilepsy ||
            answers.pregnancy ||
            answers.lateScheduleSleepsWellOnceAsleep ||
            (answers.safetyCriticalActivity && answers.prefersNoRestriction)

        if (restrictionContraindicated) {
            return result(Pathway.WITHOUT_RESTRICTION, answers, null, referrals, notices)
        }

        val floor = if (answers.safetyCriticalActivity) {
            notices += Notice.REINFORCED_DROWSINESS_WARNING
            config.safetyCriticalMinimumWindow
        } else {
            config.defaultMinimumWindow
        }
        return result(Pathway.FULL, answers, floor, referrals, notices)
    }

    private fun result(
        pathway: Pathway,
        answers: ScreeningAnswers,
        floor: Duration?,
        referrals: Set<Referral>,
        notices: Set<Notice>,
    ) = ScreeningResult(
        pathway = pathway,
        ageBand = answers.ageBand,
        minimumSleepWindow = floor,
        referrals = referrals.toSet(),
        notices = notices.toSet(),
    )
}
