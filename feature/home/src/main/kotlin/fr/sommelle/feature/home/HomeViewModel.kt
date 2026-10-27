package fr.sommelle.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.model.Pathway
import fr.sommelle.core.presentation.FrenchFormat
import fr.sommelle.core.presentation.ProgramOutcome
import fr.sommelle.core.presentation.WeekBadge
import fr.sommelle.core.presentation.WindowStep
import fr.sommelle.core.presentation.nightsToFill
import fr.sommelle.core.presentation.weekBadge
import fr.sommelle.core.protocol.ProgramPlanner
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
    /** Vrai quand il faut demander le ressenti de la journée : au début, puis à partir de la semaine 6. */
    val askDaytimeImpact: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repository: SommelleRepository) : ViewModel() {

    private val planner = ProgramPlanner()

    /** Chaque changement de données relance la synchronisation des fenêtres dues. */
    val state: StateFlow<HomeState?> = combine(repository.snapshot.filterNotNull(), repository.daytimeImpacts) { snapshot, impacts -> snapshot to impacts }
        .mapLatest { (snapshot, impacts) ->
            val step = repository.synchronize()
            val today = repository.today()
            val programWeek = planner.programWeek(snapshot.program.startDate, today)
            val (badge, week) = weekBadge(programWeek)
            val endImpactDone = impacts.any { (planner.programWeek(snapshot.program.startDate, it.date) ?: 0) >= ProgramOutcome.AVAILABLE_FROM_WEEK }
            val inProgram = snapshot.program.screening.pathway != Pathway.OUT_OF_SCOPE
            HomeState(
                date = FrenchFormat.longDate(today),
                badge = badge,
                week = week,
                pathway = snapshot.program.screening.pathway,
                step = step,
                nightsToFill = nightsToFill(today, snapshot.entries.map { it.nightOf }.toSet()),
                lastNight = today,
                askDaytimeImpact = inProgram &&
                    (impacts.isEmpty() || ((programWeek ?: 0) >= ProgramOutcome.AVAILABLE_FROM_WEEK && !endImpactDone)),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun saveDaytimeImpact(score: Int) {
        viewModelScope.launch { repository.saveDaytimeImpact(score) }
    }
}
