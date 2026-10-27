package fr.sommelle.screenshots

import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import fr.sommelle.core.designsystem.SommelleTheme
import fr.sommelle.core.presentation.DiaryStep
import fr.sommelle.feature.diary.DiaryMode
import org.junit.Rule
import org.junit.Test

/**
 * Captures des écrans pour la documentation. Chaque méthode produit une image nommée d'après elle,
 * copiée par la CI dans docs/captures/<méthode>.png.
 */
class ScreensTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
    )

    private fun snap(night: Boolean = false, content: @Composable () -> Unit) =
        paparazzi.snapshot { SommelleTheme(night = night) { content() } }

    @Test fun bienvenue() = snap { Samples.Welcome() }
    @Test fun questionnaire() = snap { Samples.Question() }
    @Test fun resultat() = snap { Samples.Result() }
    @Test fun accueil() = snap { Samples.Home() }
    @Test fun accueil_nuit() = snap(night = true) { Samples.Home() }
    @Test fun accueil_observation() = snap { Samples.HomeObservation() }
    @Test fun heure_de_lever() = snap { Samples.WakeTime() }
    @Test fun agenda_heure() = snap { Samples.Diary(DiaryMode.GUIDED, DiaryStep.BED_TIME) }
    @Test fun agenda_estimation() = snap { Samples.Diary(DiaryMode.GUIDED, DiaryStep.SLEEP_ONSET) }
    @Test fun agenda_reveils() = snap { Samples.Diary(DiaryMode.GUIDED, DiaryStep.AWAKENINGS) }
    @Test fun agenda_resume() = snap { Samples.Diary(DiaryMode.SUMMARY, DiaryStep.BED_TIME) }
    @Test fun bilan() = snap { Samples.Week() }
    @Test fun nuit() = snap(night = true) { Samples.Night() }
    @Test fun accueil_question() = snap { Samples.HomeWithQuestion() }
    @Test fun bilan_programme() = snap { Samples.WeekWithOutcome() }
    @Test fun reglages() = snap { Samples.Settings() }
    @Test fun resume_medecin() = snap { Samples.Report() }
    @Test fun exercices() = snap { Samples.Exercises() }
    @Test fun exercice() = snap { Samples.Exercise() }
}
