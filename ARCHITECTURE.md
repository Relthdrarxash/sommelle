# Sommelle : architecture et parcours (proposition v0.1)

Application Android d'accompagnement à l'insomnie fondée sur la TCC-I. 100 % locale, sans compte, gratuite, en français.

Statut : proposition en cours de validation. Le moteur `core:protocol` est implémenté et testé ; les modules Android restent à construire.

---

## 1. Socle scientifique

| Règle | Choix retenu | Sources |
|---|---|---|
| Traitement de référence | TCC-I multicomposante en première intention, y compris en format numérique | Riemann et al., *European Insomnia Guideline 2023*, J Sleep Res ; Edinger et al., *AASM Clinical Practice Guideline*, JCSM 2021 |
| Efficacité du numérique | TCC-I numérique efficace, effets plus faibles sans soutien humain, rétention = enjeu principal | Zachariae et al., Sleep Med Rev 2016 ; Soh et al., Sleep Med Rev 2020 |
| Agenda du sommeil | Consensus Sleep Diary (version Core) | Carney et al., Sleep 2012 |
| Ligne de base | 7 jours d'agenda avant toute restriction (14 si nuits très variables) | Pratique standard des manuels de TCC-I |
| Restriction du temps au lit | Fenêtre initiale = temps de sommeil total moyen de la ligne de base, plancher 5 h 30 (paramétrable, jamais sous 5 h) | Spielman et al., Sleep 1987 ; Kyle et al., Sleep 2014 (effets secondaires) |
| Ajustement hebdomadaire | Efficacité ≥ 85 % : +15 min ; < 80 % : -15 min (sans passer sous le plancher) ; entre les deux : inchangé. Seuils abaissés de 5 points pour les 65 ans et plus | Pratique standard ; adaptation seniors à valider |
| Heure de lever fixe | Ancre de la fenêtre, 7 jours sur 7 | Spielman 1987 |
| Contrôle du stimulus | Se lever après environ 20 min sans sommeil (estimé, pas chronométré), lit réservé au sommeil, pas de sieste ou sieste courte avant 15 h | Bootzin 1972 ; AASM 2021 |
| Hygiène du sommeil | Contenu secondaire uniquement : non recommandée seule | AASM 2021 |
| Échelles validées | ISI et Epworth sous licence (Mapi Research Trust) : à vérifier avant intégration. Par défaut, suivi par indicateurs de l'agenda | À vérifier |

**Calculs (module `core:protocol`)**
- Temps au lit (TIB) = sortie du lit (Q7) − mise au lit (Q1)
- Temps de sommeil total (TST) = (réveil final (Q6) − extinction (Q2)) − latence d'endormissement (Q3) − éveils nocturnes (Q5). Le temps passé au lit avant l'extinction compte dans le TIB mais pas dans le TST, ce qui rejoint le contrôle du stimulus.
- Efficacité du sommeil (SE) = TST / TIB
- Efficacité d'une semaine = somme des TST / somme des TIB (chaque nuit pondérée par sa durée). Minimum 5 nuits renseignées sur 7 pour calculer ou ajuster une fenêtre (sinon, fenêtre maintenue et message explicatif).
- Fenêtre initiale arrondie au quart d'heure supérieur, puis bornée entre le plancher et 9 h.

---

## 2. Sécurité et exclusions

Questionnaire d'entrée, trois issues possibles : parcours complet, parcours sans restriction (contrôle du stimulus, cognitif, éducation), ou orientation vers un médecin.

| Situation | Issue |
|---|---|
| Ronflements forts + pauses respiratoires constatées ou somnolence marquée | Orientation médecin (apnée suspectée), parcours sans restriction en attendant |
| Trouble bipolaire, épilepsie | Parcours sans restriction + orientation médecin |
| Grossesse | Parcours sans restriction |
| Conduite professionnelle, machines dangereuses, métier de sécurité | Restriction avec plancher relevé à 6 h et avertissement somnolence renforcé, ou sans restriction (choix de l'utilisateur) |
| Travail posté ou de nuit | Hors périmètre v1 : orientation et contenu éducatif seulement |
| Nuits interrompues par une cause externe (nourrisson, soins à un proche) | Message adapté : la TCC-I cible l'insomnie, pas ce type de privation |
| Coucher et lever très tardifs mais sommeil normal une fois endormi | Signalement d'un possible retard de phase, orientation |
| Détresse psychique exprimée | Affichage permanent du 3114 dans l'aide, sans diagnostic |

Avertissement somnolence au volant affiché au début de la restriction et à chaque réduction de fenêtre.

---

## 3. Typologie des utilisateurs

| Profil | Particularités | Conséquences de conception |
|---|---|---|
| Actif 25-55 ans | Tranche la plus touchée, charge mentale, peu de temps | Saisie en 20 s, rappels courts, ton direct |
| Femmes (surreprésentées) | Insomnie liée aux transitions hormonales, charge familiale | Textes non genrés, contenu sur ménopause et sommeil en module éducatif |
| Seniors 65+ | Sommeil naturellement plus fragmenté, littératie numérique variable | Seuils d'efficacité abaissés, police large par défaut, parcours sans jargon |
| Étudiants, jeunes adultes | Horaires irréguliers, écrans au lit | Insistance sur heure de lever fixe, signaux d'écran utiles |
| Personnes précaires | Téléphones d'entrée de gamme, forfaits limités, plus forte prévalence | APK léger, 100 % hors ligne, aucune dépendance réseau |
| Faible littératie | Difficulté avec les concepts abstraits | Langage clair, pictogrammes, une idée par écran |

Le profil n'est jamais demandé tel quel : il découle de quelques questions (âge par tranche, horaires, préférences d'affichage) et ajuste textes, seuils et rappels.

