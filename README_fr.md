# basic-oai-harvester

Il s'agit d'un outil simple en ligne de commande pour moissonner des métadonnées en utilisant le protocole OAI-PMH. Il
offre trois fonctions principales :

1. **Démarrer un nouveau moissonnage :** Initier un nouveau moissonnage de métadonnées à partir d'une source de données
   OAI-PMH.
2. **Reprendre un moissonnage interrompu :** Poursuivre un moissonnage qui a été interrompue.
3. **Mettre à jour un moissonnage complet :** Mettre à jour un moissonnage précédemment terminé pour récupérer toutes
   les métadonnées nouvelles ou modifiées.

## Table des matières

- [Installation](#installation)
- [Utilisation](#utilisation)
- [Contributions](#contributions)
- [Licence](#licence)
- [Remerciements](#remerciements)
- [Contact](#contact)

## Installation

Pour commencer :

1. Téléchargez le fichier zip correspondant à votre système d'exploitation depuis la page de release du dépôt
   GitHub (`ubuntu` devrait fonctionner sur la plupart des distributions Linux).
2. Décompressez le fichier et placez le répertoire `basic-oai-harvester` l'emplacement de votre choix.

## Utilisation

Pour des informations générales sur le programme, exécutez (utilisez `basic-oai-harvester.exe` à la place sur Windows) :

```bash
basic-oai-harvester
```

Pour obtenir de l'aide sur une commande spécifique, exécutez :

```bash
basic-oai-harvester help harvest
# ou
basic-oai-harvester help resume
# ou
basic-oai-harvester help update
```

Les trois principales commandes sont :

```bash
# Démarrer un nouveau moissonnage
basic-oai-harvester harvest [url OAI-PMH] [options]

# Reprendre un moissonnage interrompu
basic-oai-harvester resume [options]

# Mettre à jour un moissonnage complet
basic-oai-harvester update [options]
```

Par exemple, pour démarrer un nouveau moissonnage complet à l'url
OAI-PMH `https://oai.example.org/oai` et enregistrer les résultats dans un répertoire nommé `results` sous le répertoire
actuel, exécutez :

```bash
basic-oai-harvester harvest https://oai.example.org/oai -d results
```

## Contributions

Si vous avez des suggestions d'améliorations ou souhaitez signaler un problème, veuillez créer un ticket.

Si vous souhaitez contribuer à une fonctionnalité ou des améliorations, commencez par reconsidérer le nom du dépôt,
puis, si vous pensez toujours que cela a du sens, veuillez créer un ticket pour d'abord en discuter.

## Licence

Ce projet est sous licence MIT - consultez le fichier [LICENSE](LICENSE) pour plus de détails.

## Remerciements

Ce projet est écrit en Java, utilise Maven et dépend de diverses bibliothèques tierces et autres dépendances. Consultez
le fichier [pom.xml](pom.xml) pour une liste de ces dépendances.

## Contact

Pour des questions, des rapports d'erreurs ou des contributions, veuillez créer un ticket.
