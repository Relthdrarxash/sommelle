package fr.sommelle.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.designsystem.BackPill
import fr.sommelle.core.designsystem.ChoiceGrid
import fr.sommelle.core.designsystem.InfoLine
import fr.sommelle.core.designsystem.ScreenScaffold
import fr.sommelle.core.designsystem.ScreenTitle
import fr.sommelle.core.designsystem.SommelleButton
import fr.sommelle.core.designsystem.SommelleButtonStyle
import fr.sommelle.core.designsystem.StepProgress
import fr.sommelle.core.designsystem.YesNoButtons
import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.Notice
import fr.sommelle.core.model.Pathway
import fr.sommelle.core.model.Referral
import fr.sommelle.core.presentation.ScreeningQuestion

@Composable
fun OnboardingScreen(repository: SommelleRepository, onDone: () -> Unit) {
    val viewModel: OnboardingViewModel = viewModel { OnboardingViewModel(repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state != OnboardingState.Welcome) { viewModel.back() }

    OnboardingContent(
        state = state,
        onStart = viewModel::start,
        onChooseAge = viewModel::chooseAge,
        onAnswer = viewModel::answer,
        onBack = viewModel::back,
        onConfirm = { viewModel.confirm(onDone) },
    )
}

/** Questionnaire d'entrée sans état ni dépendance : affiché tel quel dans les captures de la documentation. */
@Composable
fun OnboardingContent(
    state: OnboardingState,
    onStart: () -> Unit,
    onChooseAge: (AgeBand) -> Unit,
    onAnswer: (Boolean) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    when (val s = state) {
        OnboardingState.Welcome -> Welcome(onStart = onStart)
        is OnboardingState.Questions -> if (s.flow.isAgeStep) {
            AgeStep(
                step = s.flow.stepNumber,
                total = s.flow.totalSteps,
                selected = s.flow.answers?.ageBand,
                onBack = onBack,
                onSelect = onChooseAge,
            )
        } else {
            QuestionStep(
                question = checkNotNull(s.flow.current),
                step = s.flow.stepNumber,
                total = s.flow.totalSteps,
                onBack = onBack,
                onAnswer = onAnswer,
            )
        }
        is OnboardingState.Result -> ResultStep(
            pathway = s.result.pathway,
            referrals = s.result.referrals,
            notices = s.result.notices,
            onBack = onBack,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun Welcome(onStart: () -> Unit) {
    ScreenScaffold(
        bottom = { SommelleButton(stringResource(R.string.onb_welcome_start), onStart) },
    ) {
        ScreenTitle(stringResource(R.string.onb_welcome_title))
        Body(R.string.onb_welcome_1)
        Body(R.string.onb_welcome_2)
        Body(R.string.onb_welcome_3)
        Body(R.string.onb_welcome_4)
        Body(R.string.onb_intro_questions)
    }
}

@Composable
private fun AgeStep(step: Int, total: Int, selected: AgeBand?, onBack: () -> Unit, onSelect: (AgeBand) -> Unit) {
    ScreenScaffold {
        BackPill(onBack)
        StepProgress(step, total)
        ScreenTitle(stringResource(R.string.onb_age_title))
        ChoiceGrid(
            options = AgeBand.entries,
            selected = selected,
            label = { stringResource(it.label()) },
            onSelect = onSelect,
            columns = 1,
        )
    }
}

@Composable
private fun QuestionStep(question: ScreeningQuestion, step: Int, total: Int, onBack: () -> Unit, onAnswer: (Boolean) -> Unit) {
    ScreenScaffold(
        bottom = { YesNoButtons(onYes = { onAnswer(true) }, onNo = { onAnswer(false) }) },
    ) {
        BackPill(onBack)
        StepProgress(step, total)
        ScreenTitle(stringResource(question.text()))
        question.hint()?.let { Body(it) }
        if (question == ScreeningQuestion.DISTRESS) Body(R.string.onb_h_private)
    }
}

@Composable
private fun ResultStep(
    pathway: Pathway,
    referrals: Set<Referral>,
    notices: Set<Notice>,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val (title, text) = when (pathway) {
        Pathway.FULL -> R.string.onb_result_full_title to R.string.onb_result_full_text
        Pathway.WITHOUT_RESTRICTION -> R.string.onb_result_partial_title to R.string.onb_result_partial_text
        Pathway.OUT_OF_SCOPE -> R.string.onb_result_out_title to R.string.onb_result_out_text
    }
    ScreenScaffold(
        bottom = {
            if (Notice.CRISIS_RESOURCES in notices) {
                SommelleButton(
                    text = stringResource(R.string.onb_call_3114),
                    onClick = { uriHandler.openUri("tel:3114") },
                    style = SommelleButtonStyle.Outlined,
                )
            }
            SommelleButton(
                text = stringResource(if (pathway == Pathway.OUT_OF_SCOPE) R.string.onb_understood else R.string.onb_start_program),
                onClick = onConfirm,
            )
        },
    ) {
        BackPill(onBack)
        ScreenTitle(stringResource(title))
        Body(text)
        val messages = referrals.map { it.text() } + notices.map { it.text() }
        if (messages.isNotEmpty()) {
            Text(
                text = stringResource(R.string.onb_result_good_to_know),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                messages.forEach { message ->
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                        InfoLine(Icons.Filled.Info, stringResource(message), Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Body(@StringRes text: Int) {
    Text(stringResource(text), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
}

@StringRes
private fun AgeBand.label(): Int = when (this) {
    AgeBand.UNDER_18 -> R.string.onb_age_under_18
    AgeBand.FROM_18_TO_24 -> R.string.onb_age_18_24
    AgeBand.FROM_25_TO_44 -> R.string.onb_age_25_44
    AgeBand.FROM_45_TO_64 -> R.string.onb_age_45_64
    AgeBand.FROM_65 -> R.string.onb_age_65
}

@StringRes
private fun ScreeningQuestion.text(): Int = when (this) {
    ScreeningQuestion.SHIFT_WORK -> R.string.onb_q_shift_work
    ScreeningQuestion.EXTERNAL_DISRUPTION -> R.string.onb_q_external
    ScreeningQuestion.SNORING_PAUSES -> R.string.onb_q_snoring
    ScreeningQuestion.DAYTIME_SLEEPINESS -> R.string.onb_q_sleepiness
    ScreeningQuestion.BIPOLAR -> R.string.onb_q_bipolar
    ScreeningQuestion.EPILEPSY -> R.string.onb_q_epilepsy
    ScreeningQuestion.PREGNANCY -> R.string.onb_q_pregnancy
    ScreeningQuestion.SAFETY_CRITICAL -> R.string.onb_q_safety
    ScreeningQuestion.PREFERS_NO_RESTRICTION -> R.string.onb_q_prefers_no_restriction
    ScreeningQuestion.LATE_SCHEDULE -> R.string.onb_q_late
    ScreeningQuestion.DISTRESS -> R.string.onb_q_distress
}

@StringRes
private fun ScreeningQuestion.hint(): Int? = when (this) {
    ScreeningQuestion.SHIFT_WORK -> R.string.onb_h_shift_work
    ScreeningQuestion.EXTERNAL_DISRUPTION -> R.string.onb_h_external
    ScreeningQuestion.DAYTIME_SLEEPINESS -> R.string.onb_h_sleepiness
    ScreeningQuestion.SAFETY_CRITICAL -> R.string.onb_h_safety
    ScreeningQuestion.PREFERS_NO_RESTRICTION -> R.string.onb_h_prefers_no_restriction
    else -> null
}

@StringRes
private fun Referral.text(): Int = when (this) {
    Referral.MINOR -> R.string.onb_ref_minor
    Referral.SLEEP_APNEA_SUSPECTED -> R.string.onb_ref_apnea
    Referral.EXCESSIVE_DAYTIME_SLEEPINESS -> R.string.onb_ref_sleepiness
    Referral.BIPOLAR_DISORDER -> R.string.onb_ref_bipolar
    Referral.EPILEPSY -> R.string.onb_ref_epilepsy
    Referral.DELAYED_SLEEP_PHASE_SUSPECTED -> R.string.onb_ref_delayed
    Referral.SHIFT_WORK -> R.string.onb_ref_shift
}

@StringRes
private fun Notice.text(): Int = when (this) {
    Notice.REINFORCED_DROWSINESS_WARNING -> R.string.onb_notice_drowsiness
    Notice.EXTERNAL_DISRUPTION -> R.string.onb_notice_external
    Notice.PREGNANCY -> R.string.onb_notice_pregnancy
    Notice.CRISIS_RESOURCES -> R.string.onb_notice_crisis
}
