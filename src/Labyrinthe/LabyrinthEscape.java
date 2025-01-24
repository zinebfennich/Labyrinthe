/**
 * Package Labyrinthe : Implémente un algorithme pour résoudre un problème
 * de labyrinthe avec des contraintes de propagation de feu.
 */
package Labyrinthe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
/**
 * Représente un nœud (case) dans la grille utilisée pour le calcul du chemin.
 * Chaque nœud contient sa position, son coût, et une référence à son parent pour reconstruire le chemin.
 */
class Node implements Comparable<Node> {
    /** Coordonnée x de la case. */
    int x;

    /** Coordonnée y de la case. */
    int y;

    /** Coût du chemin depuis le point de départ. */
    int gCost;

    /** Estimation heuristique de la distance au point de sortie. */
    int hCost;

    /** Référence au parent de ce nœud, utilisée pour reconstruire le chemin. */
    Node parent;

    /**
     * Constructeur de Node.
     *
     * @param x       Coordonnée x de la case.
     * @param y       Coordonnée y de la case.
     * @param gCost   Coût parcouru depuis le départ.
     * @param hCost   Estimation heuristique de la distance au point de sortie.
     * @param parent  Référence au parent de ce nœud.
     */
    public Node(int x, int y, int gCost, int hCost, Node parent) {
        this.x = x;
        this.y = y;
        this.gCost = gCost;
        this.hCost = hCost;
        this.parent = parent;
    }

    /**
     * Calcule le coût total (fCost) de ce nœud.
     * fCost = gCost + hCost
     *
     * @return Coût total du nœud.
     */
    public int fCost() {
        return gCost + hCost;
    }

    /**
     * Compare deux nœuds en fonction de leur coût total (fCost).
     *
     * @param other L'autre nœud à comparer.
     * @return Résultat de la comparaison.
     */
    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.fCost(), other.fCost());
    }
    /**
     * Vérifie si deux nœuds sont égaux en comparant leurs coordonnées.
     *
     * @param o Objet à comparer.
     * @return true si les nœuds sont égaux, sinon false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node n = (Node) o;
        return this.x == n.x && this.y == n.y;
    }
    /**
     * Calcule le code de hachage pour le nœud.
     *
     * @return Code de hachage basé sur les coordonnées.
     */
    @Override
    public int hashCode() {
        // Combinaison simple des coordonnées
        return 31 * x + y;
    }
}
/**
 * Classe principale pour résoudre le problème du labyrinthe.
 * Lit un fichier contenant des labyrinthes, calcule le temps de propagation du feu
 * et détermine si le chemin est possible avant que le feu n'atteigne la sortie.
 */
public class LabyrinthEscape {

    /** Déplacements possibles : Haut, Droite, Bas, Gauche. */
    private static final int[] dx = {-1, 0, 1, 0};
    private static final int[] dy = {0, 1, 0, -1};

    /** Représente une valeur très grande pour l'initialisation. */
    private static final int INF = Integer.MAX_VALUE;
    /**
     * Méthode principale du programme.
     *
     * @param args Arguments de la ligne de commande.
     */
    public static void main(String[] args) {
        Scanner scanner = null;
        try {
            // Charger input.txt en tant que ressource depuis le JAR
            InputStream is = LabyrinthEscape.class.getResourceAsStream("/Labyrinthe/input.txt");
            if (is == null) {
                System.err.println("Resource /Labyrinthe/input.txt introuvable dans le JAR.");
                return;
            }
            scanner = new Scanner(is);

            int T = scanner.nextInt();    // Nombre de labyrinthes
            List<String> results = new ArrayList<>();

            for (int t = 0; t < T; t++) {
                int N = scanner.nextInt(); // Nombre de lignes
                int M = scanner.nextInt(); // Nombre de colonnes
                scanner.nextLine(); // Consomme la fin de ligne

                char[][] grid = new char[N][M];

                for (int i = 0; i < N; i++) {
                    if (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.length() < M) {
                            System.out.println("Ligne " + (i + 1) + " trop courte.");
                            return;
                        }
                        grid[i] = line.substring(0, M).toCharArray();
                    } else {
                        System.out.println("Fichier de carte incomplet.");
                        return;
                    }
                }

                results.add(canEscape(grid) ? "Y" : "N");
            }

            // Affiche tous les résultats
            for (String result : results) {
                System.out.println(result);
            }

        } catch (Exception e) { // Remplacez FileNotFoundException par Exception
            System.err.println("Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
    /**
     * Vérifie s'il est possible d'échapper au labyrinthe avant que le feu n'atteigne la sortie.
     *
     * @param grid Grille du labyrinthe.
     * @return true si une évasion est possible, sinon false.
     */
    static boolean canEscape(char[][] grid) {
        int[] start = findPosition(grid, 'D');
        int[] end = findPosition(grid, 'S');

        if (start == null || end == null) {
            // Si pas de D ou pas de S, échec direct
            return false;
        }

        int[][] fireTime = computeFireTime(grid);
        List<Node> path = aStarWithFire(grid, start, end, fireTime);

        return (path != null && !path.isEmpty());
    }

    /**
     * Calcule, via un BFS multi-source, le temps d'arrivée du feu dans chaque case.
     * @param grid Grille du labyrinthe.
     * @return Tableau des temps d'arrivée du feu pour chaque case.
     */
    static int[][] computeFireTime(char[][] grid) {
        int N = grid.length;
        int M = grid[0].length;

        int[][] fireTime = new int[N][M];
        for (int i = 0; i < N; i++) {
            Arrays.fill(fireTime[i], INF);
        }

        Queue<int[]> queue = new LinkedList<>();

        // Enfile toutes les positions 'F' (feu) avec temps = 0
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (grid[i][j] == 'F') {
                    fireTime[i][j] = 0;
                    queue.offer(new int[]{i, j});
                }
            }
        }

        // Parcours BFS pour calculer la propagation du feu
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];
            int t = fireTime[x][y];

