/**
 * Aplicação de Grafos de Alta Dimensionalidade — Rede Social
 * Trabalho Colaborativo II — Ciência da Computação (PUCPR)
 *
 * Autores:
 *   - Jafte Carneiro Fagundes da Silva (@cyberfika)
 *   - Nicolas Hrescak (@NicolasHrescak)
 *
 * Professor: Fabrício Enembreck
 */

package graph;

import graph.algorithm.GraphAlgorithms;
import graph.domain.SocialNetwork;
import graph.generator.GraphGenerator;
import graph.io.PajekIO;
import graph.model.Graph;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Ponto de entrada da aplicação — menu interativo via console.
 *
 * <p>Responsabilidade exclusiva: orquestrar a interação com o usuário.
 * Esta classe não contém algoritmos, lógica de domínio, geração de grafos
 * nem I/O de arquivos — tudo é delegado às classes especializadas dos
 * pacotes {@code graph.algorithm}, {@code graph.domain}, {@code graph.generator}
 * e {@code graph.io}.
 *
 * <p>O menu principal oferece três formas de carregar um grafo:
 * gerar uma rede social, gerar um grafo aleatório ou importar um arquivo Pajek.
 * Após o carregamento, um segundo menu expõe todas as operações disponíveis.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);

    /** Grafo atualmente carregado. {@code null} enquanto nenhum grafo foi carregado. */
    private static Graph graph          = null;

    /** Serviço de algoritmos vinculado ao grafo atual. Recriado a cada novo grafo. */
    private static GraphAlgorithms algs = null;

    /** Serviço de domínio da rede social vinculado ao grafo atual. Recriado a cada novo grafo. */
    private static SocialNetwork social = null;

    /** {@code true} se o grafo atual foi importado de arquivo (bloqueia re-exportação). */
    private static boolean wasLoaded    = false;

    public static void main(String[] args) {
        boolean useConsole = false;
        if (args.length == 0) {
            useConsole = Menu.chooseConsoleMode();
        } else {
            for (String arg : args) {
                if (arg.equalsIgnoreCase("--console")) {
                    useConsole = true;
                    break;
                }
            }
        }

        if (useConsole) {
            boolean running = true;
            while (running) {
                showMainMenu();
                String option = sc.nextLine().trim();
                switch (option) {
                    case "1" -> loadSocialNetwork();
                    case "2" -> generateRandom();
                    case "3" -> loadPajek();
                    case "0" -> running = false;
                    default  -> System.out.println("Invalid option.");
                }
                if (graph != null && !option.equals("0")) graphMenu();
            }
            System.out.println("\nGoodbye!");
            sc.close();
        } else {
            // Launch GUI on the Event Dispatch Thread
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
                new graph.gui.GraphGUI().setVisible(true);
            });
        }
    }

    // -------------------------------------------------------------------------
    // MENUS
    // -------------------------------------------------------------------------

    /** Exibe o menu principal com as opções de carregamento de grafo. */
    private static void showMainMenu() {
        System.out.println("\n+================================================+");
        System.out.println("|   High-Dimensional Graph Application           |");
        System.out.println("+================================================+");
        System.out.println("|  [1] Generate Social Network (5,000 people)    |");
        System.out.println("|  [2] Generate Random Graph                     |");
        System.out.println("|  [3] Load Graph (Pajek format)                 |");
        System.out.println("|  [0] Exit                                      |");
        System.out.println("+================================================+");
        System.out.print("Choice: ");
    }

    /**
     * Exibe o menu de operações sobre o grafo carregado e processa as escolhas
     * até que o usuário opte por voltar ao menu principal.
     */
    private static void graphMenu() {
        boolean active = true;
        while (active) {
            System.out.println("\n--- Graph loaded: " + graph.numVertices + " vertices ---");
            System.out.println(" [1] Check connectivity");
            System.out.println(" [2] Show components");
            System.out.println(" [3] Check Eulerian");
            System.out.println(" [4] Check cycle");
            System.out.println(" [5] Closeness Centrality");
            System.out.println(" [6] Betweenness Centrality");
            System.out.println(" [7] Follower Recommendation System");
            System.out.println(" [8] Print graph (first 20 vertices)");
            if (!wasLoaded) System.out.println(" [9] Export to Pajek");
            System.out.println(" [0] Back to main menu");
            System.out.print("Choice: ");
            switch (sc.nextLine().trim()) {
                case "1" -> checkConnectivity();
                case "2" -> showComponents();
                case "3" -> checkEulerian();
                case "4" -> checkCycle();
                case "5" -> calcCloseness();
                case "6" -> calcBetweenness();
                case "7" -> recommendFollower();
                case "8" -> printGraph(20);
                case "9" -> { if (!wasLoaded) exportPajek(); }
                case "0" -> active = false;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // AÇÕES DO MENU PRINCIPAL
    // -------------------------------------------------------------------------

    /** Gera a rede social com 5.000 pessoas a partir do arquivo {@code data/names.txt}. */
    private static void loadSocialNetwork() {
        System.out.println("\nLoading social network...");
        setGraph(GraphGenerator.generateSocialNetwork("names.txt"), false);
        if (graph != null) System.out.println("Social network ready!");
    }

    /** Solicita parâmetros ao usuário e gera um grafo aleatório. */
    private static void generateRandom() {
        try {
            System.out.print("Number of vertices: ");
            int n = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Number of edges: ");
            int m = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Should be connected? (y/n): ");
            boolean connected = sc.nextLine().trim().equalsIgnoreCase("y");
            setGraph(GraphGenerator.generateRandomGraph(n, m, connected), false);
            if (graph != null) System.out.println("Graph generated!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    /** Solicita o nome do arquivo e importa um grafo no formato Pajek. */
    private static void loadPajek() {
        System.out.print("File name in pajek/input/ (e.g. graph.txt): ");
        String filename = sc.nextLine().trim();
        setGraph(PajekIO.importFrom(filename), true);
        if (graph != null) System.out.println("Graph loaded!");
    }

    /**
     * Centraliza a atribuição de um novo grafo e recria os serviços dependentes.
     * Se {@code g} for {@code null} (erro de geração/importação), mantém o estado atual.
     *
     * @param g      novo grafo a usar (ignorado se {@code null})
     * @param loaded {@code true} se o grafo foi importado de arquivo
     */
    private static void setGraph(Graph g, boolean loaded) {
        if (g == null) return;
        graph     = g;
        algs      = new GraphAlgorithms(g);
        social    = new SocialNetwork(g);
        wasLoaded = loaded;
    }

    // -------------------------------------------------------------------------
    // AÇÕES DO MENU DO GRAFO
    // -------------------------------------------------------------------------

    /** Verifica e exibe se o grafo é conexo (fracamente conexo para direcionados). */
    private static void checkConnectivity() {
        boolean connected = algs.isConnected();
        System.out.println("\nConnected: " + (connected ? "Yes" : "No"));
        if (!connected) System.out.println("Use option [2] to view the components.");
    }

    /** Lista todos os componentes conexos do grafo, mostrando até 10 vértices por componente. */
    private static void showComponents() {
        List<List<Integer>> components = algs.getComponents();
        System.out.println("\n--- Components (" + components.size() + " total) ---");
        for (int i = 0; i < components.size(); i++) {
            List<Integer> c = components.get(i);
            System.out.print("Component " + (i + 1) + " (" + c.size() + " vertices): {");
            int show = Math.min(c.size(), 10);
            for (int j = 0; j < show; j++) {
                System.out.print(graph.getName(c.get(j)));
                if (j < show - 1) System.out.print(", ");
            }
            if (c.size() > 10) System.out.print(", ...");
            System.out.println("}");
        }
    }

    /** Verifica e exibe se o grafo é Euleriano, Semi-Euleriano ou nenhum dos dois. */
    private static void checkEulerian() {
        int r = algs.isEulerian();
        System.out.println("\nEulerian: " + switch (r) {
            case 2 -> "Yes — has Eulerian Cycle";
            case 1 -> "Semi-Eulerian — has Eulerian Path";
            default -> "No";
        });
    }

    /** Verifica e exibe se o grafo contém pelo menos um ciclo. */
    private static void checkCycle() {
        System.out.println("\nHas cycle: " + (algs.hasCycle() ? "Yes" : "No"));
    }

    /**
     * Calcula e exibe o Top 10 de Centralidade de Proximidade.
     * Para grafos com mais de 500 vértices, solicita confirmação antes de prosseguir
     * devido ao custo computacional O(V × (V + E) log V).
     */
    private static void calcCloseness() {
        System.out.println("\nCalculating Closeness Centrality...");
        if (graph.numVertices > 500) {
            System.out.print("Large graph (" + graph.numVertices + " vertices). Continue? (y/n): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("y")) return;
        }
        double[] c = algs.closenessCentrality();

        System.out.println("\n--- Top 10 Closeness Centrality ---");
        List<int[]> ranking = new ArrayList<>();
        for (int i = 0; i < graph.numVertices; i++) ranking.add(new int[]{i, 0});
        ranking.sort((a, b) -> Double.compare(c[b[0]], c[a[0]]));
        for (int i = 0; i < Math.min(10, ranking.size()); i++)
            System.out.printf("  %s: %.6f%n", graph.getName(ranking.get(i)[0]), c[ranking.get(i)[0]]);
    }

    /**
     * Calcula e exibe o Top 10 de Centralidade de Intermediação (Algoritmo de Brandes).
     * Para grafos com mais de 500 vértices, solicita confirmação antes de prosseguir
     * devido ao custo computacional O(V × (V + E) log V).
     */
    private static void calcBetweenness() {
        System.out.println("\nCalculating Betweenness Centrality (Brandes)...");
        if (graph.numVertices > 500) {
            System.out.print("Large graph (" + graph.numVertices + " vertices). This may take a while. Continue? (y/n): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("y")) return;
        }
        double[] b = algs.betweennessCentrality();
        List<int[]> ranking = new ArrayList<>();
        for (int i = 0; i < graph.numVertices; i++) ranking.add(new int[]{i, 0});
        ranking.sort((a, bv) -> Double.compare(b[bv[0]], b[a[0]]));

        System.out.println("\n--- Top 10 Betweenness Centrality ---");
        for (int i = 0; i < Math.min(10, ranking.size()); i++)
            System.out.printf("  %s: %.2f%n", graph.getName(ranking.get(i)[0]), b[ranking.get(i)[0]]);
    }

    /**
     * Solicita dois nomes ao usuário e exibe a recomendação de seguir,
     * com o caminho completo de conexões encontrado via DFS.
     */
    private static void recommendFollower() {
        System.out.print("Source person name: ");
        String source = sc.nextLine().trim();
        System.out.print("Target person name: ");
        String target = sc.nextLine().trim();
        System.out.println("\n" + social.recommendFollower(source, target));
    }

    /** Solicita o nome do arquivo e exporta o grafo atual no formato Pajek. */
    private static void exportPajek() {
        System.out.print("Output file name (e.g. network.txt): ");
        String filename = sc.nextLine().trim();
        PajekIO.export(graph, filename);
    }

    // -------------------------------------------------------------------------
    // EXIBIÇÃO DO GRAFO
    // -------------------------------------------------------------------------

    /**
     * Imprime os primeiros {@code limit} vértices do grafo no console,
     * mostrando até 5 vizinhos por vértice.
     *
     * @param limit número máximo de vértices a exibir
     */
    private static void printGraph(int limit) {
        int show = Math.min(graph.numVertices, limit);
        for (int i = 0; i < show; i++) {
            System.out.print("  [" + graph.getName(i) + "] ->");
            int count = 0;
            for (int[] e : graph.neighbors(i)) {
                System.out.print(" " + graph.getName(e[0]) + "(w=" + e[1] + ")");
                if (++count >= 5) { System.out.print(" ..."); break; }
            }
            System.out.println();
        }
        if (graph.numVertices > limit)
            System.out.println("  ... (" + (graph.numVertices - limit) + " vertices omitted)");
    }
}
