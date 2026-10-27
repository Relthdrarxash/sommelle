# Le protocole de Sommelle

Ce que fait l'appli, sur quelles recommandations elle s'appuie, et où s'arrête ce qu'elle peut faire. Toutes les valeurs citées ici se trouvent dans le code, regroupées dans `ProtocolConfig` (module `core:protocol`) avec un numéro de version.

Sommelle est un outil d'accompagnement et d'éducation, pas un dispositif médical. Elle ne pose aucun diagnostic et ne remplace pas un avis médical.

---

## 1. Socle scientifique

| Règle | Choix retenu | Sources |
|---|---|---|
| Traitement de référence | TCC-I multicomposante en première intention, y compris en format numérique | Riemann et al., *European Insomnia Guideline 2023*, J Sleep Res ; Edinger et al., *AASM Clinical Practice Guideline*, JCSM 2021 |
| Efficacité du numérique | TCC-I numérique efficace, effets plus faibles sans soutien humain, rétention = enjeu principal | Zachariae et al., Sleep Med Rev 2016 ; Soh et al., Sleep Med Rev 2020 |
| Agenda du sommeil | Consensus Sleep Diary (version Core) | Carney et al., Sleep 2012 |
| Semaine d'observation | 7 jours d'agenda avant toute restriction | Pratique standard des manuels de TCC-I |
| Restriction du temps au lit | Fenêtre initiale = temps de sommeil moyen de la semaine d'observation, plancher 5 h 30 (6 h pour les métiers à risque), jamais sous 5 h, jamais au-delà de 9 h | Spielman et al., Sleep 1987 ; Kyle et al., Sleep 2014 (effets secondaires) |
| Ajustement hebdomadaire | Efficacité ≥ 85 % : +15 min ; < 80 % : −15 min (sans passer sous le plancher) ; entre les deux : inchangé. Seuils abaissés de 5 points à partir de 65 ans | Pratique standard ; adaptation seniors à valider |
| Heure de lever fixe | Ancre de la fenêtre, 7 jours sur 7 | Spielman 1987 |
| Contrôle du stimulus | Se lever après environ 20 min sans sommeil (estimé, pas chronométré), lit réservé au sommeil, pas de sieste ou sieste courte avant 15 h | Bootzin 1972 ; AASM 2021 |
| Hygiène du sommeil | Contenu secondaire uniquement : non recommandée seule | AASM 2021 |

### Calculs

- Temps au lit = sortie du lit − mise au lit.
- Temps de sommeil = (réveil final − extinction) − temps d'endormissement − éveils nocturnes. Le temps passé au lit avant d'éteindre compte dans le temps au lit mais pas dans le sommeil, ce qui rejoint le contrôle du stimulus.
- Efficacité du sommeil = temps de sommeil / temps au lit.
- Efficacité d'une semaine = somme des temps de sommeil / somme des temps au lit : chaque nuit pèse selon sa durée.
- Il faut au moins 5 nuits renseignées sur 7 pour calculer ou ajuster une fenêtre. Sinon, la fenêtre est maintenue et l'appli explique pourquoi.
- La fenêtre initiale est arrondie au quart d'heure supérieur, puis bornée entre le plancher et 9 h.

---

## 2. Sécurité

Un questionnaire d'entrée de 2 à 3 minutes oriente vers l'un de trois parcours : complet, sans restriction (contrôle du stimulus, exercices cognitifs, éducation), ou orientation vers un médecin.

