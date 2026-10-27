# Sommelle : kit de lancement

Tout ce qu'il faut pour faire connaître Sommelle, dans l'ordre. Les supports prêts à l'emploi sont listés en fin de document.

---

## 1. Le message

**En une phrase**
> La méthode recommandée contre l'insomnie, gratuite, sans compte, et vos données restent sur votre téléphone.

**En un paragraphe**
> Sommelle accompagne les adultes insomniaques pendant six à huit semaines avec la thérapie cognitivo-comportementale de l'insomnie, le traitement conseillé en premier par les recommandations européennes et américaines, avant les somnifères. Un agenda du matin en vingt secondes, des horaires de sommeil ajustés chaque semaine, un appui la nuit. Gratuite, libre, sans publicité, et rien ne quitte le téléphone.

**Pour les professionnels de santé**
> Une alternative concrète aux hypnotiques, à proposer dès la consultation : la TCC-I dans la poche du patient, avec un questionnaire de sécurité qui oriente vers le médecin quand il le faut.

### Ce qu'on ne dit jamais

| Ne pas dire | Dire plutôt |
|---|---|
| « Soigne l'insomnie », « guérit », « traitement » | « Accompagne », « fondée sur la TCC-I » |
| « Remplace les somnifères » | « Une alternative ou un relais, à voir avec son médecin » |
| « Validée cliniquement » (tant que ce n'est pas le cas) | « Fondée sur les recommandations » |
| « 100 % anonyme » | « Aucune donnée collectée, rien n'est envoyé » |

Sommelle n'est pas un dispositif médical : la moindre revendication thérapeutique, y compris sur le site ou les réseaux, pourrait la faire basculer dans le champ du règlement européen sur les dispositifs médicaux.

---

## 2. Le plan

### Étape 1 : avant d'en parler (novembre et décembre 2026)
- [ ] Relecture du protocole par un professionnel du sommeil (ARCHITECTURE.md, section 8).
- [ ] Test avec 5 à 10 personnes sur trois semaines au moins, dont des plus de 65 ans et des personnes peu à l'aise avec le numérique.
- [ ] Activer GitHub Pages (site et politique de confidentialité en ligne).
- [ ] Créer la clé de signature et les secrets de publication (CONTRIBUTING.md).

### Étape 2 : lancement discret (janvier 2027)
- [ ] Première version publiée sur GitHub (`git tag v0.1.0`).
- [ ] Soumission à F-Droid (section 4).
- [ ] Test fermé sur Google Play, puis production (section 3).
- [ ] Message dans deux ou trois communautés (modèle en section 6).

### Étape 3 : prescripteurs (février 2027)
- [ ] Contact avec le Réseau Morphée et la SFRMS (modèle en section 5).
- [ ] Fiche imprimée chez quelques médecins généralistes et pharmacies de proximité.
- [ ] Service de santé universitaire de Rennes ou de Paris.

### Étape 4 : médias (mars 2027)
- [ ] Communiqué court envoyé deux à trois semaines avant la Journée du sommeil, mi-mars (modèle en section 7).

---

## 3. Google Play

**Compte** : compte développeur personnel (frais d'inscription uniques). Les nouveaux comptes personnels doivent d'abord faire un test fermé avec un nombre minimum de testeurs pendant deux semaines avant de publier en production : vérifier les exigences en vigueur dans la Play Console.

**Fiche** : les textes et visuels sont dans `fastlane/metadata/android/fr-FR/` et se mettent à jour avec l'appli. La bannière et l'icône se régénèrent avec `python3 marketing/build.py`.

**Réglages à remplir dans la Play Console**

| Rubrique | Réponse |
|---|---|
| Catégorie | Santé et remise en forme |
| Public cible | 18 ans et plus |
| Publicité | Non |
| Politique de confidentialité | https://relthdrarxash.github.io/Sommelle/confidentialite.html |
| Applications de santé | Déclarer l'usage : suivi du sommeil et accompagnement comportemental ; pas de dispositif médical |
| Sécurité des données : collecte | Aucune donnée collectée |
| Sécurité des données : partage | Aucune donnée partagée |
| Chiffrement en transit | Sans objet (aucune transmission) |
| Suppression des données | Désinstaller l'appli ou effacer ses données efface tout |

---

## 4. F-Droid

F-Droid ne publie que des applis libres sans traceur : Sommelle remplit déjà les conditions (licence GPL, aucune dépendance propriétaire, aucun service Google). Les textes et captures sont lus directement depuis le dossier `fastlane/` du dépôt.

Démarche : proposer l'appli dans le dépôt `fdroiddata` sur GitLab (fichier de métadonnées pointant vers ce dépôt et l'étiquette de version), selon le guide « Inclusion How-To » de F-Droid.

---

## 5. Modèle : premier contact avec un professionnel

> **Objet** : Une appli gratuite de TCC-I, et votre regard de spécialiste
>
> Bonjour [Docteur / Madame / Monsieur],
>
> Je développe Sommelle, une application Android gratuite et libre qui accompagne les adultes insomniaques avec les composantes de la TCC-I : agenda du sommeil, restriction du temps au lit ajustée chaque semaine, contrôle du stimulus. Un questionnaire d'entrée écarte la restriction et oriente vers le médecin en cas d'apnée suspectée, de somnolence diurne, de trouble bipolaire, d'épilepsie ou de grossesse. Aucune donnée ne quitte le téléphone.
>
> Avant de la diffuser, je souhaite faire relire le protocole par un professionnel du sommeil. Accepteriez-vous d'y consacrer une demi-heure ? Le détail, avec les seuils et les sources, est ici : https://relthdrarxash.github.io/Sommelle/professionnels.html
>
> Merci pour votre temps,
> [Prénom Nom]

---

## 6. Modèle : message dans une communauté

> Bonjour à tous. Saviez-vous que le traitement recommandé en premier contre l'insomnie n'est pas un médicament mais une thérapie (TCC-I) ? Elle reste très difficile d'accès en France, alors j'ai développé une appli Android gratuite qui en reprend les étapes : Sommelle. Pas de compte, pas de pub, rien n'est envoyé, et le code est public. Elle ne remplace pas un médecin, mais si ça peut aider : https://relthdrarxash.github.io/Sommelle/. Les retours sont les bienvenus, surtout les critiques.

Règles : lire le règlement de la communauté avant de publier, répondre à chaque commentaire, ne jamais publier le même message partout le même jour.

---

## 7. Modèle : communiqué pour la Journée du sommeil

> **Insomnie : une appli française gratuite pour accéder au traitement recommandé**
>
> En France, l'insomnie chronique touche entre 15 et 20 % des adultes. Le traitement conseillé en premier, la thérapie cognitivo-comportementale de l'insomnie, reste difficile d'accès faute de praticiens formés, et les somnifères sont encore largement prescrits.
>
> Sommelle, une application Android gratuite et libre, reprend les étapes de cette thérapie : un agenda du sommeil rempli chaque matin, des horaires de sommeil ajustés chaque semaine, et un appui la nuit. Conçue pour être utilisable par tous, y compris les personnes âgées, elle ne collecte aucune donnée : tout reste sur le téléphone.
>
> Sommelle ne remplace pas un avis médical. Contact : [adresse]. Site : https://relthdrarxash.github.io/Sommelle/

Le chiffre de prévalence vient de Santé publique France (Bulletin épidémiologique hebdomadaire). Vérifier qu'il n'existe pas de chiffre plus récent avant envoi.

---

## 8. Mesurer sans trahir la promesse

L'appli ne contient aucun outil de mesure : c'est un engagement. Pour savoir si Sommelle aide :
- les statistiques agrégées de la Play Console (installations, désinstallations, notes) ;
- les avis sur les stores et les retours sur GitHub ;
- plus tard, un questionnaire volontaire proposé en fin de programme, rempli sur une page web, sans lien avec les données de l'appli.

---

## Supports prêts à l'emploi

| Support | Emplacement |
|---|---|
| Site, page professionnels, confidentialité | `site/`, publié sur GitHub Pages |
| Fiche à imprimer pour les prescripteurs | `site/fiche-prescripteurs.pdf` (modèle : `marketing/fiche-prescripteurs.html`) |
| Bannière (liens partagés et Google Play) | `site/img/banniere.png`, `fastlane/.../images/featureGraphic.png` |
| Icône Google Play | `fastlane/.../images/icon.png` |
| Textes et captures des stores | `fastlane/metadata/android/fr-FR/` |