            for (int k = 0; k < 4; k++) {
                int nx = x + dx[k];
                int ny = y + dy[k];

                if (isValid(nx, ny, grid)) {
                    if (fireTime[nx][ny] > t + 1) {
                        fireTime[nx][ny] = t + 1;
                        queue.offer(new int[]{nx, ny});
                    }
                }
            }
        }
        return fireTime;
    }

    /**
     * Implémente l'algorithme A* pour trouver un chemin sécurisé.
     *
     * @param grid     Grille du labyrinthe.
     * @param start    Position de départ.
     * @param end      Position de sortie.
     * @param fireTime Temps d'arrivée du feu sur chaque case.
     * @return Liste des nœuds du chemin trouvé, ou null si aucun chemin.
     */
    static List<Node> aStarWithFire(char[][] grid, int[] start, int[] end, int[][] fireTime) {


        int N = grid.length;
        int M = grid[0].length;

        // Pour garder en mémoire le meilleur gCost trouvé pour chaque (x,y)
        int[][] bestG = new int[N][M];
        for (int i = 0; i < N; i++) {
            Arrays.fill(bestG[i], INF);
        }

        // Création du noeud de départ
        Node startNode = new Node(start[0], start[1], 0,
                heuristic(start[0], start[1], end[0], end[1]), null);

        // Structure de priorité (ordre par fCost)
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        // Ensemble fermé pour stocker les (x,y) déjà explorés
        Set<String> closedSet = new HashSet<>();

        openSet.add(startNode);
        bestG[start[0]][start[1]] = 0;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            int x = current.x;
            int y = current.y;

            // Si déjà exploré, on skip
            if (closedSet.contains(x + "," + y)) {
                continue;
            }
            closedSet.add(x + "," + y);

            // Si on atteint S, on reconstruit le chemin
            if (x == end[0] && y == end[1]) {
                return reconstructPath(current);
            }

            // Exploration des voisins
            for (int k = 0; k < 4; k++) {
                int nx = x + dx[k];
                int ny = y + dy[k];

                if (!isValid(nx, ny, grid)) {
                    continue;
                }

                // On veut arriver à (nx,ny) à time = current.gCost + 1
                int tentativeG = current.gCost + 1;

                // Vérification feu : on ne peut entrer que si
                // tentativeG < fireTime[nx][ny]
                if (tentativeG >= fireTime[nx][ny]) {
                    // Le feu y arrive avant ou en même temps => pas possible
                    continue;
                }

                // Vérifie si c'est mieux que tout ce qu'on connaissait
                if (tentativeG < bestG[nx][ny]) {
                    bestG[nx][ny] = tentativeG;
                    int h = heuristic(nx, ny, end[0], end[1]);
                    Node neighbor = new Node(nx, ny, tentativeG, h, current);
                    openSet.add(neighbor);
                }
            }
        }
        // Impossible de trouver un chemin sans être rattrapé par le feu
        return null;
    }

    /**
     * Calcule l'heuristique Manhattan entre deux points.
     *
     * @param x1 Coordonnée x du premier point.
     * @param y1 Coordonnée y du premier point.
     * @param x2 Coordonnée x du second point.
     * @param y2 Coordonnée y du second point.
     * @return Distance Manhattan entre les deux points.
     */
    private static int heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /**
     * Vérifie si une case est valide pour un déplacement.
     *
     * @param x    Coordonnée x de la case.
     * @param y    Coordonnée y de la case.
     * @param grid Grille du labyrinthe.
     * @return true si la case est valide, sinon false.
     */
    private static boolean isValid(int x, int y, char[][] grid) {
        if (x < 0 || x >= grid.length || y < 0 || y >= grid[0].length) {
            return false;
        }
        return (grid[x][y] != '#');
    }

    /**
     * Retrouve la position (x, y) d'un caractère donné dans la grille.
     *
     * @param grid   Grille du labyrinthe.
     * @param target Caractère à rechercher ('D', 'S', etc.).
     * @return Tableau contenant les coordonnées [x, y], ou null si introuvable.
     */
    static int[] findPosition(char[][] grid, char target) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    /**
     * Construit la liste du chemin en remontant via les parents des nœuds.
     *
     * @param current Dernier nœud du chemin.
     * @return Liste des nœuds constituant le chemin, de départ à arrivée.
     */
    private static List<Node> reconstructPath(Node current) {
        List<Node> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
