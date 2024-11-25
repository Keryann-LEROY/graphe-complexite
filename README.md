# Sudoku Solver and Minimal Grid Generator

Ce projet implémente une application permettant de générer une grille de Sudoku contenant le plus petit ensemble d'indices possible, tout en assurant une solution unique. Il utilise la bibliothèque **Choco Solver** pour résoudre le problème et deux méthodes distinctes d'exploration et d'optimisation.

## Contenu du projet

Le projet contient plusieurs fichiers et classes organisés dans les dossiers suivants : 

### Structure
- **Dossier `Methodes`** : 
  - Contient deux classes principales implémentant l'interface `OptimalSudokuSolver` :
    - `CompleteIcrementalSearch`
    - `CompleteReverseTreeExploration`
  - Ces classes définissent les stratégies pour trouver la grille minimale avec une solution unique.

- **Fichier `MinimalGridGenerator`** :
  - Contient la méthode `main`.
  - Permet d'exécuter le projet et de générer une grille minimale en utilisant une des deux stratégies.

- **Dossier `utils`** :
  - Contient des classes auxiliaires pour gérer les métadonnées du Sudoku et les propagateurs de contraintes.

## Environnement

- **IDE** : IntelliJ IDEA
- **Java Development Kit (JDK)** : Version 22
- **Framework** : [Choco Solver](https://choco-solver.org/) pour la modélisation et la résolution.

## Instructions d'installation et d'exécution

### Étape 1 : Préparer l'environnement
1. Assurez-vous d'avoir **IntelliJ IDEA** installé sur votre machine.
2. Installez le **JDK 22** et configurez IntelliJ pour utiliser cette version.
3. Téléchargez la bibliothèque **Choco Solver** depuis son site officiel ou ajoutez-la en tant que dépendance Maven/Gradle.

### Étape 2 : Cloner et importer le projet
1. Clonez ce dépôt dans votre environnement local :
   ```bash
   git clone <URL-du-repo>
2. Choisissez la méthode de résolution à utiliser en modifiant cette ligne :
    ```java
    OptimalSudokuSolver solver = new CompleteReverseTreeExploration(sudoku, desiredSolution);
    ```
    ou :
    ```java
    OptimalSudokuSolver solver = new CompleteIcrementalSearch(sudoku, desiredSolution);
    ```
3. Exécutez la méthode `main` pour générer la grille de Sudoku minimale et valider les solutions.

### Étape 4 : Résultat
L'application affiche :
- La solution initiale générée aléatoirement.
- La grille minimale avec le plus petit ensemble d'indices.
- La validation de l'unicité de la solution.

## Détails des méthodes

### CompleteIcrementalSearch
Cette méthode :
- Incrémente progressivement le nombre d'indices.
- Utilise un solveur pour valider l'unicité de la solution et trouve la configuration optimale.

### CompleteReverseTreeExploration
Cette méthode :
- Explore l'arbre des solutions dans l'ordre inverse.
- Maximise directement le nombre de cases vides tout en maintenant une solution unique.

## Exemple de sortie
```text
Initial Solution:
4 . 3 . 2 1
. 5 . 3 . 4
...
Has One Solution: true
Has Two Solutions: false
Elapsed Time: 1.234s
```

