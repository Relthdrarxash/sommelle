package fr.sommelle.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Une ligne de comparaison avant/après, déjà mise en forme. */
data class ComparisonRow(val label: String, val before: String, val after: String, val trendWord: String)

/**
 * Tableau avant/après. L'évolution est écrite en toutes lettres (« mieux », « stable »), jamais portée
 * par la seule couleur, et chaque ligne est lue d'un bloc par TalkBack.
 */
@Composable
fun ComparisonTable(beforeTitle: String, afterTitle: String, rows: List<ComparisonRow>, modifier: Modifier = Modifier) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().clearAndSetSemantics { }) {
            Text("", modifier = Modifier.weight(1.4f))
            Text(beforeTitle, style = MaterialTheme.typography.labelSmall, color = muted, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text(afterTitle, style = MaterialTheme.typography.labelSmall, color = muted, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        rows.forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            val description = "${row.label} : $beforeTitle ${row.before}, $afterTitle ${row.after}, ${row.trendWord}."
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp).clearAndSetSemantics { contentDescription = description }) {
                Column(Modifier.weight(1.4f)) {
                    Text(row.label, style = MaterialTheme.typography.bodySmall)
                    Text(row.trendWord, style = MaterialTheme.typography.labelSmall, color = muted)
                }
                Text(row.before, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text(row.after, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }
    }
}
