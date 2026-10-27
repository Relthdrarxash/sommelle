# Sommelle

[![CI](https://github.com/Relthdrarxash/sommelle/actions/workflows/ci.yml/badge.svg)](https://github.com/Relthdrarxash/sommelle/actions/workflows/ci.yml)

Application Android d'accompagnement à l'insomnie fondée sur la TCC-I (thérapie cognitivo-comportementale de l'insomnie), le traitement recommandé en premier par les recommandations européennes et américaines. Gratuite, en français, sans compte et sans publicité. Aucune donnée ne quitte le téléphone.

Sommelle est un outil d'accompagnement et d'éducation. Ce n'est pas un dispositif médical et elle ne remplace pas un avis médical.

**[Site de Sommelle](https://relthdrarxash.github.io/sommelle/)** · [Pour les professionnels de santé](https://relthdrarxash.github.io/sommelle/professionnels.html) · [Confidentialité](https://relthdrarxash.github.io/sommelle/confidentialite.html)

## Ce que fait l'appli

Pendant six à huit semaines :

- **Un questionnaire d'entrée** écarte la restriction du sommeil quand elle est déconseillée (apnée possible, trouble bipolaire, épilepsie, grossesse, métier à risque) et oriente vers un médecin si besoin.
- **Un agenda du sommeil** se remplit chaque matin en une vingtaine de secondes, sans clavier.
- **Une fenêtre de sommeil** (heures de coucher et de lever) est calculée après une semaine d'observation, puis ajustée chaque semaine selon les nuits.
- **Un écran pour la nuit**, presque noir, rappelle quoi faire quand le sommeil ne vient pas.
- **Cinq exercices cognitifs** se débloquent au fil du programme : idées sur le sommeil, mise au repos de la journée, intention paradoxale, décatastrophisation, relaxation.
- **Un bilan avant/après** compare la semaine d'observation aux dernières nuits.
- **Un résumé pour le médecin**, à montrer à l'écran ou à partager en PDF et en tableur.
- **Des rappels** optionnels le matin et le soir.

## Aperçu

<table>
  <tr>
    <td align="center"><img src="docs/captures/accueil.png" width="220" alt="Accueil : fenêtre de sommeil, coucher 0 h 30 et lever 6 h 45, bouton Remplir ma nuit"><br>Accueil</td>
    <td align="center"><img src="docs/captures/agenda_heure.png" width="220" alt="Agenda guidé : heure du coucher en grand avec deux boutons moins 15 et plus 15 minutes"><br>Agenda guidé</td>
    <td align="center"><img src="docs/captures/agenda_estimation.png" width="220" alt="Agenda guidé : grandes pastilles pour estimer le temps d'endormissement"><br>Estimations</td>
  </tr>
  <tr>
    <td align="center"><img src="docs/captures/agenda_resume.png" width="220" alt="Agenda résumé : toute la nuit sur une carte, chaque ligne modifiable"><br>Agenda résumé</td>
    <td align="center"><img src="docs/captures/bilan.png" width="220" alt="Bilan de la semaine : phrase de synthèse, nouvelle fenêtre et graphique temps au lit et temps dormi"><br>Bilan de la semaine</td>
    <td align="center"><img src="docs/captures/nuit.png" width="220" alt="Écran de nuit presque noir aux tons chauds, trois consignes et un bouton"><br>Je n'arrive pas à dormir</td>
  </tr>
  <tr>
    <td align="center"><img src="docs/captures/bienvenue.png" width="220" alt="Écran de bienvenue du questionnaire d'entrée"><br>Bienvenue</td>
    <td align="center"><img src="docs/captures/questionnaire.png" width="220" alt="Question du questionnaire d'entrée avec deux grands boutons Oui et Non"><br>Questionnaire</td>
    <td align="center"><img src="docs/captures/accueil_nuit.png" width="220" alt="Accueil en thème de nuit, sombre et chaud"><br>Thème de nuit</td>
  </tr>
  <tr>
    <td align="center"><img src="docs/captures/resume_medecin.png" width="220" alt="Résumé pour le médecin : programme, parcours, fenêtre actuelle, avec boutons de partage en PDF et en tableur"><br>Résumé pour le médecin</td>
    <td align="center"><img src="docs/captures/exercices.png" width="220" alt="Liste des exercices cognitifs avec leur état, fait ou à faire"><br>Exercices</td>
    <td align="center"><img src="docs/captures/bilan_programme.png" width="220" alt="Bilan du programme : comparaison entre la semaine d'observation et les sept dernières nuits"><br>Bilan avant/après</td>
  </tr>
</table>

Les captures sont générées à partir du code des écrans à chaque modification.

## Principes

- **Fondée sur les recommandations.** Agenda du Consensus Sleep Diary, restriction du temps au lit et contrôle du stimulus selon les recommandations européennes (2023) et américaines (2021). Les règles, seuils et sources sont décrits dans le [dossier scientifique](https://relthdrarxash.github.io/sommelle/protocole.html), avec leurs limites.
- **100 % locale.** Pas de compte, pas de serveur, pas de permission Internet, pas de traceur. Le code étant public, chacun peut le vérifier.
- **Pour tout le monde.** Conçue pour une personne mal réveillée, y compris âgée ou peu à l'aise avec le numérique : grands boutons, pas de jargon, pas de geste caché, police Atkinson Hyperlegible, thème de nuit chaud. Les choix sont expliqués dans les [principes d'interface](documentation/design.md).
- **Sans culpabilité.** Pas de score quotidien, pas de série à maintenir, pas de rouge.

## Documentation

| Document | Contenu |
|---|---|
| [Dossier scientifique](https://relthdrarxash.github.io/sommelle/protocole.html) | Pour les soignants : composantes de TCC-I, calculs avec exemple, sécurité, exercices, limites, sources (source : `docs/protocole.html`) |
| [Principes d'interface](documentation/design.md) | Pour qui, et comment l'interface s'adapte à une personne fatiguée |
| [Architecture](documentation/architecture.md) | Modules, données, accessibilité |
| [Contribuer](CONTRIBUTING.md) | Compiler, tester, captures d'écran, site |

## Compiler

Ouvrir le projet dans Android Studio, ou en ligne de commande :

```bash
./gradlew test                 # tests du moteur et de la logique d'écran
./gradlew :app:assembleDebug   # application de développement
```

L'APK de développement de chaque modification est aussi téléchargeable dans l'onglet Actions.

## Contact

Questions, retours d'utilisation, remarques de professionnels de santé : [escale.apps@proton.me](mailto:escale.apps@proton.me).

## En cas de détresse

Si vous traversez une période difficile, le 3114 (numéro national de prévention du suicide) est joignable gratuitement, 24 h/24 et 7 j/7.

## Licence

Sommelle est distribuée sous licence [GNU GPL v3.0](LICENSE). Vous pouvez l'utiliser, l'étudier, la modifier et la redistribuer, à condition que toute version redistribuée reste publiée sous la même licence, avec son code source.
