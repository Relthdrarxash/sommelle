package fr.sommelle.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.model.SleepQuality
import fr.sommelle.core.presentation.DiaryDraft
import fr.sommelle.core.presentation.DiaryStep
import fr.sommelle.core.presentation.TimeField
import fr.sommelle.core.protocol.DiaryIssue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class DiaryMode { GUIDED, SUMMARY }

data class DiaryState(
    val draft: DiaryDraft,
    val mode: DiaryMode,
    val step: DiaryStep = DiaryStep.BED_TIME,
    /** Vrai quand on corrige une ligne depuis le résumé : « Suivant » y ramène. */
    val returnToSummary: Boolean = false,
    /** Problème bloquant à expliquer, s'il y en a un. */
    val blockingIssue: DiaryIssue? = null,
)

/** Actions de l'agenda, séparées du ViewModel pour afficher l'écran sans données réelles. */
interface DiaryActions {
    fun shift(field: TimeField, minutes: Long)
    fun sleepOnset(minutes: Long)
    fun awakeningCount(count: Int)
    fun awakeMinutes(minutes: Long)
    fun quality(value: SleepQuality)
    fun next(onSaved: () -> Unit)
    fun back(): Boolean
    fun editFromSummary(step: DiaryStep)
    fun guidedFromStart()
    fun dismissIssue()
    fun save(onSaved: () -> Unit)
}

class DiaryViewModel(
    private val repository: SommelleRepository,
    private val nightOf: LocalDate,
) : ViewModel(), DiaryActions {

    private val _state = MutableStateFlow<DiaryState?>(null)
    val state: StateFlow<DiaryState?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = repository.entry(nightOf)
            val loggedNights = repository.snapshot.first()?.entries?.size ?: 0
            val draft = existing?.let { DiaryDraft.prefilled(nightOf, it).copy(prefillSource = it.prefillSource) }
                ?: DiaryDraft.prefilled(nightOf, repository.previousEntry(nightOf))
            // Guidé les deux premières semaines, puis résumé une fois l'habitude prise (docs/design.md, section 5).
            val mode = if (existing != null || loggedNights >= HABIT_NIGHTS) DiaryMode.SUMMARY else DiaryMode.GUIDED
            _state.value = DiaryState(draft = draft, mode = mode)
        }
    }

    override fun shift(field: TimeField, minutes: Long) = edit { it.shift(field, minutes) }
    override fun sleepOnset(minutes: Long) = edit { it.withSleepOnset(minutes) }
    override fun awakeningCount(count: Int) = edit { it.withAwakeningCount(count) }
    override fun awakeMinutes(minutes: Long) = edit { it.withAwakeMinutes(minutes) }
    override fun quality(value: SleepQuality) = edit { it.withQuality(value) }

    /** Étape suivante, retour au résumé, ou enregistrement à la dernière question. */
    override fun next(onSaved: () -> Unit) {
        val s = _state.value ?: return
        val following = s.step.next
        when {
            s.returnToSummary -> _state.update { it?.copy(mode = DiaryMode.SUMMARY, returnToSummary = false) }
            following != null -> _state.update { it?.copy(step = following) }
            else -> save(onSaved)
        }
    }

    /** Retour : question précédente, ou résumé. Renvoie faux quand il faut quitter l'écran. */
    override fun back(): Boolean {
        val s = _state.value ?: return false
        if (s.mode == DiaryMode.SUMMARY) return false
        val previous = s.step.previous
        return when {
            s.returnToSummary -> { _state.update { it?.copy(mode = DiaryMode.SUMMARY, returnToSummary = false) }; true }
            previous != null -> { _state.update { it?.copy(step = previous) }; true }
            else -> false
        }
    }

    override fun editFromSummary(step: DiaryStep) = _state.update { it?.copy(mode = DiaryMode.GUIDED, step = step, returnToSummary = true) }

    override fun guidedFromStart() = _state.update { it?.copy(mode = DiaryMode.GUIDED, step = DiaryStep.BED_TIME, returnToSummary = false) }

    override fun dismissIssue() = _state.update { it?.copy(blockingIssue = null) }

    override fun save(onSaved: () -> Unit) {
        val s = _state.value ?: return
        val blocking = s.draft.issues().firstOrNull { it.blocking }
        if (blocking != null) {
            _state.update { it?.copy(blockingIssue = blocking) }
            return
        }
        viewModelScope.launch {
            repository.saveEntry(s.draft.toEntry())
            onSaved()
        }
    }

    private fun edit(change: (DiaryDraft) -> DiaryDraft) = _state.update { it?.copy(draft = change(it.draft), blockingIssue = null) }

    private companion object {
        const val HABIT_NIGHTS = 14
    }
}
