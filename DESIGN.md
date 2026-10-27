# Sommelle : principes d'interface

Comment rendre l'appli moderne et utilisable par tout le monde, y compris les personnes peu à l'aise avec le numérique.

Statut : proposition v0.1, à confronter à des tests utilisateurs (section 9).

---

## 1. Le vrai utilisateur : une personne fatiguée

La typologie d'ARCHITECTURE.md décrit qui utilise Sommelle. Ce qui compte encore plus pour l'interface, c'est **dans quel état** on l'utilise.

- **Le matin, au réveil**, au moment de remplir l'agenda. L'inertie du sommeil dégrade nettement la mémoire de travail : le pic se situe dans les toutes premières minutes, et des effets restent mesurables jusqu'à deux heures (Université du Colorado, petite étude, à confirmer). Et un insomniaque se réveille après une mauvaise nuit.
- **La nuit, au lit**, quand on ne dort pas. La personne est anxieuse, dans le noir, et toute lumière vive ou tout chiffre peut aggraver la situation.
- **Le soir**, au moment de se coucher selon la fenêtre prescrite, souvent plus tard qu'avant et avec l'envie de tricher.

**Conséquence** : Sommelle se conçoit pour un cerveau au ralenti. Si l'interface marche pour quelqu'un de mal réveillé, elle marchera pour tout le monde. C'est le même raisonnement que l'accessibilité en général : ce qui aide les plus fragiles aide aussi tous les autres.

---

## 2. Ce que « moderne » veut dire ici

Moderne ne veut pas dire minimaliste. La tendance « flat » des années 2010 (icônes sans libellé, boutons qui ressemblent à du texte, gris clair sur blanc) est précisément ce qui perd les personnes peu à l'aise avec le numérique.

Google a fondé Material 3 Expressive (2025) sur 46 études et plus de 18 000 participants. Avec suivi oculaire, les éléments clés étaient repérés jusqu'à 4 fois plus vite dans les versions expressives. Surtout, les participants de plus de 45 ans, plus lents à repérer ces éléments dans les interfaces actuelles, faisaient aussi bien que les plus jeunes dans les versions expressives.

