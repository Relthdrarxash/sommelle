package fr.sommelle.feature.cognitive

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.designsystem.BackPill
import fr.sommelle.core.designsystem.ScreenScaffold
import fr.sommelle.core.designsystem.ScreenTitle
import fr.sommelle.core.designsystem.SommelleButton
import fr.sommelle.core.model.Pathway
import fr.sommelle.core.presentation.CognitiveModule
import fr.sommelle.core.protocol.ProgramPlanner
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ModulesState(
    val inProgram: Boolean,
    val available: List<CognitiveModule>,
    val completed: Set<CognitiveModule>,
    val nextLocked: CognitiveModule?,
)

class ModulesViewModel(private val repository: SommelleRepository) : ViewModel() {
    private val planner = ProgramPlanner()

    val state: StateFlow<ModulesState?> = combine(repository.snapshot.filterNotNull(), repository.completedModules) { snapshot, done ->
        val week = planner.programWeek(snapshot.program.startDate, repository.today())
        ModulesState(
            inProgram = snapshot.program.screening.pathway != Pathway.OUT_OF_SCOPE,
            available = CognitiveModule.available(week),
            completed = done,
            nextLocked = CognitiveModule.nextLocked(week),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun complete(module: CognitiveModule, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.completeModule(module)
            onDone()
        }
    }
}

@Composable
fun ModulesScreen(repository: SommelleRepository, onOpen: (CognitiveModule) -> Unit) {
    val viewModel: ModulesViewModel = viewModel { ModulesViewModel(repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return
    ModulesContent(s, onOpen)
}

/** Liste des exercices sans état ni dépendance : affichée telle quelle dans les captures. */
@Composable
fun ModulesContent(state: ModulesState, onOpen: (CognitiveModule) -> Unit) {
    ScreenScaffold {
        ScreenTitle(stringResource(R.string.cog_title))
        if (!state.inProgram) {
            Text(stringResource(R.string.cog_out_of_scope), style = MaterialTheme.typography.bodyLarge)
            return@ScreenScaffold
        }
        Text(stringResource(R.string.cog_intro), style = MaterialTheme.typography.bodyLarge)
        if (state.available.isEmpty()) {
            Text(stringResource(R.string.cog_locked_all), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        state.available.forEach { module ->
            ModuleRow(module, done = module in state.completed, onClick = { onOpen(module) })
        }
        state.nextLocked?.takeIf { state.available.isNotEmpty() }?.let {
            Text(stringResource(R.string.cog_next, it.week), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModuleRow(module: CognitiveModule, done: Boolean, onClick: () -> Unit) {
    val status = stringResource(if (done) R.string.cog_done else R.string.cog_todo)
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clickable(onClick = onClick)
                .semantics { role = Role.Button }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(module.title()), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(module.summary()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (done) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ModuleScreen(repository: SommelleRepository, module: CognitiveModule, onBack: () -> Unit) {
    val viewModel: ModulesViewModel = viewModel { ModulesViewModel(repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    ModuleContent(
        module = module,
        done = state?.completed?.contains(module) == true,
        onBack = onBack,
        onFinish = { viewModel.complete(module, onBack) },
    )
}

/** Un exercice : une introduction, puis des étapes courtes, une par carte. */
@Composable
fun ModuleContent(module: CognitiveModule, done: Boolean, onBack: () -> Unit, onFinish: () -> Unit) {
    val steps = stringArrayResource(module.steps())
    ScreenScaffold(
        bottom = {
            if (done) {
                Text(stringResource(R.string.cog_finished), style = MaterialTheme.typography.bodyMedium)
            } else {
                SommelleButton(stringResource(R.string.cog_finish), onFinish, icon = Icons.Filled.Check)
            }
        },
    ) {
        BackPill(onBack)
        ScreenTitle(stringResource(module.title()))
        Text(stringResource(module.intro()), style = MaterialTheme.typography.bodyLarge)
        steps.forEachIndexed { index, step ->
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.cog_step, index + 1, steps.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(step, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@StringRes
fun CognitiveModule.title(): Int = when (this) {
    CognitiveModule.BELIEFS -> R.string.cog_beliefs_title
    CognitiveModule.WIND_DOWN -> R.string.cog_winddown_title
    CognitiveModule.PARADOXICAL_INTENTION -> R.string.cog_paradox_title
    CognitiveModule.DECATASTROPHIZING -> R.string.cog_decatastrophize_title
    CognitiveModule.RELAXATION -> R.string.cog_relax_title
}

@StringRes
private fun CognitiveModule.summary(): Int = when (this) {
    CognitiveModule.BELIEFS -> R.string.cog_beliefs_summary
    CognitiveModule.WIND_DOWN -> R.string.cog_winddown_summary
    CognitiveModule.PARADOXICAL_INTENTION -> R.string.cog_paradox_summary
    CognitiveModule.DECATASTROPHIZING -> R.string.cog_decatastrophize_summary
    CognitiveModule.RELAXATION -> R.string.cog_relax_summary
}

@StringRes
private fun CognitiveModule.intro(): Int = when (this) {
    CognitiveModule.BELIEFS -> R.string.cog_beliefs_intro
    CognitiveModule.WIND_DOWN -> R.string.cog_winddown_intro
    CognitiveModule.PARADOXICAL_INTENTION -> R.string.cog_paradox_intro
    CognitiveModule.DECATASTROPHIZING -> R.string.cog_decatastrophize_intro
    CognitiveModule.RELAXATION -> R.string.cog_relax_intro
}

@ArrayRes
private fun CognitiveModule.steps(): Int = when (this) {
    CognitiveModule.BELIEFS -> R.array.cog_beliefs_steps
    CognitiveModule.WIND_DOWN -> R.array.cog_winddown_steps
    CognitiveModule.PARADOXICAL_INTENTION -> R.array.cog_paradox_steps
    CognitiveModule.DECATASTROPHIZING -> R.array.cog_decatastrophize_steps
    CognitiveModule.RELAXATION -> R.array.cog_relax_steps
}
