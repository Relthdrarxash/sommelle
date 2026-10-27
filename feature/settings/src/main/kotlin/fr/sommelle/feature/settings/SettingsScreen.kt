package fr.sommelle.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.designsystem.InfoLine
import fr.sommelle.core.designsystem.ScreenScaffold
import fr.sommelle.core.designsystem.ScreenTitle
import fr.sommelle.core.designsystem.SommelleButton
import fr.sommelle.core.designsystem.SommelleButtonStyle
import fr.sommelle.core.designsystem.SommelleIcons
import fr.sommelle.core.designsystem.TimeStepper
import fr.sommelle.core.presentation.FrenchFormat
import fr.sommelle.core.presentation.ReminderPlanner
import fr.sommelle.core.presentation.ReminderSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

private const val PRIVACY_URL = "https://relthdrarxash.github.io/sommelle/confidentialite.html"

data class SettingsState(
    val reminders: ReminderSettings,
    /** Heure effective du rappel du matin (réglée ou calculée). */
    val morningTime: LocalTime,
    /** Vrai quand une fenêtre de sommeil existe : le rappel du soir a un sens. */
    val hasWindow: Boolean,
)

class SettingsViewModel(private val repository: SommelleRepository) : ViewModel() {

    val state: StateFlow<SettingsState?> = combine(repository.reminderSettings, repository.snapshot) { settings, snapshot ->
        SettingsState(
            reminders = settings,
            morningTime = ReminderPlanner.morningTime(settings, snapshot?.program?.wakeTime),
            hasWindow = snapshot?.windows?.isNotEmpty() == true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun update(change: (ReminderSettings, LocalTime) -> ReminderSettings) {
        val current = state.value ?: return
        viewModelScope.launch { repository.saveReminderSettings(change(current.reminders, current.morningTime)) }
    }

    fun erase(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.eraseEverything()
            onDone()
        }
    }
}

@Composable
fun SettingsScreen(repository: SommelleRepository, onOpenReport: () -> Unit, onErased: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel { SettingsViewModel(repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return
    val context = LocalContext.current
    var notificationsAllowed by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsAllowed = granted
    }

    fun ensurePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    SettingsContent(
        state = s,
        notificationsAllowed = notificationsAllowed,
        onMorning = { enabled ->
            if (enabled) ensurePermission()
            viewModel.update { r, _ -> r.copy(morningEnabled = enabled) }
        },
        onMorningShift = { minutes -> viewModel.update { r, time -> r.copy(morningTime = time.plusMinutes(minutes)) } },
        onEvening = { enabled ->
            if (enabled) ensurePermission()
            viewModel.update { r, _ -> r.copy(eveningEnabled = enabled) }
        },
        onOpenReport = onOpenReport,
        onErase = { viewModel.erase(onErased) },
    )
}

/** Réglages sans état ni dépendance : affichés tels quels dans les captures de la documentation. */
@Composable
fun SettingsContent(
    state: SettingsState,
    notificationsAllowed: Boolean,
    onMorning: (Boolean) -> Unit,
    onMorningShift: (Long) -> Unit,
    onEvening: (Boolean) -> Unit,
    onOpenReport: () -> Unit,
    onErase: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    var confirmErase by rememberSaveable { mutableStateOf(false) }

    ScreenScaffold {
        ScreenTitle(stringResource(R.string.settings_title))

        Section(stringResource(R.string.settings_reminders)) {
            ToggleRow(
                title = stringResource(R.string.settings_morning),
                hint = stringResource(R.string.settings_morning_hint),
                checked = state.reminders.morningEnabled,
                onChange = onMorning,
            )
            if (state.reminders.morningEnabled) {
                Text(stringResource(R.string.settings_morning_time), style = MaterialTheme.typography.titleSmall)
                TimeStepper(
                    value = FrenchFormat.time(state.morningTime),
                    onMinus = { onMorningShift(-15) },
                    onPlus = { onMorningShift(15) },
                )
            }
            ToggleRow(
                title = stringResource(R.string.settings_evening),
                hint = stringResource(if (state.hasWindow) R.string.settings_evening_hint else R.string.settings_evening_unavailable),
                checked = state.reminders.eveningEnabled && state.hasWindow,
                enabled = state.hasWindow,
                onChange = onEvening,
            )
            if (!notificationsAllowed && (state.reminders.morningEnabled || state.reminders.eveningEnabled)) {
                Text(stringResource(R.string.settings_notifications_off), style = MaterialTheme.typography.bodySmall)
            }
        }

        Section(stringResource(R.string.settings_for_doctor)) {
            Text(stringResource(R.string.settings_report_hint), style = MaterialTheme.typography.bodyMedium)
            SommelleButton(stringResource(R.string.settings_report), onOpenReport, style = SommelleButtonStyle.Outlined)
        }

        Section(stringResource(R.string.settings_data)) {
            InfoLine(SommelleIcons.History, stringResource(R.string.settings_data_hint))
            SommelleButton(stringResource(R.string.settings_privacy), { uriHandler.openUri(PRIVACY_URL) }, style = SommelleButtonStyle.Quiet)
            SommelleButton(stringResource(R.string.settings_erase), { confirmErase = true }, style = SommelleButtonStyle.Quiet)
        }

        Text(stringResource(R.string.settings_help), style = MaterialTheme.typography.bodySmall)
        SommelleButton(stringResource(R.string.settings_call_3114), { uriHandler.openUri("tel:3114") }, style = SommelleButtonStyle.Quiet)
    }

    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            title = { Text(stringResource(R.string.settings_erase_title)) },
            text = { Text(stringResource(R.string.settings_erase_text)) },
            confirmButton = {
                TextButton(onClick = { confirmErase = false; onErase() }) { Text(stringResource(R.string.settings_erase_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmErase = false }) { Text(stringResource(R.string.settings_erase_cancel)) }
            },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            content()
        }
    }
}

/** Ligne entière cliquable avec interrupteur, annoncée comme un interrupteur par TalkBack. */
@Composable
private fun ToggleRow(title: String, hint: String, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}
