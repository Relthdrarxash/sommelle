package fr.sommelle.feature.diary

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
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
import fr.sommelle.core.designsystem.SommelleIcons
import fr.sommelle.core.designsystem.StepProgress
import fr.sommelle.core.designsystem.TimeStepper
import fr.sommelle.core.model.PrefillSource
import fr.sommelle.core.model.SleepQuality
import fr.sommelle.core.presentation.DiaryChoices
import fr.sommelle.core.presentation.DiaryDraft
import fr.sommelle.core.presentation.DiaryStep
import fr.sommelle.core.presentation.FrenchFormat
import fr.sommelle.core.presentation.TimeField
import fr.sommelle.core.protocol.DiaryIssue
import java.time.Duration
import java.time.LocalDate

@Composable
fun DiaryScreen(repository: SommelleRepository, nightOf: LocalDate, onClose: () -> Unit) {
    val viewModel: DiaryViewModel = viewModel(key = "diary-$nightOf") { DiaryViewModel(repository, nightOf) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return

    val goBack = { if (!viewModel.back()) onClose() }
    BackHandler(onBack = goBack)

    when (s.mode) {
        DiaryMode.SUMMARY -> Summary(
            state = s,
            onEdit = viewModel::editFromSummary,
            onConfirm = { viewModel.save(onClose) },
            onGuided = viewModel::guidedFromStart,
            onBack = onClose,
            onFixIssue = { step -> viewModel.dismissIssue(); viewModel.editFromSummary(step) },
        )
        DiaryMode.GUIDED -> Guided(
            state = s,
            viewModel = viewModel,
            onBack = goBack,
            onNext = { viewModel.next(onClose) },
            onFixIssue = { step -> viewModel.dismissIssue(); viewModel.editFromSummary(step) },
        )
    }
}

@Composable
private fun Guided(state: DiaryState, viewModel: DiaryViewModel, onBack: () -> Unit, onNext: () -> Unit, onFixIssue: (DiaryStep) -> Unit) {
    val draft = state.draft
    val isLast = state.step.next == null
    ScreenScaffold(
        bottom = {
            SommelleButton(
                text = stringResource(
                    when {
                        state.returnToSummary -> R.string.diary_back_to_summary
                        isLast -> R.string.diary_save
                        else -> R.string.diary_next
                    },
                ),
                onClick = onNext,
                icon = if (isLast && !state.returnToSummary) Icons.Filled.Check else null,
            )
        },
    ) {
        BackPill(onBack)
        StepProgress(state.step.number, DiaryStep.total)
        state.blockingIssue?.let { IssueCard(it, onFixIssue) }
        when (state.step) {
            DiaryStep.BED_TIME -> TimeQuestion(R.string.diary_q_bed, R.string.diary_h_bed, draft, TimeField.BED_TIME, viewModel)
            DiaryStep.LIGHTS_OUT -> TimeQuestion(R.string.diary_q_lights, R.string.diary_h_lights, draft, TimeField.LIGHTS_OUT, viewModel)
            DiaryStep.SLEEP_ONSET -> {
                Question(R.string.diary_q_onset, R.string.diary_h_onset)
                ChoiceGrid(
                    options = DiaryChoices.sleepOnsetMinutes,
                    selected = draft.sleepOnsetMinutes,
                    label = { onsetLabel(it) },
                    onSelect = viewModel::sleepOnset,
                )
            }
            DiaryStep.AWAKENINGS -> {
                Question(R.string.diary_q_awakenings, R.string.diary_h_awakenings)
                ChoiceGrid(
                    options = DiaryChoices.awakeningCounts,
                    selected = draft.awakeningCount,
                    label = { countLabel(it) },
                    onSelect = viewModel::awakeningCount,
                    columns = 3,
                )
                if (draft.awakeningCount > 0) {
                    Text(stringResource(R.string.diary_q_awake_minutes), style = MaterialTheme.typography.titleLarge)
                    ChoiceGrid(
                        options = DiaryChoices.awakeMinutes,
                        selected = draft.awakeMinutes,
                        label = { awakeLabel(it) },
                        onSelect = viewModel::awakeMinutes,
                    )
                }
            }
            DiaryStep.FINAL_WAKE -> TimeQuestion(R.string.diary_q_final, R.string.diary_h_final, draft, TimeField.FINAL_WAKE, viewModel)
            DiaryStep.OUT_OF_BED -> TimeQuestion(R.string.diary_q_out, null, draft, TimeField.OUT_OF_BED, viewModel)
            DiaryStep.QUALITY -> {
                Question(R.string.diary_q_quality, null)
                ChoiceGrid(
                    options = SleepQuality.entries,
                    selected = draft.quality,
                    label = { stringResource(it.label()) },
                    onSelect = viewModel::quality,
                    columns = 1,
                )
            }
        }
    }
}

@Composable
private fun Question(@StringRes title: Int, @StringRes hint: Int?) {
    ScreenTitle(stringResource(title))
    if (hint != null) {
        Text(stringResource(hint), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TimeQuestion(@StringRes title: Int, @StringRes hint: Int?, draft: DiaryDraft, field: TimeField, viewModel: DiaryViewModel) {
    Question(title, hint)
    val source = draft.prefillSource
    TimeStepper(
        value = FrenchFormat.time(draft.time(field)),
        onMinus = { viewModel.shift(field, -DiaryChoices.TIME_STEP_MINUTES) },
        onPlus = { viewModel.shift(field, DiaryChoices.TIME_STEP_MINUTES) },
        footer = if (source != null) {
            { PrefillLine(source) }
        } else {
            null
        },
    )
}

@Composable
private fun Summary(
    state: DiaryState,
    onEdit: (DiaryStep) -> Unit,
    onConfirm: () -> Unit,
    onGuided: () -> Unit,
    onBack: () -> Unit,
    onFixIssue: (DiaryStep) -> Unit,
) {
    val d = state.draft
    val rows = listOf(
        Triple(DiaryStep.BED_TIME, stringResource(R.string.diary_row_bed), FrenchFormat.time(d.bedTime)),
        Triple(DiaryStep.LIGHTS_OUT, stringResource(R.string.diary_row_lights), FrenchFormat.time(d.lightsOut)),
        Triple(DiaryStep.SLEEP_ONSET, stringResource(R.string.diary_row_onset), onsetLabel(d.sleepOnsetMinutes)),
        Triple(
            DiaryStep.AWAKENINGS,
            stringResource(R.string.diary_row_awakenings),
            if (d.awakeningCount == 0) {
                stringResource(R.string.diary_none)
            } else {
                pluralStringResource(R.plurals.diary_awakenings_value, d.awakeningCount, d.awakeningCount, awakeLabel(d.awakeMinutes))
            },
        ),
        Triple(DiaryStep.FINAL_WAKE, stringResource(R.string.diary_row_final), FrenchFormat.time(d.finalWake)),
        Triple(DiaryStep.OUT_OF_BED, stringResource(R.string.diary_row_out), FrenchFormat.time(d.outOfBed)),
        Triple(DiaryStep.QUALITY, stringResource(R.string.diary_row_quality), stringResource(d.quality.label())),
    )
    ScreenScaffold(
        bottom = {
            SommelleButton(stringResource(R.string.diary_confirm), onConfirm, icon = Icons.Filled.Check)
            SommelleButton(stringResource(R.string.diary_guided), onGuided, style = SommelleButtonStyle.Quiet)
        },
    ) {
        BackPill(onBack)
        Text(FrenchFormat.nightLabel(d.nightOf), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ScreenTitle(stringResource(R.string.diary_summary_title))
        Text(stringResource(R.string.diary_summary_hint), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        state.blockingIssue?.let { IssueCard(it, onFixIssue) }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Column {
                rows.forEachIndexed { index, (step, label, value) ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SummaryRow(label, value, onClick = { onEdit(step) })
                }
            }
        }
        d.prefillSource?.let { PrefillLine(it) }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, onClick: () -> Unit) {
    val description = stringResource(R.string.diary_edit_row, label, value)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            }
            .padding(start = 20.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Text(value, style = MaterialTheme.typography.titleLarge)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun IssueCard(issue: DiaryIssue, onFix: (DiaryStep) -> Unit) {
    val (message, step) = when (issue) {
        DiaryIssue.AWAKE_TIME_EXCEEDS_SLEEP_PERIOD -> R.string.diary_issue_awake_too_long to DiaryStep.SLEEP_ONSET
        else -> R.string.diary_issue_order to DiaryStep.BED_TIME
    }
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(message), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            SommelleButton(stringResource(R.string.diary_issue_ok), { onFix(step) }, style = SommelleButtonStyle.Outlined)
        }
    }
}

@Composable
private fun PrefillLine(source: PrefillSource) {
    val (icon, text) = when (source) {
        PrefillSource.PREVIOUS_NIGHT -> SommelleIcons.History to R.string.diary_prefill_previous
        PrefillSource.HEALTH_CONNECT -> SommelleIcons.Watch to R.string.diary_prefill_watch
        PrefillSource.SCREEN_EVENTS -> SommelleIcons.History to R.string.diary_prefill_screen
    }
    InfoLine(icon, stringResource(text))
}

@Composable
private fun onsetLabel(minutes: Long): String = when (minutes) {
    DiaryChoices.sleepOnsetMinutes.first() -> stringResource(R.string.diary_less_than_5)
    DiaryChoices.sleepOnsetMinutes.last() -> stringResource(R.string.diary_onset_more)
    else -> FrenchFormat.duration(Duration.ofMinutes(minutes))
}

@Composable
private fun awakeLabel(minutes: Long): String = when (minutes) {
    DiaryChoices.awakeMinutes.last() -> stringResource(R.string.diary_awake_more)
    else -> FrenchFormat.duration(Duration.ofMinutes(minutes))
}

@Composable
private fun countLabel(count: Int): String = when (count) {
    0 -> stringResource(R.string.diary_none)
    DiaryChoices.awakeningCounts.last() -> stringResource(R.string.diary_five_or_more)
    else -> count.toString()
}

@StringRes
private fun SleepQuality.label(): Int = when (this) {
    SleepQuality.VERY_POOR -> R.string.diary_quality_1
    SleepQuality.POOR -> R.string.diary_quality_2
    SleepQuality.FAIR -> R.string.diary_quality_3
    SleepQuality.GOOD -> R.string.diary_quality_4
    SleepQuality.VERY_GOOD -> R.string.diary_quality_5
}
