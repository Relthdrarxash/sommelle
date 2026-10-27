package fr.sommelle.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.designsystem.ScreenScaffold
import fr.sommelle.core.designsystem.SommelleButton
import fr.sommelle.core.designsystem.SommelleButtonStyle
import fr.sommelle.core.designsystem.SommelleColors
import fr.sommelle.core.designsystem.SommelleIcons
import fr.sommelle.core.model.Pathway
import fr.sommelle.core.model.SleepWindow
import fr.sommelle.core.presentation.FrenchFormat
import fr.sommelle.core.presentation.WeekBadge
import fr.sommelle.core.presentation.WindowStep
import java.time.LocalDate

@Composable
fun HomeScreen(
    repository: SommelleRepository,
    onFillNight: (LocalDate) -> Unit,
    onChooseWakeTime: () -> Unit,
    onCantSleep: () -> Unit,
) {
    val viewModel: HomeViewModel = viewModel { HomeViewModel(repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return
    HomeContent(s, onFillNight, onChooseWakeTime, onCantSleep)
}

/** Accueil sans état ni dépendance : affiché tel quel dans les captures d'écran de la documentation. */
@Composable
fun HomeContent(
    s: HomeState,
    onFillNight: (LocalDate) -> Unit,
    onChooseWakeTime: () -> Unit,
    onCantSleep: () -> Unit,
) {
    val diaryAvailable = s.pathway != Pathway.OUT_OF_SCOPE
    ScreenScaffold(
        bottom = {
            if (diaryAvailable) {
                SommelleButton(
                    text = stringResource(R.string.home_cant_sleep),
                    onClick = onCantSleep,
                    style = SommelleButtonStyle.Outlined,
                    icon = SommelleIcons.Moon,
                )
            }
        },
    ) {
        Header(s)
        ProgramCard(s, onChooseWakeTime)
        if (diaryAvailable) DiarySection(s, onFillNight)
    }
}

@Composable
private fun Header(s: HomeState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                s.date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = when (s.badge) {
                        WeekBadge.OBSERVATION -> stringResource(R.string.home_badge_observation)
                        WeekBadge.PROGRAM_WEEK -> stringResource(R.string.home_badge_week, s.week)
                        WeekBadge.MAINTENANCE -> stringResource(R.string.home_badge_maintenance)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
        Text(
            stringResource(R.string.home_hello),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun ProgramCard(s: HomeState, onChooseWakeTime: () -> Unit) {
    when (val step = s.step) {
        is WindowStep.Current -> WindowCard(step.window)
        is WindowStep.Baseline -> InfoCard(
            title = stringResource(R.string.home_baseline_title),
            text = pluralStringResource(R.plurals.home_baseline_text, step.daysLeft, step.daysLeft),
        )
        is WindowStep.BaselineExtended -> InfoCard(
            title = stringResource(R.string.home_extended_title),
            text = stringResource(R.string.home_extended_text, step.loggedNights, step.requiredNights),
        )
        is WindowStep.NeedsWakeTime -> InfoCard(
            title = stringResource(R.string.home_wake_title),
            text = stringResource(R.string.home_wake_text),
        ) {
            SommelleButton(stringResource(R.string.home_wake_button), onChooseWakeTime)
        }
        else -> when (s.pathway) {
            Pathway.OUT_OF_SCOPE -> InfoCard(stringResource(R.string.home_out_title), stringResource(R.string.home_out_text))
            else -> InfoCard(stringResource(R.string.home_partial_title), stringResource(R.string.home_partial_text))
        }
    }
}

/** Carte bleu nuit de la maquette : coucher et lever en grand, et la raison en une phrase. */
@Composable
private fun WindowCard(window: SleepWindow) {
    val bed = FrenchFormat.time(window.bedTime)
    Surface(shape = MaterialTheme.shapes.large, color = SommelleColors.Night, contentColor = SommelleColors.Cream) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(SommelleIcons.Moon, contentDescription = null, tint = SommelleColors.Moon, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(R.string.home_window_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeTile(stringResource(R.string.home_bedtime), bed, Modifier.weight(1f))
                TimeTile(stringResource(R.string.home_waketime), FrenchFormat.time(window.wakeTime), Modifier.weight(1f))
            }
            Text(stringResource(R.string.home_window_reason, bed), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TimeTile(label: String, value: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, color = Color.White.copy(alpha = 0.12f), contentColor = SommelleColors.Cream) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = SommelleColors.LavenderOnNight)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun InfoCard(title: String, text: String, action: (@Composable () -> Unit)? = null) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            action?.invoke()
        }
    }
}

@Composable
private fun DiarySection(s: HomeState, onFillNight: (LocalDate) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val missing = s.nightsToFill.firstOrNull()
        if (missing != null) {
            Text(
                stringResource(R.string.home_night_missing, FrenchFormat.nightLabel(missing).replaceFirstChar { it.lowercase() }),
                style = MaterialTheme.typography.bodyLarge,
            )
            SommelleButton(
                text = stringResource(R.string.home_fill_night),
                onClick = { onFillNight(missing) },
                style = SommelleButtonStyle.Accent,
                icon = Icons.Filled.Edit,
            )
        } else {
            Text(stringResource(R.string.home_night_done), style = MaterialTheme.typography.bodyLarge)
            SommelleButton(
                text = stringResource(R.string.home_fix_night),
                onClick = { onFillNight(s.lastNight) },
                style = SommelleButtonStyle.Quiet,
            )
        }
    }
}
