# Sudoku Solver and Minimal Grid Generator

Ce projet implémente une application permettant de générer une grille de Sudoku contenant le plus petit ensemble d'indices possible, tout en assurant une solution unique. Il utilise la bibliothèque **Choco Solver** pour résoudre le problème et deux méthodes distinctes d'exploration et d'optimisation.

## Contenu du projet

Le projet contient plusieurs fichiers et classes organisés : 

### Structure
- **Package `Methodes`** : 
  - Contient deux classes principales implémentant l'interface `OptimalSudokuSolver` :
    - `CompleteIcrementalSearch`
    - `CompleteReverseTreeExploration`
    - `IncompleteHeuristicDrivenRandomSearch`
    - `LargeNeighborhoodSearch`
  - Ces classes définissent les stratégies pour trouver la grille minimale avec une solution unique.

- **`MinimalGridGenerator`** :
  - Contient la méthode `main`.
  - Permet d'exécuter le projet et de générer une grille minimale en utilisant une des stratégies.

- **`SodukuBenchmark`** :
  - Cette classe génère les fichiers CSV des résultats sur différentes approches, on a utiliser Tableau [Tableau](https://public.tableau.com/app/discover) pour la visualisation
    de ces fichiers CSV.

- **Package `utils`** :
  - Contient des classes auxiliaires pour gérer les métadonnées du Sudoku et les propagateurs de contraintes.

## Environnement

- **IDE** : IntelliJ IDEA
- **Java Development Kit (JDK)** : Version 22
- **Framework** : [Choco Solver](https://choco-solver.org/) pour la modélisation et la résolution.

