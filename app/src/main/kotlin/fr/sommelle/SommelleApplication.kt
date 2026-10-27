package fr.sommelle

import android.app.Application
import fr.sommelle.core.data.SommelleRepository

/** Assemblage des dépendances, sans framework d'injection : une seule source de données. */
class SommelleApplication : Application() {
    val repository: SommelleRepository by lazy { SommelleRepository.create(this) }
}
