# LabyrinthEscape

## Description

**LabyrinthEscape** est un projet visant à résoudre un labyrinthe où un prisonnier doit atteindre une sortie (**S**) avant d'être rattrapé par le feu (**F**). Les murs (**#**) bloquent aussi bien la progression du prisonnier que celle du feu.

Le projet implémente une interface graphique interactive ainsi qu'une résolution algorithmique combinant un **BFS multi-source** et un **A* modifié**.

---

## Fonctionnalités principales

1. **Lecture des labyrinthes** :
   - Lecture des grilles depuis un fichier `input.txt`.

2. **Interface graphique interactive** (Java Swing) :
   - Affiche la grille (murs, feu, départ, sortie).
   - Navigation entre plusieurs labyrinthes (« Précédent », « Suivant »).
   - Bouton « Résoudre » pour exécuter l'algorithme et visualiser le chemin.

3. **Algorithme de résolution** :
   - **BFS multi-source** pour calculer le temps d'arrivée du feu dans chaque case.
   - **A*** pour trouver un chemin sécurisé pour le prisonnier jusqu'à la sortie.
   - Visualisation du chemin trouvé en bleu.

---

## Structure du projet

### 1. **Lecture et interface graphique**
- **LabyrinthGUI** :
  - Lecture des labyrinthes depuis `input.txt`.
  - Navigation entre les labyrinthes.
  - Bouton « Résoudre » pour exécuter l'algorithme.
- **LabyrinthPanel** :
  - Affichage des éléments du labyrinthe avec code couleur :
    - Noir : murs (**#**)
    - Rouge : feu (**F**)
    - Cyan : départ (**D**)
    - Jaune : sortie (**S**)
    - Vert : vide
  - Visualisation du chemin en bleu.

### 2. **Algorithme de résolution**
- **computeFireTime** :
  - Implémente un BFS multi-source pour calculer les temps d'arrivée du feu.
- **aStarWithFire** :
  - Modifie l'algorithme A* pour inclure une contrainte liée au feu.
  - Évite les cases où le feu est présent ou imminent.

### 3. **Classe Node**
- Contient les coordonnées d'une case, les coûts `gCost` et `hCost`, et une référence au parent.
- Comparaison des noeuds par leur `fCost = gCost + hCost`.

---

## Algorithme A*

1. **Principe de l'A*** :
   - Heuristique : distance de Manhattan (∣x-xend∣ + ∣y-yend∣).
   - Optimalité garantie si l'heuristique ne surestime jamais la distance réelle.

2. **Modification avec le feu** :
   - Contraintes ajoutées pour éviter les cases atteintes par le feu.

3. **Complexité** :
   - **BFS du feu** : O(N x M)
   - **A*** : O((N x M) x log(N x M))

---

## Fichiers importants

- `input.txt` : fichier contenant les labyrinthes à résoudre.
- `LabyrinthGUI.java` : classe principale pour l'interface utilisateur.
- `LabyrinthEscape.java` : contient les algorithmes BFS et A*.
- `LabyrinthPanel.java` : gère l'affichage des labyrinthes.

---

## Instructions d'exécution

1. Compiler les fichiers sources :
   ```bash
   javac src/Labyrinthe/*.java
   ```

2. Exécuter le programme :
   ```bash
   java Labyrinthe.LabyrinthGUI
   ```

3. Fournir un fichier `input.txt` au même niveau que les fichiers compilés.

---

## Exemple d'input.txt

```
3
5 5
#####
#D  #
#  F#
#   #
#####

5 5
#####
#D F#
#  ##
#   S
#####
```

---

## Auteurs
- **Zineb Fennich**
