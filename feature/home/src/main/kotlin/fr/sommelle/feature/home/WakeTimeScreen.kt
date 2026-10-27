package fr.sommelle.feature.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.designsystem.BackPill
import fr.sommelle.core.designsystem.InfoLine
import fr.sommelle.core.designsystem.ScreenScaffold
import fr.sommelle.core.designsystem.ScreenTitle
import fr.sommelle.core.designsystem.SommelleButton
import fr.sommelle.core.designsystem.SommelleIcons
import fr.sommelle.core.designsystem.TimeStepper
import fr.sommelle.core.presentation.DiaryChoices
import fr.sommelle.core.presentation.FrenchFormat
import fr.sommelle.core.presentation.WindowStep
import kotlinx.coroutines.launch
import java.time.LocalTime

/** Choix de l'heure de lever fixe, ancre de la fenêtre de sommeil. */
@Composable
fun WakeTimeScreen(repository: SommelleRepository, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var minutes by rememberSaveable { mutableStateOf<Int?>(null) }
    var suggested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (minutes == null) {
            val proposal = (repository.synchronize() as? WindowStep.NeedsWakeTime)?.suggested
            suggested = proposal != null
            minutes = (proposal ?: LocalTime.of(7, 0)).toSecondOfDay() / 60
        }
    }
    val current = minutes ?: return
    val time = LocalTime.MIDNIGHT.plusMinutes(current.toLong())

    ScreenScaffold(
        bottom = {
            SommelleButton(stringResource(R.string.wake_confirm), onClick = {
                scope.launch {
                    repository.setWakeTime(time)
                    onDone()
                }
            })
        },
    ) {
        BackPill(onDone)
        ScreenTitle(stringResource(R.string.wake_title))
        Text(stringResource(R.string.wake_text), style = MaterialTheme.typography.bodyLarge)
        TimeStepper(
            value = FrenchFormat.time(time),
            onMinus = { minutes = Math.floorMod(current - DiaryChoices.TIME_STEP_MINUTES.toInt(), 1440) },
            onPlus = { minutes = Math.floorMod(current + DiaryChoices.TIME_STEP_MINUTES.toInt(), 1440) },
            footer = if (suggested) {
                { InfoLine(SommelleIcons.History, stringResource(R.string.wake_suggested)) }
            } else {
                null
            },
        )
    }
}
