# Contribuer à Sommelle

## Intégration continue

Chaque envoi sur `main` et chaque pull request lancent le workflow **CI** (`.github/workflows/ci.yml`) :

1. vérification du wrapper Gradle ;
2. tests unitaires (`./gradlew test`) : moteur TCC-I et logique d'écran ;
3. analyse Android (`./gradlew :app:lintDebug`) ;
4. construction de l'APK de développement, disponible en téléchargement dans l'onglet Actions (artefact `sommelle-debug`, conservé 14 jours).

En cas d'échec, les rapports de tests et de lint sont joints à l'exécution (artefact `rapports`).

## Captures d'écran

Les captures de la documentation (`docs/captures`) sont produites par le module `:screenshots`, qui affiche les écrans avec [Paparazzi](https://github.com/cashapp/paparazzi), sans téléphone ni émulateur. Le workflow **Captures d'écran** les régénère à chaque modification des écrans sur `main` et les enregistre dans le dépôt quand elles changent. Il peut aussi être lancé à la main depuis l'onglet Actions.

Pour ajouter une capture : écrire une méthode dans `screenshots/src/test/kotlin/fr/sommelle/screenshots/ScreensTest.kt`. L'image prend le nom de la méthode (`bilan()` donne `docs/captures/bilan.png`). Les écrans s'affichent à partir de leur version sans état (`HomeContent`, `DiaryContent`, etc.), avec des données d'exemple.

Pour les générer localement : `./gradlew :screenshots:recordPaparazziDebug`.

## Site et supports

- Le dossier `site/` est publié sur GitHub Pages par le workflow **Site**, à chaque modification sur `main`, avec les captures d'écran à jour.
- La fiche pour les prescripteurs, la bannière et l'icône des stores se régénèrent depuis les modèles de `marketing/` : `python3 marketing/build.py` (prérequis dans l'en-tête du script).
- Les textes et captures des stores sont dans `fastlane/metadata/android/fr-FR/`, lus par F-Droid et réutilisables pour Google Play.

## Dépendances

Dependabot propose chaque mois les mises à jour des dépendances Gradle et des actions GitHub.

## Publication

Pousser une étiquette de version produit un APK et un bundle signés, publiés dans une version GitHub avec leur empreinte SHA-256 :

```bash
git tag v0.1.0
git push origin v0.1.0
```

### Clé de signature (à faire une seule fois)

1. Créer la clé, à conserver précieusement hors du dépôt : la perdre empêche toute mise à jour de l'appli déjà installée.

   ```bash
   keytool -genkeypair -v -keystore sommelle.jks -alias sommelle \
     -keyalg RSA -keysize 4096 -validity 10000
   ```

2. L'encoder en base64 :

   ```bash
   base64 -w0 sommelle.jks
   ```

3. Dans GitHub, **Settings → Secrets and variables → Actions**, créer quatre secrets :

   | Secret | Contenu |
   |---|---|
   | `SOMMELLE_KEYSTORE_BASE64` | Le résultat de l'étape 2 |
   | `SOMMELLE_KEYSTORE_PASSWORD` | Mot de passe du keystore |
   | `SOMMELLE_KEY_ALIAS` | `sommelle` |
   | `SOMMELLE_KEY_PASSWORD` | Mot de passe de la clé |

Sans ces secrets, le workflow de publication s'arrête avec un message explicite ; la CI ordinaire n'en a pas besoin.
