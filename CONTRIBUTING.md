# Contribuer à Sommelle

## Intégration continue

Chaque envoi sur `main` et chaque pull request lancent le workflow **CI** (`.github/workflows/ci.yml`) :

1. vérification du wrapper Gradle ;
2. tests unitaires (`./gradlew test`) : moteur TCC-I et logique d'écran ;
3. analyse Android (`./gradlew :app:lintDebug`) ;
4. construction de l'APK de développement, disponible en téléchargement dans l'onglet Actions (artefact `sommelle-debug`, conservé 14 jours).

En cas d'échec, les rapports de tests et de lint sont joints à l'exécution (artefact `rapports`).

Dependabot propose chaque mois les mises à jour des dépendances Gradle et des actions GitHub.

## Publication

Pousser une étiquette de version produit un APK et un bundle signés, publiés dans une version GitHub avec leur empreinte SHA-256 :

```bash
git tag v0.1.0
git push origin v0.1.0
```
