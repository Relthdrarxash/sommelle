package fr.sommelle.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class SommelleButtonStyle { Primary, Accent, Outlined, Quiet }

/**
 * Bouton de Sommelle : toujours un libellé, une icône facultative, et une hauteur généreuse
 * (64 dp pour les actions principales, DESIGN.md section 8).
 */
@Composable
fun SommelleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SommelleButtonStyle = SommelleButtonStyle.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val content: @Composable RowScope.() -> Unit = {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
    val colors = MaterialTheme.colorScheme
    when (style) {
        SommelleButtonStyle.Primary -> Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
            enabled = enabled,
            shape = CircleShape,
            content = content,
        )
        SommelleButtonStyle.Accent -> Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.onSecondary),
            content = content,
        )
        SommelleButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
            enabled = enabled,
            shape = CircleShape,
            border = BorderStroke(2.dp, colors.primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
            content = content,
        )
        SommelleButtonStyle.Quiet -> TextButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
            enabled = enabled,
            content = content,
        )
    }
}

/** Bouton « Retour » toujours libellé ; il ne fait jamais perdre de données. */
@Composable
fun BackPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        contentPadding = PaddingValues(start = 10.dp, end = 18.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.ds_back), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun StepProgress(current: Int, total: Int, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.ds_step_of, current, total)
    Column(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = label },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(
            progress = { current.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}

/** Titre d'écran, annoncé comme titre par TalkBack. */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.semantics { heading() },
    )
}

/** Heure en très grand avec deux gros boutons ±15 min : pas de cadran d'horloge (DESIGN.md, section 4). */
@Composable
fun TimeStepper(
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StepButton(stringResource(R.string.ds_minus_15), stringResource(R.string.ds_minus_15_description), onMinus, Modifier.weight(1f))
                StepButton(stringResource(R.string.ds_plus_15), stringResource(R.string.ds_plus_15_description), onPlus, Modifier.weight(1f))
            }
            footer?.invoke()
        }
    }
}

@Composable
private fun StepButton(label: String, description: String, onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp).semantics { contentDescription = description },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

/** Grandes pastilles à choix unique, annoncées comme boutons radio. */
@Composable
fun <T> ChoiceGrid(
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2,
) {
    Column(modifier = modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { option ->
                    ChoiceButton(label(option), option == selected, { onSelect(option) }, Modifier.weight(1f))
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ChoiceButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(if (selected) colors.primary else colors.surface)
            .border(2.dp, if (selected) colors.primary else colors.outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                color = if (selected) colors.onPrimary else colors.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Deux réponses de même poids, l'une sous l'autre pour rester lisibles à 200 % de taille de texte. */
@Composable
fun YesNoButtons(onYes: () -> Unit, onNo: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SommelleButton(stringResource(R.string.ds_yes), onYes, style = SommelleButtonStyle.Outlined)
        SommelleButton(stringResource(R.string.ds_no), onNo, style = SommelleButtonStyle.Outlined)
    }
}

/** Petite mention explicative, avec une icône décorative. */
@Composable
fun InfoLine(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Gabarit d'écran : contenu défilant (rien n'est coupé à 200 % de taille de texte),
 * actions principales toujours visibles en bas.
 */
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    bottom: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content,
        )
        if (bottom != null) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = bottom,
            )
        }
    }
}
