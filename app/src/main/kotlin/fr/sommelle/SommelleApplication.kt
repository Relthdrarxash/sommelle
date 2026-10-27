package fr.sommelle

import android.app.Application
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.data.SommelleRepositoryProvider
import fr.sommelle.feature.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** Assemblage des dépendances, sans framework d'injection : une seule source de données. */
class SommelleApplication : Application(), SommelleRepositoryProvider {

    override val repository: SommelleRepository by lazy { SommelleRepository.create(this) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Un seul endroit replanifie les rappels : à chaque changement de réglages, d'heure de lever ou de fenêtre.
        scope.launch {
            combine(repository.reminderSettings, repository.snapshot) { settings, snapshot ->
                Triple(settings, snapshot?.program?.wakeTime, snapshot?.windows?.maxByOrNull { it.window.effectiveFrom }?.window?.bedTime)
            }
                .distinctUntilChanged()
                .collect { ReminderScheduler.reschedule(this@SommelleApplication, repository) }
        }
    }
}
