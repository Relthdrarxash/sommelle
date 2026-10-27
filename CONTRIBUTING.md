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
