package fr.sommelle.feature.week

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.designsystem.ScreenScaffold
import fr.sommelle.core.designsystem.ScreenTitle
import fr.sommelle.core.designsystem.SommelleButton
import fr.sommelle.core.designsystem.SommelleButtonStyle
import fr.sommelle.core.designsystem.SommelleColors
import fr.sommelle.core.presentation.ReportTone
import fr.sommelle.core.presentation.WeekBar
import fr.sommelle.core.presentation.WeeklyReport

@Composable
fun WeekScreen(repository: SommelleRepository) {
    val viewModel: WeekViewModel = viewModel { WeekViewModel(repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return
    var showHow by rememberSaveable { mutableStateOf(false) }

    ScreenScaffold {
        Text(stringResource(R.string.week_label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val (title, text) = headline(s)
        ScreenTitle(title)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)

        if (s.bedTime != null && s.wakeTime != null) WindowCard(s.bedTime, s.wakeTime)

        Chart(s.bars, s.chartDescription)

        if (s.restriction) {
            SommelleButton(
                text = stringResource(R.string.week_how_title),
                onClick = { showHow = !showHow },
                style = SommelleButtonStyle.Quiet,
            )
            if (showHow) {
                Text(stringResource(R.string.week_how_text), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun headline(s: WeekState): Pair<String, String> {
    val report: WeeklyReport? = s.report
    return when {
        !s.restriction -> stringResource(R.string.week_partial_title) to stringResource(R.string.week_partial_text)
        report != null -> {
            val eff = report.efficiency.orEmpty()
            val change = report.change.orEmpty()
            when (report.tone) {
                ReportTone.LONGER -> stringResource(R.string.week_longer_title) to stringResource(R.string.week_longer_text, eff, change)
                ReportTone.SHORTER -> stringResource(R.string.week_shorter_title) to stringResource(R.string.week_shorter_text, eff, change)
                ReportTone.STEADY -> stringResource(R.string.week_steady_title) to stringResource(R.string.week_steady_text, eff)
                ReportTone.AT_FLOOR -> stringResource(R.string.week_floor_title) to stringResource(R.string.week_floor_text, eff)
                ReportTone.AT_MAXIMUM -> stringResource(R.string.week_max_title) to stringResource(R.string.week_max_text, eff)
                ReportTone.MISSING_NIGHTS -> stringResource(R.string.week_missing_title) to
                    stringResource(R.string.week_missing_text, report.loggedNights, report.requiredNights)
            }
        }
        s.window != null -> stringResource(R.string.week_first_title) to stringResource(R.string.week_first_text)
        else -> stringResource(R.string.week_pending_title) to stringResource(R.string.week_pending_text)
    }
}

@Composable
private fun WindowCard(bed: String, wake: String) {
    Surface(shape = MaterialTheme.shapes.large, color = SommelleColors.Night, contentColor = SommelleColors.Cream) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.week_window_current), style = MaterialTheme.typography.bodySmall, color = SommelleColors.LavenderOnNight)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.week_bedtime), style = MaterialTheme.typography.bodySmall, color = SommelleColors.LavenderOnNight)
                    Text(bed, style = MaterialTheme.typography.headlineMedium)
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.week_waketime), style = MaterialTheme.typography.bodySmall, color = SommelleColors.LavenderOnNight)
                    Text(wake, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }
}

/**
 * Barres horizontales « au lit » (contour) et « dormi » (plein) : les deux séries se distinguent
 * par la forme, pas seulement par la couleur. TalkBack lit une description complète à la place.
 */
@Composable
private fun Chart(bars: List<WeekBar>, description: String) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.large, color = colors.surface, shadowElevation = 1.dp) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.week_chart_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            if (bars.isEmpty()) {
                Text(stringResource(R.string.week_chart_empty), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                return@Column
            }
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.clearAndSetSemantics { }) {
                Legend(outlined = true, label = stringResource(R.string.week_legend_bed))
                Legend(outlined = false, label = stringResource(R.string.week_legend_sleep))
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.clearAndSetSemantics { contentDescription = description },
            ) {
                bars.forEach { bar ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(bar.label, style = MaterialTheme.typography.titleSmall)
                        BarRow(bar.timeInBedFraction, bar.timeInBed, outlined = true)
                        BarRow(bar.totalSleepFraction, bar.totalSleep, outlined = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun BarRow(fraction: Float, value: String, outlined: Boolean) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(6.dp)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0.02f, 1f) * 0.75f)
                .height(if (outlined) 14.dp else 18.dp)
                .then(
                    if (outlined) Modifier.border(2.dp, colors.primary, shape).background(colors.surface, shape)
                    else Modifier.background(colors.primary, shape),
                ),
        )
        Spacer(Modifier.width(8.dp))
        Text(value, style = if (outlined) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun Legend(outlined: Boolean, label: String) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(4.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(width = 20.dp, height = 14.dp)
                .then(
                    if (outlined) Modifier.border(2.dp, colors.primary, shape)
                    else Modifier.background(colors.primary, shape),
                ),
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}