| Situation | Issue |
|---|---|
| Ronflements forts avec pauses respiratoires constatées, ou somnolence marquée dans la journée | Orientation médecin (apnée possible), parcours sans restriction en attendant |
| Trouble bipolaire, épilepsie | Parcours sans restriction et orientation médecin |
| Grossesse | Parcours sans restriction |
| Conduite professionnelle, machines dangereuses, métier de sécurité | Restriction avec plancher relevé à 6 h et avertissement renforcé, ou parcours sans restriction, au choix de la personne |
| Travail posté ou de nuit | Hors du champ de l'appli : orientation et contenu éducatif |
| Nuits interrompues par une cause extérieure (nourrisson, soins à un proche) | Message adapté : la TCC-I cible l'insomnie, pas ce type de privation |
| Coucher et lever très tardifs avec un sommeil normal une fois endormi | Signalement d'un possible retard de phase, orientation |
| Détresse exprimée | Le 3114 est mis en avant, en plus de sa présence permanente dans l'appli |

Un avertissement sur la somnolence au volant s'affiche au début de la restriction et à chaque réduction de la fenêtre.

---

## 3. Déroulé du programme

| Semaine | Contenu |
|---|---|
| 0 | Observation : agenda du matin seul, aucune règle contraignante |
| 1 | Première fenêtre de sommeil, contrôle du stimulus |
| 2 à 5 | Ajustement hebdomadaire de la fenêtre et exercices cognitifs, débloqués au fil des semaines |
| 6 | Bilan avant/après |
| Ensuite | Maintien : la fenêtre continue de s'ajuster chaque semaine |

Une nuit peut être remplie jusqu'à 48 h après. Aucun mécanisme ne culpabilise : pas de série perdue, pas de rouge.

### Exercices cognitifs

Chaque exercice se fait en quelques minutes, une étape par écran, et reste disponible une fois débloqué.

| Semaine | Exercice | Technique |
|---|---|---|
| 2 | Les idées sur le sommeil | Restructuration des croyances dysfonctionnelles (Morin, DBAS) |
| 2 | Mettre sa journée au repos | Temps de décompression et « inquiétude constructive » en début de soirée (Carney et Waters, 2006) |
| 3 | Arrêter de chercher le sommeil | Intention paradoxale (Espie et Lindsay, 1985) |
| 4 | Revenir au plus probable | Décatastrophisation des conséquences d'une mauvaise nuit |
| 5 | Relâcher le corps | Relaxation musculaire progressive (Jacobson ; recommandée par l'AASM 2021) |

### Bilan avant/après

À partir de la semaine 6, l'appli compare la semaine d'observation aux 7 dernières nuits : temps d'endormissement, éveils nocturnes, temps de sommeil, efficacité, qualité ressentie. L'évolution est écrite en toutes lettres (« En amélioration », « Stable », « En recul »), jamais portée par la seule couleur.

Une question simple sur la gêne ressentie dans la journée (de 0 à 4) est posée au début et à la fin. Ce n'est pas une échelle validée : l'Index de sévérité de l'insomnie (ISI) serait l'outil de référence, mais il est soumis à licence.

### Résumé pour le médecin

La personne peut montrer un résumé à son médecin, à l'écran ou en le partageant :
- en PDF : parcours, fenêtre actuelle, tableau hebdomadaire, bilan avant/après, 28 dernières nuits ;
- en tableur (CSV) : l'agenda complet.

Les deux fichiers sont générés sur le téléphone et partagés par la personne elle-même.

---

## 4. Limites et points à valider

Plusieurs paramètres sont des choix de conception raisonnés, pas des valeurs établies. Ils sont signalés comme tels dans le code et doivent être relus par un professionnel du sommeil :

- plancher de fenêtre (5 h 30) et seuils d'ajustement, notamment l'adaptation à partir de 65 ans ;
- efficacité hebdomadaire calculée sur les totaux plutôt qu'en moyenne des nuits, et arrondi supérieur de la fenêtre initiale ;
- plafond de la fenêtre à 9 h ;
- contenu des exercices cognitifs et semaine de déblocage ;
- question sur la gêne dans la journée, en attendant une échelle validée ;
- règles d'orientation et formulation des messages, dont l'avertissement sur la somnolence ;
- ordre d'introduction des composantes.

L'efficacité de l'appli elle-même n'a pas fait l'objet d'une étude clinique.
