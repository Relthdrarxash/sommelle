# Architecture technique

Kotlin, Jetpack Compose, Material 3, Room, Navigation Compose, WorkManager. minSdk 26 (Android 8), targetSdk 35. Aucune permission réseau.

## Modules

```
:app                    Navigation et assemblage des modules
:core:model             Entités pures : agenda, questionnaire, fenêtre de sommeil
:core:protocol          Moteur TCC-I : métriques, validation, sécurité, fenêtre, plan du programme
:core:presentation      Logique d'écran : mise en forme, agenda, bilan, exercices, résumé, rappels
:core:data              Base Room locale et dépôt unique
:core:designsystem      Thèmes jour et nuit, police, composants accessibles
:feature:onboarding     Questionnaire d'entrée
:feature:home           Accueil quotidien, heure de lever
:feature:diary          Agenda du sommeil, guidé et résumé
:feature:week           Bilan de la semaine et bilan avant/après
:feature:night          « Je n'arrive pas à dormir »
:feature:cognitive      Exercices cognitifs
:feature:export         Résumé pour le médecin : écran, PDF, CSV
:feature:reminders      Rappels du matin et du soir
:feature:settings       Réglages et effacement des données
:screenshots            Captures de la documentation et des stores (non embarqué)
```

## Principes

- **Les règles cliniques sont isolées.** `core:model`, `core:protocol` et `core:presentation` sont en pur Kotlin, sans dépendance Android. Elles se testent sans téléphone et se relisent sans connaître Android.
- **Les paramètres sont versionnés.** Toutes les valeurs cliniques sont dans `ProtocolConfig`. Chaque fenêtre de sommeil enregistrée garde la version qui l'a produite.
- **Le programme est un assemblage.** Chaque composante (agenda, restriction, contrôle du stimulus, exercices) implémente `ProtocolComponent` : semaine d'activation et éligibilité selon le questionnaire. En ajouter ou en retirer ne touche pas au reste du moteur.
- **Les écrans sont sans état.** Chaque écran existe en version `XxxContent` qui ne reçoit que des données. C'est elle que testent les captures automatiques.
- **Pas de framework d'injection.** L'application assemble un dépôt unique et le fournit aux écrans, ce qui suffit à cette taille.

## Données

- Base Room sur le téléphone, migrations explicites entre versions de schéma.
- Aucun compte, aucun serveur, aucun SDK d'analytics, de publicité ou de rapport de plantage.
- Les rappels sont planifiés localement par WorkManager.
- Le PDF et le CSV du résumé sont générés dans le cache de l'appli et partagés via le sélecteur Android : rien n'est envoyé sans action de la personne.
- Effacement complet en un geste depuis les réglages.
- Si la sauvegarde Android est activée sur le téléphone, elle peut inclure les données de Sommelle dans le compte Google de la personne, chiffrées. La [politique de confidentialité](https://relthdrarxash.github.io/sommelle/confidentialite.html) le précise.

## Accessibilité

Cible WCAG 2.2 AA et RGAA. Les choix d'interface sont détaillés dans [design.md](design.md).

- TalkBack : descriptions sur tous les éléments, graphiques et tableaux lus d'un bloc avec leur sens.
- Taille du texte système respectée ; corps de texte à 18 sp, jamais sous 15 sp.
- Cibles tactiles de 64 dp pour les actions principales, 48 dp minimum ailleurs.
- Contrastes de texte de 4,5:1 minimum, dans les deux thèmes.
- Aucune information portée par la seule couleur.

## Qualité

Chaque modification passe par l'intégration continue : tests unitaires des modules pur Kotlin, analyse Android, construction de l'APK, et régénération des captures d'écran. Le détail est dans [CONTRIBUTING.md](../CONTRIBUTING.md).
