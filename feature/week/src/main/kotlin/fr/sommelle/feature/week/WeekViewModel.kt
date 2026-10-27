package fr.sommelle.feature.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.model.SleepWindow
import fr.sommelle.core.presentation.FrenchFormat
import fr.sommelle.core.presentation.ProgramHistory
import fr.sommelle.core.presentation.WeekBar
import fr.sommelle.core.presentation.WeekChart
import fr.sommelle.core.presentation.WeeklyReport
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class WeekState(
    val restriction: Boolean,
    val report: WeeklyReport?,
    val window: SleepWindow?,
    val bedTime: String?,
    val wakeTime: String?,
    val bars: List<WeekBar>,
    val chartDescription: String,
)

class WeekViewModel(repository: SommelleRepository) : ViewModel() {

    val state: StateFlow<WeekState?> = repository.snapshot
        .filterNotNull()
        .map { snapshot ->
            val window = snapshot.windows.maxByOrNull { it.window.effectiveFrom }?.window
            val bars = WeekChart.bars(ProgramHistory.periods(snapshot.program.startDate, snapshot.windows, snapshot.entries))
            WeekState(
                restriction = snapshot.program.screening.restrictionAllowed,
                report = ProgramHistory.latestReport(snapshot.windows, snapshot.entries),
                window = window,
                bedTime = window?.let { FrenchFormat.time(it.bedTime) },
                wakeTime = window?.let { FrenchFormat.time(it.wakeTime) },
                bars = bars,
                chartDescription = WeekChart.accessibilityDescription(bars),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
