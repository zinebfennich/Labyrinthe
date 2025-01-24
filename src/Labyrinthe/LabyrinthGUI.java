package Labyrinthe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interface graphique qui lit T labyrinthes depuis input.txt,
 * puis permet de les afficher un par un et de résoudre chacun avec LabyrinthEscape.
 */
public class LabyrinthGUI extends JFrame {

    private List<char[][]> labyrinths;  // liste des labyrinthes lus
    private int currentIndex;           // indice du labyrinthe en cours d'affichage

    private LabyrinthPanel labyrinthPanel;  // panel pour dessiner la grille + chemin
    private JLabel infoLabel;               // label pour infos : "Labyrinthe 1/3", etc.
    private JButton prevButton, nextButton, solveButton;

    public LabyrinthGUI() {
        super("Labyrinth Escape GUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        labyrinths = new ArrayList<>();
        currentIndex = 0;

        // Panel d'affichage graphique
        labyrinthPanel = new LabyrinthPanel();
        add(labyrinthPanel, BorderLayout.CENTER);

        // Label d'info en haut
        infoLabel = new JLabel("Aucun labyrinthe charge.");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);

        // Barre de boutons en bas
        JPanel buttonPanel = new JPanel();
        prevButton  = new JButton("<< Precedent");
        solveButton = new JButton("Resoudre");
        nextButton  = new JButton("Suivant >>");

        buttonPanel.add(prevButton);
        buttonPanel.add(solveButton);
        buttonPanel.add(nextButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        prevButton.addActionListener(e -> showPreviousLabyrinth());
        nextButton.addActionListener(e -> showNextLabyrinth());
        solveButton.addActionListener(e -> solveCurrentLabyrinth());

        // On charge tout de suite input.txt en tant que ressource
        loadLabyrinths();
    }

    /**
     * Charge T labyrinthes depuis le fichier input.txt inclus dans le JAR.
     */
    private void loadLabyrinths() {
        labyrinths.clear();
        currentIndex = 0;

        // Charger input.txt en tant que ressource depuis le JAR
        InputStream is = LabyrinthGUI.class.getResourceAsStream("/Labyrinthe/input.txt");
        if (is == null) {
            infoLabel.setText("Resource /Labyrinthe/input.txt introuvable dans le JAR.");
            return;
        }

        try (Scanner sc = new Scanner(is)) {
            if (!sc.hasNextInt()) {
                infoLabel.setText("Format du fichier incorrect : nombre de labyrinthes manquant.");
                return;
            }
            int T = sc.nextInt();  // nombre de labyrinthes
            for (int i = 0; i < T; i++) {
                if (!sc.hasNextInt()) {
                    infoLabel.setText("Format du fichier incorrect : nombre de lignes manquant pour le labyrinthe " + (i + 1));
                    return;
                }
                int N = sc.nextInt(); // Nombre de lignes
                if (!sc.hasNextInt()) {
                    infoLabel.setText("Format du fichier incorrect : nombre de colonnes manquant pour le labyrinthe " + (i + 1));
                    return;
                }
                int M = sc.nextInt(); // Nombre de colonnes
                sc.nextLine(); // consomme la fin de ligne

                char[][] grid = new char[N][M];
                for (int r = 0; r < N; r++) {
                    if (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        if (line.length() < M) {
                            infoLabel.setText("Ligne " + (r + 1) + " trop courte dans le labyrinthe " + (i + 1));
                            return;
                        }
                        grid[r] = line.substring(0, M).toCharArray();
                    } else {
                        infoLabel.setText("Fichier de carte incomplet pour le labyrinthe " + (i + 1));
                        return;
                    }
                }
                labyrinths.add(grid);
            }
        } catch (Exception e) {
            infoLabel.setText("Erreur lors du chargement des labyrinthes : " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (!labyrinths.isEmpty()) {
            infoLabel.setText("Labyrinthes chargés : " + labyrinths.size() + " (affiche le #1)");
            labyrinthPanel.setGrid(labyrinths.get(0));
        } else {
            infoLabel.setText("Aucun labyrinthe dans le fichier.");
        }
    }

    /**
     * Affiche le labyrinthe d'indice index (si valide).
     */
    private void showLabyrinth(int index) {
        if (index < 0 || index >= labyrinths.size()) return;
        currentIndex = index;
        labyrinthPanel.setGrid(labyrinths.get(currentIndex));
        infoLabel.setText("Labyrinthes : " + labyrinths.size()
                + "  (Labyrinthe #" + (currentIndex+1) + ")");
    }

    private void showPreviousLabyrinth() {
        if (labyrinths.isEmpty()) return;
        int newIndex = currentIndex - 1;
        if (newIndex < 0) {
            newIndex = labyrinths.size() - 1; // boucle : dernier
        }
        showLabyrinth(newIndex);
    }

    private void showNextLabyrinth() {
        if (labyrinths.isEmpty()) return;
        int newIndex = currentIndex + 1;
        if (newIndex >= labyrinths.size()) {
            newIndex = 0; // boucle : premier
        }
        showLabyrinth(newIndex);
    }

    /**
     * Appelle l'algorithme LabyrinthEscape pour résoudre le labyrinthe courant,
     * et affiche le chemin en bleu (si trouvé).
     */
    private void solveCurrentLabyrinth() {
        if (labyrinths.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Aucun labyrinthe charge.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Récupère la grille
        char[][] grid = labyrinths.get(currentIndex);

        // 1) Trouve start/end
        int[] start = LabyrinthEscape.findPosition(grid, 'D');
        int[] end   = LabyrinthEscape.findPosition(grid, 'S');
        if (start == null || end == null) {
            JOptionPane.showMessageDialog(this,
                    "Pas de D ou de S dans ce labyrinthe.",
                    "Resultat",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2) Calcul du temps de feu
        int[][] fireTime = LabyrinthEscape.computeFireTime(grid);

        // 3) Appel A*
        List<Node> path = LabyrinthEscape.aStarWithFire(grid, start, end, fireTime);
        boolean canEscape = (path != null && !path.isEmpty());

        // 4) Affiche le résultat
        if (canEscape) {
            labyrinthPanel.setPath(path); // dessine le chemin en bleu
            JOptionPane.showMessageDialog(this,
                    "Le prisonnier peut s'echapper (Y).\nChemin trouve : " + path.size() + " cases.",
                    "Resultat", JOptionPane.INFORMATION_MESSAGE);
        } else {
            labyrinthPanel.setPath(null);
            JOptionPane.showMessageDialog(this,
                    "Le prisonnier ne peut pas s'echapper (N).",
                    "Resultat", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LabyrinthGUI gui = new LabyrinthGUI();
            gui.setVisible(true);
        });
    }
}
