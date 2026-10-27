package fr.sommelle.feature.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.model.SleepWindow
import fr.sommelle.core.presentation.FrenchFormat
import fr.sommelle.core.presentation.OutcomeRow
import fr.sommelle.core.presentation.ProgramOutcome
import fr.sommelle.core.protocol.ProgramPlanner
import fr.sommelle.core.presentation.ProgramHistory
import fr.sommelle.core.presentation.WeekBar
import fr.sommelle.core.presentation.WeekChart
import fr.sommelle.core.presentation.WeeklyReport
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class WeekState(
    val restriction: Boolean,
    val report: WeeklyReport?,
    val window: SleepWindow?,
    val bedTime: String?,
    val wakeTime: String?,
    val bars: List<WeekBar>,
    val chartDescription: String,
    /** Bilan avant/après, à partir de la semaine 6 du programme. */
    val outcome: List<OutcomeRow> = emptyList(),
)

class WeekViewModel(repository: SommelleRepository) : ViewModel() {

    private val planner = ProgramPlanner()

    val state: StateFlow<WeekState?> = combine(repository.snapshot.filterNotNull(), repository.daytimeImpacts) { snapshot, impacts ->
            val window = snapshot.windows.maxByOrNull { it.window.effectiveFrom }?.window
            val periods = ProgramHistory.periods(snapshot.program.startDate, snapshot.windows, snapshot.entries)
            val bars = WeekChart.bars(periods)
            val today = repository.today()
            val week = planner.programWeek(snapshot.program.startDate, today) ?: 0
            val outcome = if (week >= ProgramOutcome.AVAILABLE_FROM_WEEK) {
                ProgramOutcome.compare(periods.first(), ProgramOutcome.recentPeriod(snapshot.entries, today), impacts.firstOrNull(), impacts.lastOrNull())
            } else {
                emptyList()
            }
            WeekState(
                restriction = snapshot.program.screening.restrictionAllowed,
                report = ProgramHistory.latestReport(snapshot.windows, snapshot.entries),
                window = window,
                bedTime = window?.let { FrenchFormat.time(it.bedTime) },
                wakeTime = window?.let { FrenchFormat.time(it.wakeTime) },
                bars = bars,
                chartDescription = WeekChart.accessibilityDescription(bars),
                outcome = outcome,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
