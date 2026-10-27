package fr.sommelle.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.model.AgeBand
import fr.sommelle.core.model.ScreeningAnswers
import fr.sommelle.core.model.ScreeningResult
import fr.sommelle.core.presentation.ScreeningFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface OnboardingState {
    data object Welcome : OnboardingState
    data class Questions(val flow: ScreeningFlow) : OnboardingState
    data class Result(val flow: ScreeningFlow, val answers: ScreeningAnswers, val result: ScreeningResult) : OnboardingState
}

class OnboardingViewModel(private val repository: SommelleRepository) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Welcome)
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun start() = _state.update { OnboardingState.Questions(ScreeningFlow()) }

    fun chooseAge(ageBand: AgeBand) = advance { it.chooseAge(ageBand) }

    fun answer(yes: Boolean) = advance { it.answer(yes) }

    /** Retour à l'étape précédente ; depuis l'âge, retour à l'accueil du questionnaire. */
    fun back() = _state.update { state ->
        when (state) {
            OnboardingState.Welcome -> state
            is OnboardingState.Questions ->
                if (state.flow.isAgeStep) OnboardingState.Welcome else OnboardingState.Questions(state.flow.back())
            is OnboardingState.Result -> OnboardingState.Questions(state.flow.back())
        }
    }

    fun confirm(onDone: () -> Unit) {
        val result = _state.value as? OnboardingState.Result ?: return
        viewModelScope.launch {
            repository.startProgram(result.answers)
            onDone()
        }
    }

    private fun advance(step: (ScreeningFlow) -> ScreeningFlow) = _state.update { state ->
        val questions = state as? OnboardingState.Questions ?: return@update state
        val next = step(questions.flow)
        val answers = next.answers
        if (next.finished && answers != null) {
            OnboardingState.Result(next, answers, repository.evaluate(answers))
        } else {
            OnboardingState.Questions(next)
        }
    }
}