**Choix retenu** : Material 3 Expressive comme base (natif dans Jetpack Compose), avec :
- des formes franches et des boutons qui ressemblent à des boutons ;
- une hiérarchie visible : une action principale par écran, nettement plus grande que le reste ;
- des animations qui expliquent (d'où vient un élément, où il va), jamais décoratives, et désactivées si le réglage système « supprimer les animations » est actif ;
- un retour haptique léger sur les validations.

Sommelle doit avoir l'air d'une appli de 2026, soignée et rassurante, sans ressembler à un logiciel « pour seniors ». Une interface qui infantilise fait fuir tout le monde, y compris les personnes âgées.

---

## 3. Tout public : règles non négociables

En France, 16 % des 15 ans et plus étaient en situation d'illectronisme en 2019, et cette part montait à 67 % chez les 75 ans et plus (INSEE). L'insomnie touche davantage les personnes âgées et précaires, qui sont aussi les plus exposées à l'illectronisme.

| Règle | Pourquoi |
|---|---|
| Toute icône est accompagnée d'un libellé | Une icône seule est une devinette |
| Aucun geste caché : pas de balayage ni d'appui long obligatoire | Ces gestes ne se découvrent pas |
| Une seule action principale par écran | Pas d'hésitation sur « où appuyer » |
| Le bouton retour ne fait jamais perdre de données | Supprime la peur de « casser quelque chose » |
| Annuler plutôt que confirmer | « Saisie enregistrée. Annuler » vaut mieux qu'une boîte « Êtes-vous sûr ? » |
| Pas de clavier quand on peut l'éviter | Saisir au clavier au réveil est pénible et source d'erreurs |
| Pas de jargon | « Temps dormi » plutôt que « TST », « nuits plus pleines » plutôt que « efficacité » à l'écran (le pourcentage reste disponible) |
| Vouvoiement chaleureux | Convient à tous les âges dans un contexte de santé |
| Pas de compte, pas de mot de passe | Un obstacle de moins, cohérent avec le 100 % local |
| Fonctionne sur un téléphone d'entrée de gamme | APK léger, pas d'animation lourde, pas de réseau |

---

## 4. Saisir l'agenda en 20 secondes, même mal réveillé

C'est l'écran le plus important : s'il est pénible, les gens abandonnent, et le programme s'arrête.

**Pas de cadran d'horloge.** Le sélecteur d'heure circulaire est difficile pour beaucoup de gens. Chaque heure s'affiche en grand, pré-remplie, avec deux gros boutons « − 15 min » et « + 15 min » de part et d'autre. Un appui sur l'heure ouvre une saisie directe pour ceux qui préfèrent.

**Des estimations, pas des chronométrages.** La TCC-I repose sur le ressenti, et demander des minutes exactes pousse à surveiller l'horloge la nuit. La latence et les éveils se choisissent parmi de grandes pastilles : « Moins de 5 min », « 15 min », « 30 min », « 45 min », « 1 h », « Plus ». Chaque pastille correspond à une valeur enregistrée.

**Deux modes, qui évoluent avec la personne :**
1. **Guidé** (par défaut les deux premières semaines) : une question par écran, une barre de progression, un gros bouton « Suivant ». Six écrans courts, sans rien à mémoriser.
2. **Résumé** (proposé une fois l'habitude prise) : une seule carte « Hier soir 23 h 15 → ce matin 6 h 45 », avec chaque élément modifiable d'un appui, puis « C'est bien ça ».

**Pré-remplissage visible.** Les valeurs proposées (veille, montre, écran) sont signalées par une petite mention « proposé à partir de votre montre ». Rien n'est enregistré sans un appui sur « Valider ».

**Oublis tolérés.** Une nuit peut être remplie jusqu'à 48 h après, sans reproche. Le message du lendemain dit simplement : « Vous n'avez pas rempli la nuit de mardi. Vous vous en souvenez ? », avec un bouton « Je ne sais plus » qui laisse la nuit vide.

---

## 5. Ne pas aggraver l'insomnie

Une appli de sommeil peut nuire au sommeil. Deux pièges documentés :
- **L'orthosomnie** : la quête d'un sommeil « parfait » mesuré par des applis et objets connectés peut entretenir l'anxiété et l'insomnie (Baron et al., J Clin Sleep Med, 2017).
- **La surveillance de l'horloge** la nuit augmente les inquiétudes et retarde l'endormissement (Tang, Schmidt et Harvey, J Behav Ther Exp Psychiatry, 2007).

**Choix retenus :**
- **Pas de score de sommeil quotidien**, pas de note sur 100, pas de comparaison avec les autres. Le bilan est hebdomadaire, quand l'ajustement de la fenêtre a lieu.
- **Pas de série de jours** ni de badge qui se perd : rien qui culpabilise après une mauvaise nuit.
- **Pas de rouge** pour les résultats. Une semaine difficile est présentée comme une information, pas comme un échec.
- **La nuit, aucune heure ni aucun chiffre à l'écran.**

---

## 6. Les trois moments de la journée

**Le matin : remplir l'agenda.** Interface claire et douce, grandes cibles, parcours décrit en section 4. La notification du matin ouvre directement la saisie, sans passer par l'accueil.

**Le soir : l'heure du coucher.** Un rappel discret avant l'heure de la fenêtre, avec une phrase qui explique plutôt qu'elle n'ordonne : « Votre fenêtre commence à 0 h 30. Rester debout jusque-là, c'est ce qui rend le sommeil plus profond. » La restriction est la partie la plus contre-intuitive du programme et celle où l'on décroche le plus : chaque rappel en redonne la raison, en une phrase.

**La nuit : « Je n'arrive pas à dormir ».** Un seul gros bouton, accessible depuis l'accueil et depuis un raccourci. Il ouvre un écran presque noir, aux couleurs chaudes et très atténuées, avec la luminosité de l'appli abaissée. Il rappelle simplement la règle du contrôle du stimulus (se lever, aller dans une autre pièce, revenir quand le sommeil revient) et propose une activité calme hors du lit. Aucun chiffre, aucune heure, aucun défilement. L'écran se ferme tout seul : on ne doit pas rester sur son téléphone au lit.

---

## 7. Montrer la progression

- **Une phrase avant un graphique** : « Cette semaine, vous avez dormi 88 % du temps passé au lit. Votre fenêtre s'allonge de 15 minutes : coucher à 0 h 15. » Le graphique vient après, pour ceux qui le veulent.
- **Un seul graphique principal** : barres hebdomadaires du temps au lit et du temps dormi, les deux se rapprochant au fil des semaines. C'est l'image qui explique le mieux le principe de la restriction.
- **Accessibilité** : chaque graphique a une description textuelle complète lue par TalkBack, et aucune information n'est portée par la seule couleur.

---

## 8. Système visuel

| Élément | Choix | Raison |
|---|---|---|
| Base | Material 3 Expressive (Compose) | Repérage plus rapide, notamment après 45 ans |
| Police | Atkinson Hyperlegible Next (Braille Institute, gratuite) | Conçue pour la basse vision : chaque lettre se distingue (I, l, 1 ; O, 0) |
| Taille de texte | 18 sp par défaut pour le corps, jamais sous 16 sp ; suit le réglage système jusqu'à 200 % | Lisible sans lunettes au réveil |
| Cibles tactiles | 56 dp pour les actions principales, 48 dp minimum ailleurs | Au-delà du minimum, pour des doigts imprécis au réveil |
| Thème de jour | Clair et doux, sans blanc pur, contraste ≥ 4,5:1 | Moins agressif au réveil |
| Thème de nuit | Sombre et chaud (pas de bleu), automatique entre 21 h et 7 h, contraste maintenu | Lumière moins stimulante, lisibilité préservée |
| Illustrations | Motifs nocturnes simples (ciel, lune, lumière douce), pas de mascotte | Chaleur sans infantiliser |
| Vocabulaire | Phrases courtes, une idée par phrase, glossaire intégré | Inspiré du FALC |

---

## 9. Valider avec de vraies personnes

Ces choix restent des hypothèses tant qu'ils n'ont pas été testés.

- **Qui** : au moins cinq personnes par vague, dont des plus de 65 ans et des personnes peu à l'aise avec le numérique. Les conseillers numériques des espaces France Services et les associations d'inclusion numérique (Emmaüs Connect par exemple) peuvent aider à les recruter.
- **Quoi** : remplir l'agenda juste après le réveil (vrai contexte, pas en salle de réunion), comprendre la fenêtre de sommeil et retrouver le bouton de nuit.
- **Indicateurs** : temps de saisie de l'agenda (objectif sous 30 s en mode guidé), taux d'erreur, nuits remplies sur la durée du programme, score SUS (System Usability Scale).
- **Accessibilité** : passage complet avec TalkBack, à 200 % de taille de texte, et avec Accessibility Scanner.

---

## 10. Pistes pour plus tard

- **Widget d'accueil** pour remplir l'agenda sans ouvrir l'appli, ajouté depuis l'appli d'un seul appui (sans manipulation de l'écran d'accueil).
- **Saisie vocale** (« Je me suis couché vers 23 h, endormi vers minuit ») analysée sur le téléphone, sans envoi de données.
- **Mode aidant** : une version très simplifiée qu'un proche peut configurer pour une personne âgée.

---

## Sources

- Google, Material 3 Expressive, 2025 : [Chrome Unboxed](https://chromeunboxed.com/google-leaks-the-reason-and-process-behind-their-new-material-3-expressive-design-language)
- INSEE, illectronisme : [Insee Analyses n° 85](https://www.insee.fr/fr/statistiques/7636058)
- Inertie du sommeil, Université du Colorado : [CU Boulder Today](https://www.colorado.edu/today/node/6796)
- Baron KG et al., « Orthosomnia: are some patients taking the quantified self too far? », J Clin Sleep Med, 2017
- Tang NKY, Schmidt DA, Harvey AG, « Sleeping with the enemy: clock monitoring in the maintenance of insomnia », J Behav Ther Exp Psychiatry, 2007