---

## 4. Parcours

1. **Accueil** : ce que fait l'appli, ce qu'elle ne fait pas, données locales.
2. **Questionnaire d'entrée** (sécurité + profil), 2 à 3 minutes.
3. **Semaine 0, ligne de base** : agenda seul + contenu éducatif léger. Pas encore de règles contraignantes.
4. **Semaines 1 à 6** : chaque lundi, bilan + nouvelle fenêtre expliquée. Introduction progressive : contrôle du stimulus (S1), modules cognitifs (S2-S4), prévention des rechutes (S6).
5. **Maintien** : agenda allégé (facultatif), fenêtre stabilisée, bilan mensuel.

Rétention : un seul geste par jour (agenda du matin), progression visible (graphique d'efficacité), aucun mécanisme culpabilisant (pas de série perdue, pas de rouge). Un jour manqué peut être rempli jusqu'à 48 h après.

---

## 5. Architecture technique

Kotlin, Jetpack Compose, Material 3, Room, Navigation Compose, WorkManager. minSdk 26, targetSdk 35. Pas de framework d'injection pour l'instant : l'application assemble un dépôt unique et le passe aux écrans, ce qui suffit à cette taille et garde le build simple.

```
:app                          Navigation, assemblage des modules
:core:model                   Entités pures (Kotlin, sans Android)
:core:protocol                Moteur TCC-I pur Kotlin, testé
:core:presentation            Logique d'écran pur Kotlin, testée (mise en forme, agenda, calendrier, bilan)
:core:data                    Room, DAO, dépôt unique
:core:designsystem            Thèmes jour et nuit, police, composants accessibles
:feature:onboarding           Questionnaire d'entrée
:feature:home                 Accueil quotidien, heure de lever
:feature:diary                Agenda du sommeil (guidé et résumé)
:feature:week                 Bilan hebdomadaire et graphique
:feature:night                « Je n'arrive pas à dormir »
:feature:cognitive            Exercices cognitifs (croyances, journée au repos, intention paradoxale,
                              décatastrophisation, relaxation)
:feature:export               Résumé pour le médecin : écran, PDF et CSV générés sur le téléphone
:feature:reminders            Rappels du matin et du soir (WorkManager)
:feature:settings             Réglages, effacement des données
:screenshots                  Captures de la documentation et des stores (Paparazzi)

À venir :
:feature:education            Contenus éducatifs complets
:integration:healthconnect    Pré-remplissage depuis Health Connect
:integration:usagestats       Pré-remplissage par signaux d'écran
```

**Principes de modularité**
- `core:protocol` ne dépend de rien d'Android : les règles cliniques sont testables, relisibles par un professionnel et versionnées (`ProtocolConfig` avec numéro de version, chaque fenêtre calculée garde la version qui l'a produite).
- Chaque composante TCC-I implémente une interface `ProtocolComponent` (prérequis, semaine d'activation, éligibilité selon le profil). Le moteur assemble le plan à partir des composantes actives : en ajouter ou en retirer ne touche pas au reste.
- Les sources de pré-remplissage implémentent `SleepSuggestionSource` avec une priorité. L'agenda validé par l'utilisateur fait toujours foi ; les suggestions sont tracées (champ `source`) mais jamais utilisées directement dans les calculs.
- Les intégrations sont optionnelles : refuser une permission désactive le module, sans effet sur le reste.

**Données**
- Room en local, sauvegarde Android Auto Backup, export chiffré (mot de passe utilisateur).
- Aucun SDK d'analytics, de publicité ou de crash reporting tiers.
- Effacement complet en un geste depuis les réglages.

---

## 6. Accessibilité

Cible WCAG 2.2 AA et RGAA.
- TalkBack : descriptions sur tous les éléments, ordre de lecture vérifié, sélecteurs d'heure utilisables sans geste fin.
- Police : respect de la taille système jusqu'à 200 %, mises en page qui se réorganisent au lieu de tronquer.
- Contrastes ≥ 4,5:1 ; thème sombre chaud par défaut entre 21 h et 7 h.
- Cibles tactiles ≥ 48 dp ; aucune information portée par la seule couleur.
- Langage clair : phrases courtes, une action par écran, glossaire intégré.
- Tests automatisés (Compose UI tests + Accessibility Scanner) dans la CI.

---

## 7. Périmètre du MVP

1. Questionnaire d'entrée (sécurité + profil minimal)
2. Agenda du sommeil (Consensus Sleep Diary Core), saisie manuelle
3. Ligne de base de 7 jours puis calcul et ajustement hebdomadaire de la fenêtre
4. Rappel quotidien local
5. Écran de progression simple

Reportés : intégrations, modules cognitifs, export PDF, contenus éducatifs complets.

---

## 8. Points à faire valider par un professionnel du sommeil

- Plancher de fenêtre (5 h 30) et seuils d'ajustement, notamment l'adaptation seniors
- Efficacité hebdomadaire calculée sur les totaux plutôt qu'en moyenne des nuits, et arrondi supérieur de la fenêtre initiale
- Plafond de la fenêtre à 9 h
- Contenu des cinq exercices cognitifs et semaine de déblocage
- Question sur le ressenti de la journée (non validée) utilisée dans le bilan avant/après, en attendant une échelle validée dont la licence est compatible
- Règles d'exclusion et formulation des orientations
- Formulation de l'avertissement somnolence
- Ordre d'introduction des composantes
- Utilisation éventuelle de l'ISI (licence et pertinence)
