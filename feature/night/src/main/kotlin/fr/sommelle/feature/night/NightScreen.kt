package fr.sommelle.feature.night

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import fr.sommelle.core.designsystem.SommelleColors
import fr.sommelle.core.designsystem.SommelleIcons
import kotlinx.coroutines.delay

/** Durée après laquelle l'écran se ferme seul : on ne doit pas rester sur son téléphone au lit. */
private const val AUTO_CLOSE_MILLIS = 90_000L

/** Luminosité de l'écran pendant l'affichage, très basse. */
private const val NIGHT_BRIGHTNESS = 0.05f

/**
 * Écran « Je n'arrive pas à dormir » (documentation/design.md, section 7) : presque noir et chaud,
 * aucune heure ni aucun chiffre (la barre d'état, qui affiche l'heure, est masquée),
 * luminosité abaissée, fermeture automatique.
 */
@Composable
fun NightScreen(onClose: () -> Unit) {
    val currentOnClose = rememberUpdatedState(onClose)
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        val previousBrightness = window?.attributes?.screenBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        window?.let { w -> w.attributes = w.attributes.apply { screenBrightness = NIGHT_BRIGHTNESS } }
        controller?.hide(WindowInsetsCompat.Type.statusBars())
        onDispose {
            window?.let { w -> w.attributes = w.attributes.apply { screenBrightness = previousBrightness } }
            controller?.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    LaunchedEffect(Unit) {
        delay(AUTO_CLOSE_MILLIS)
        currentOnClose.value()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SommelleColors.Ember)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 28.dp, vertical = 36.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Icon(SommelleIcons.Moon, contentDescription = null, tint = SommelleColors.SandMuted, modifier = Modifier.size(44.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.night_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = SommelleColors.Sand,
                    modifier = Modifier.semantics { heading() },
                )
                Text(stringResource(R.string.night_intro), style = MaterialTheme.typography.bodyLarge, color = SommelleColors.SandMuted)
            }
            Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
                NightStep(SommelleIcons.Door, stringResource(R.string.night_step_1))
                NightStep(SommelleIcons.Lamp, stringResource(R.string.night_step_2))
                NightStep(SommelleIcons.Bed, stringResource(R.string.night_step_3))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 16.dp)) {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, SommelleColors.Sand),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SommelleColors.EmberSurface,
                    contentColor = SommelleColors.Sand,
                ),
            ) {
                Text(stringResource(R.string.night_ok), style = MaterialTheme.typography.labelLarge)
            }
            Text(
                stringResource(R.string.night_auto_close),
                style = MaterialTheme.typography.bodySmall,
                color = SommelleColors.SandMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun NightStep(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = SommelleColors.Sand, modifier = Modifier.size(26.dp).padding(top = 2.dp))
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = SommelleColors.Sand)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
