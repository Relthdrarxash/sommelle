# Sommelle

Application Android d'accompagnement à l'insomnie fondée sur la TCC-I (thérapie cognitivo-comportementale de l'insomnie). Gratuite, en français, sans compte, et 100 % locale : aucune donnée ne quitte le téléphone.

Sommelle est un outil d'accompagnement et d'éducation. Ce n'est pas un dispositif médical et il ne remplace pas un avis médical.

## État

| Module | Contenu | État |
|---|---|---|
| `core:model` | Agenda du sommeil (Consensus Sleep Diary), questionnaire d'entrée, fenêtre de sommeil | Fait |
| `core:protocol` | Métriques, validation de l'agenda, questionnaire de sécurité, restriction du temps au lit, plan du programme | Fait, 37 tests |
| Modules Android | Interface, stockage, rappels | À faire |

Les deux modules `core` sont en pur Kotlin, sans dépendance Android : les règles cliniques sont lisibles et testables isolément. Toutes les valeurs cliniques sont dans `ProtocolConfig`, avec un numéro de version.

## Lancer les tests

```bash
./gradlew :core:protocol:test
```

## Documentation

- [DESIGN.md](DESIGN.md) : principes d'interface, pensés pour tous les publics et pour des personnes fatiguées.
- [ARCHITECTURE.md](ARCHITECTURE.md) : socle scientifique et sources, règles de sécurité, typologie des utilisateurs, parcours, architecture, accessibilité, périmètre du MVP et points à faire valider par un professionnel du sommeil.

## En cas de détresse

Si vous traversez une période difficile, le 3114 (numéro national de prévention du suicide) est joignable gratuitement, 24 h/24 et 7 j/7.
