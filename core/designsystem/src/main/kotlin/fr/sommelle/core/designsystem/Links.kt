package fr.sommelle.core.designsystem

import androidx.compose.ui.platform.UriHandler

/**
 * Ouvre un lien (page web, e-mail, téléphone) sans faire planter l'appli quand aucune application
 * ne sait le gérer : téléphone sans messagerie configurée, tablette sans fonction d'appel.
 */
fun UriHandler.openSafely(uri: String) {
    runCatching { openUri(uri) }
}
