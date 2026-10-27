# Contribuer à Sommelle

Les retours, signalements et propositions sont les bienvenus, en particulier de la part de professionnels du sommeil et de personnes qui utilisent l'appli. Pour un changement de règle clinique, merci de citer la source sur laquelle il s'appuie.

## Compiler et tester

```bash
./gradlew test                 # tests unitaires : moteur TCC-I et logique d'écran
./gradlew :app:lintDebug       # analyse Android
./gradlew :app:assembleDebug   # application de développement
```

Les règles cliniques vivent dans `core:protocol`, en pur Kotlin. Toute modification d'une valeur de `ProtocolConfig` doit incrémenter son numéro de version et être reportée dans [documentation/protocole.md](documentation/protocole.md).

## Intégration continue

Chaque envoi sur `main` et chaque pull request lancent le workflow **CI** :

1. vérification du wrapper Gradle ;
2. tests unitaires ;
3. analyse Android ;
4. construction de l'APK de développement, téléchargeable dans l'onglet Actions (artefact `sommelle-debug`, conservé 14 jours).

En cas d'échec, les rapports de tests et d'analyse sont joints à l'exécution (artefact `rapports`), et les erreurs apparaissent en annotations.

## Captures d'écran

Les captures du site et du README (`docs/captures`) et des stores sont produites par le module `:screenshots`, qui affiche les écrans avec [Paparazzi](https://github.com/cashapp/paparazzi), sans téléphone ni émulateur. Le workflow **Captures d'écran** les régénère à chaque modification des écrans sur `main`.

Pour ajouter une capture : écrire une méthode dans `screenshots/src/test/kotlin/fr/sommelle/screenshots/ScreensTest.kt`. L'image prend le nom de la méthode (`bilan()` donne `docs/captures/bilan.png`). Les écrans s'affichent à partir de leur version sans état (`HomeContent`, `DiaryContent`, etc.), avec les données d'exemple de `Samples.kt`.

Localement : `./gradlew :screenshots:recordPaparazziDebug`.

## Site et fiches des stores

- Le dossier `docs/` contient le site, publié tel quel sur GitHub Pages par le workflow **Site**. Les captures y sont mises à jour automatiquement.
- La documentation du projet (protocole, design, architecture) est dans `documentation/`.
- Les textes et captures des stores sont dans `fastlane/metadata/android/fr-FR/`, lus par F-Droid.

## Dépendances

Dependabot propose chaque mois les mises à jour des dépendances Gradle et des actions GitHub.

## Versions

Les versions signées sont publiées par le mainteneur à partir d'une étiquette `v*`, via le workflow **Publication**. Chaque version GitHub fournit l'APK, le bundle et leur empreinte SHA-256.
