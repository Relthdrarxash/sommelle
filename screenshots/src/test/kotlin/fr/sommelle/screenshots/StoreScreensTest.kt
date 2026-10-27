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
 * Captures pour les fiches Google Play et F-Droid, au format 16:9 exigé par Google Play
 * (rapport hauteur/largeur de 2 au plus). Copiées par la CI dans
 * fastlane/metadata/android/fr-FR/images/phoneScreenshots, dans l'ordre des noms.
 */
class StoreScreensTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL,
        theme = "android:Theme.Material.Light.NoActionBar",
    )

    private fun snap(night: Boolean = false, content: @Composable () -> Unit) =
        paparazzi.snapshot { SommelleTheme(night = night) { content() } }

    @Test fun ecran1_accueil() = snap { Samples.Home() }
    @Test fun ecran2_agenda() = snap { Samples.Diary(DiaryMode.GUIDED, DiaryStep.BED_TIME) }
    @Test fun ecran3_estimation() = snap { Samples.Diary(DiaryMode.GUIDED, DiaryStep.SLEEP_ONSET) }
    @Test fun ecran4_bilan() = snap { Samples.Week() }
    @Test fun ecran5_nuit() = snap(night = true) { Samples.Night() }
    @Test fun ecran6_questionnaire() = snap { Samples.Result() }
}
