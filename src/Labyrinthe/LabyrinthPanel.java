package Labyrinthe;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Un panel qui dessine un labyrinthe sous forme de grille colorée
 * et qui affiche un chemin en bleu, ainsi qu'une légende.
 */
public class LabyrinthPanel extends JPanel {

    private char[][] grid;         // Le labyrinthe à dessiner
    private Set<Point> pathCells;  // Les cases du chemin (Node.x, Node.y) convertis en Point(col, row)

    private int cellSize = 30;     // Taille de chaque case (en pixels)
    private int margin   = 10;     // Marge autour

    public LabyrinthPanel() {
        setBackground(Color.WHITE);
        pathCells = new HashSet<>();
    }

    /**
     * Définit la grille à afficher (mur, feu, départ...).
     * @param newGrid le tableau 2D de caractères
     */
    public void setGrid(char[][] newGrid) {
        this.grid = newGrid;
        // On efface l'ancien chemin si on recharge un nouveau labyrinthe
        this.pathCells.clear();
        repaint();
    }

    /**
     * Convertit la liste de Node (chemin A*) en un ensemble de Points pour l'affichage.
     * Chaque Node a (x=ligne, y=colonne).
     */
    public void setPath(List<Node> path) {
        this.pathCells.clear();
        if (path != null) {
            for (Node n : path) {
                //  Node.x = ligne, Node.y = colonne
                // Pour dessiner, on fait "colonne = n.y, ligne = n.x"
                this.pathCells.add(new Point(n.y, n.x));
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (grid == null) {
            g.drawString("Aucun labyrinthe à afficher.", 10, 20);
            return;
        }

        int rows = grid.length;
        int cols = grid[0].length;

        // Dessin d'un rectangle de fond
        int totalWidth  = cols * cellSize;
        int totalHeight = rows * cellSize;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(margin, margin, totalWidth, totalHeight);

        // 1) Dessiner chaque case
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char ch = grid[r][c];

                // Couleur par défaut = vert (vide)
                Color color = Color.GREEN;

                switch (ch) {
                    case '#':
                        color = Color.BLACK;   // mur
                        break;
                    case 'F':
                        color = Color.RED;     // feu
                        break;
                    case 'D':
                        color = Color.CYAN;    // départ
                        break;
                    case 'S':
                        color = Color.YELLOW;  // sortie
                        break;
                    case '.':
                        color = Color.GREEN;   // vide
                        break;
                    default:
                        color = Color.GREEN;   // par sécurité
                        break;
                }

                int xPix = margin + c * cellSize;
                int yPix = margin + r * cellSize;

                g.setColor(color);
                g.fillRect(xPix, yPix, cellSize, cellSize);

                // Trait de grille
                g.setColor(Color.BLACK);
                g.drawRect(xPix, yPix, cellSize, cellSize);
            }
        }

        // 2) Dessiner le chemin par-dessus (en bleu)
        for (Point p : pathCells) {
            // p.x = colonne, p.y = ligne
            int xPix = margin + p.x * cellSize;
            int yPix = margin + p.y * cellSize;

            g.setColor(Color.BLUE);
            g.fillRect(xPix, yPix, cellSize, cellSize);

            g.setColor(Color.BLACK);
            g.drawRect(xPix, yPix, cellSize, cellSize);
        }

        // 3) Dessiner la légende dans le coin à droite
        drawLegend(g, margin + totalWidth + 20, margin);
    }

    /**
     * Affiche la légende (couleurs -> signification)
     */
    private void drawLegend(Graphics g, int startX, int startY) {
        String[] labels = {
                "Mur (#)   = Noir",
                "Vide (.)  = Vert",
                "Feu (F)   = Rouge",
                "Départ (D)= Cyan",
                "Sortie (S)= Jaune",
                "Chemin    = Bleu"
        };
        Color[] colors = {
                Color.BLACK,
                Color.GREEN,
                Color.RED,
                Color.CYAN,
                Color.YELLOW,
                Color.BLUE
        };

        g.setColor(Color.BLACK);
        g.drawString("Légende :", startX, startY);
        int y = startY + 15;
        for (int i = 0; i < labels.length; i++) {
            // Petit carré de couleur
            g.setColor(colors[i]);
            g.fillRect(startX, y - 10, 10, 10);
            g.setColor(Color.BLACK);
            g.drawRect(startX, y - 10, 10, 10);

            // Texte
            g.drawString(labels[i], startX + 15, y);
            y += 15;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        // On adapte la taille au labyrinthe si on veut
        if (grid == null) {
            return new Dimension(400, 300);
        }
        int rows = grid.length;
        int cols = grid[0].length;
        return new Dimension(cols * cellSize + margin * 2 + 120, rows * cellSize + margin * 2);
    }
}
