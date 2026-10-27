package fr.sommelle.feature.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.designsystem.BackPill
import fr.sommelle.core.designsystem.ComparisonRow
import fr.sommelle.core.designsystem.ComparisonTable
import fr.sommelle.core.designsystem.InfoLine
import fr.sommelle.core.designsystem.ScreenScaffold
import fr.sommelle.core.designsystem.ScreenTitle
import fr.sommelle.core.designsystem.SommelleButton
import fr.sommelle.core.designsystem.SommelleButtonStyle
import fr.sommelle.core.designsystem.SommelleIcons
import fr.sommelle.core.presentation.ClinicianReport
import fr.sommelle.core.presentation.DiaryCsv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ExportState(val report: ClinicianReport, val csv: String)

class ExportViewModel(repository: SommelleRepository) : ViewModel() {
    val state: StateFlow<ExportState?> = combine(repository.snapshot.filterNotNull(), repository.daytimeImpacts) { snapshot, impacts ->
        ExportState(
            report = ClinicianReport.build(
                today = repository.today(),
                startDate = snapshot.program.startDate,
                screening = snapshot.program.screening,
                entries = snapshot.entries,
                windows = snapshot.windows,
                impacts = impacts,
            ),
            csv = DiaryCsv.export(snapshot.entries),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun ExportScreen(repository: SommelleRepository, onBack: () -> Unit) {
    val viewModel: ExportViewModel = viewModel { ExportViewModel(repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ExportContent(
        report = s.report,
        onBack = onBack,
        onSharePdf = {
            scope.launch {
                val file = withContext(Dispatchers.IO) { PdfReport.write(context, s.report) }
                PdfReport.share(context, file, "application/pdf")
            }
        },
        onShareCsv = {
            scope.launch {
                val file = withContext(Dispatchers.IO) { PdfReport.writeCsv(context, s.csv) }
                PdfReport.share(context, file, "text/csv")
            }
        },
    )
}

/** Résumé à l'écran, lisible directement par le médecin sur le téléphone. */
@Composable
fun ExportContent(report: ClinicianReport, onBack: () -> Unit, onSharePdf: () -> Unit, onShareCsv: () -> Unit) {
    ScreenScaffold(
        bottom = {
            SommelleButton(stringResource(R.string.export_share_pdf), onSharePdf)
            SommelleButton(stringResource(R.string.export_share_csv), onShareCsv, style = SommelleButtonStyle.Quiet)
        },
    ) {
        BackPill(onBack)
        ScreenTitle(stringResource(R.string.export_title))
        Text(stringResource(R.string.export_intro), style = MaterialTheme.typography.bodyMedium)
        InfoLine(SommelleIcons.History, stringResource(R.string.export_share_hint))

        Card(stringResource(R.string.export_program)) {
            Line(stringResource(R.string.export_started, report.programStart) + " · " + report.programWeek)
            Line(stringResource(R.string.export_age, report.ageBand))
            Line(stringResource(R.string.export_pathway, report.pathway))
            report.currentWindow?.let { Line(stringResource(R.string.export_window, it)) }
        }

        if (report.safetyNotes.isNotEmpty()) {
            Card(stringResource(R.string.export_safety)) {
                report.safetyNotes.forEach { Line("• $it") }
            }
        }

        Card(stringResource(R.string.export_weeks)) {
            if (report.weeks.isEmpty()) Line(stringResource(R.string.export_empty))
            report.weeks.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("${week.label} · ${stringResource(R.string.export_nights, week.nights)}", style = MaterialTheme.typography.titleSmall)
                    Line(
                        "${stringResource(R.string.export_in_bed)} ${week.timeInBed} · ${stringResource(R.string.export_asleep)} ${week.totalSleep} · " +
                            "${stringResource(R.string.export_efficiency)} ${week.efficiency}",
                    )
                    Line(
                        "${stringResource(R.string.export_onset)} ${week.sleepOnset} · ${stringResource(R.string.export_awake)} ${week.awake} · " +
                            "${stringResource(R.string.export_quality)} ${week.quality}",
                    )
                    Line("${stringResource(R.string.export_window_short)} : ${week.window}")
                }
            }
        }

        if (report.outcome.isNotEmpty()) {
            Card(stringResource(R.string.export_outcome)) {
                ComparisonTable(
                    beforeTitle = "Observation",
                    afterTitle = "Aujourd'hui",
                    rows = report.outcome.map { ComparisonRow(it.metric.label, it.before, it.after, it.trend.word) },
                )
            }
        }

        Text(stringResource(R.string.export_disclaimer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Card(title: String, content: @Composable () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            content()
        }
    }
}

@Composable
private fun Line(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall)
}
