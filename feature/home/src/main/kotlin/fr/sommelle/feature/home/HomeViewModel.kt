package fr.sommelle.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.model.Pathway
import fr.sommelle.core.presentation.FrenchFormat
import fr.sommelle.core.presentation.WeekBadge
import fr.sommelle.core.presentation.WindowStep
import fr.sommelle.core.presentation.nightsToFill
import fr.sommelle.core.presentation.weekBadge
import fr.sommelle.core.protocol.ProgramPlanner
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate

data class HomeState(
    val date: String,
    val badge: WeekBadge,
    val week: Int,
    val pathway: Pathway,
    val step: WindowStep?,
    /** Nuits encore à remplir, la plus récente d'abord. */
    val nightsToFill: List<LocalDate>,
    val lastNight: LocalDate,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repository: SommelleRepository) : ViewModel() {

    private val planner = ProgramPlanner()

    /** Chaque changement de données relance la synchronisation des fenêtres dues. */
    val state: StateFlow<HomeState?> = repository.snapshot
        .filterNotNull()
        .mapLatest { snapshot ->
            val step = repository.synchronize()
            val today = repository.today()
            val (badge, week) = weekBadge(planner.programWeek(snapshot.program.startDate, today))
            HomeState(
                date = FrenchFormat.longDate(today),
                badge = badge,
                week = week,
                pathway = snapshot.program.screening.pathway,
                step = step,
                nightsToFill = nightsToFill(today, snapshot.entries.map { it.nightOf }.toSet()),
                lastNight = today,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
