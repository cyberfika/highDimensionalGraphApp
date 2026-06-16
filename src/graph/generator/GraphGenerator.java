package graph.generator;

import graph.io.NamesLoader;
import graph.model.Graph;

import java.util.*;

/**
 * Fábrica de instâncias de {@link Graph}: grafo aleatório e rede social.
 *
 * <p>Responsabilidade exclusiva: construir grafos a partir de parâmetros ou dados externos —
 * sem algoritmos de análise, sem I/O de Pajek e sem lógica de domínio da rede social.
 *
 * <p>Dois tipos de grafo são suportados:
 * <ul>
 *   <li><b>Grafo aleatório</b> — não-direcionado, ponderado, opcionalmente conexo.
 *       Conectividade garantida via spanning tree aleatória (algoritmo de Prüfer simplificado).</li>
 *   <li><b>Rede social</b> — direcionado, com nomes reais lidos de arquivo,
 *       cada vértice seguindo entre 5 e 8 outros (garante ≥ 25.000 arestas para 5.000 nós).</li>
 * </ul>
 */
public class GraphGenerator {

    private static final Random RNG = new Random();

    /** Construtor privado — classe utilitária, não deve ser instanciada. */
    private GraphGenerator() {}

    // -------------------------------------------------------------------------
    // GERADOR DE GRAFO ALEATÓRIO
    // -------------------------------------------------------------------------

    /**
     * Gera um grafo aleatório não-direcionado ponderado (pesos entre 1 e 100).
     *
     * <p>Se {@code connected} for {@code true}, garante conectividade construindo
     * primeiro uma spanning tree aleatória e depois adicionando as arestas restantes.
     *
     * <p>Se {@code connected} for {@code false}, isola o último vértice para garantir
     * que o grafo seja desconexo (quando o número de arestas o permite).
     *
     * @param n          número de vértices
     * @param m          número de arestas desejado
     * @param connected  {@code true} para garantir conectividade
     * @return grafo gerado, ou {@code null} se os parâmetros forem inválidos
     */
    public static Graph generateRandomGraph(int n, int m, boolean connected) {
        long maxEdges = (long) n * (n - 1) / 2;
        if (m > maxEdges) {
            System.out.printf("Error: maximum edges for %d vertices is %d.%n", n, maxEdges);
            return null;
        }
        if (connected && m < n - 1) {
            System.out.printf("Error: at least %d edges are required for a connected graph.%n", n - 1);
            return null;
        }

        Graph g = new Graph(n, false);
        for (int i = 0; i < n; i++) g.setName(i, "V" + i);

        Set<String> existing = new HashSet<>();
        int created = 0;

        // Garante conectividade via spanning tree aleatória
        int vertexLimit = n;
        if (connected) {
            System.out.println("Building random spanning tree...");
            List<Integer> visited = new ArrayList<>();
            List<Integer> unvisited = new ArrayList<>();
            for (int i = 0; i < n; i++) unvisited.add(i);

            int start = unvisited.remove(RNG.nextInt(unvisited.size()));
            visited.add(start);

            while (!unvisited.isEmpty()) {
                int next   = unvisited.remove(RNG.nextInt(unvisited.size()));
                int from   = visited.get(RNG.nextInt(visited.size()));
                int weight = RNG.nextInt(100) + 1;
                g.addEdge(from, next, weight);
                existing.add(edgeKey(from, next));
                visited.add(next);
                created++;
            }
        } else if (n > 1) {
            // Isola o último vértice para garantir desconexão
            long maxDisconnected = (long)(n - 1) * (n - 2) / 2;
            if (m <= maxDisconnected) {
                vertexLimit = n - 1;
                System.out.println("Isolating vertex V" + (n - 1) + " to guarantee disconnection.");
            } else {
                System.out.println("Warning: with " + m + " edges the graph will necessarily be connected.");
            }
        }

        // Arestas restantes aleatórias
        long maxInLimit = (long) vertexLimit * (vertexLimit - 1) / 2;
        while (created < m) {
            int u = RNG.nextInt(vertexLimit);
            int v = RNG.nextInt(vertexLimit);
            if (u == v) continue;
            String k = edgeKey(u, v);
            if (!existing.contains(k)) {
                int weight = RNG.nextInt(100) + 1;
                g.addEdge(u, v, weight);
                existing.add(k);
                created++;
            }
            if (existing.size() >= maxInLimit) break;
        }

        return g;
    }

    // -------------------------------------------------------------------------
    // GERADOR DE REDE SOCIAL
    // -------------------------------------------------------------------------

    /**
     * Gera uma rede social direcionada a partir de um arquivo de nomes.
     * Cada pessoa segue entre 5 e 8 outras escolhidas aleatoriamente,
     * resultando em média ≥ 32.500 arestas para 5.000 nós.
     * Todas as arestas têm peso 1 (cada "follow" tem o mesmo peso).
     *
     * @param namesFile nome do arquivo de nomes (buscado em {@code data/})
     * @return grafo direcionado da rede social, ou {@code null} se o arquivo não for encontrado
     */
    public static Graph generateSocialNetwork(String namesFile) {
        List<String> names = NamesLoader.load(namesFile);
        if (names == null || names.isEmpty()) return null;

        int n = names.size();
        Graph g = new Graph(n, true); // direcionado
        for (int i = 0; i < n; i++) g.setName(i, names.get(i));

        // Cada pessoa segue 5 a 8 outras → média >= 32.500 arestas com 5.000 nós
        for (int i = 0; i < n; i++) {
            int followCount = RNG.nextInt(4) + 5; // [5, 8]
            Set<Integer> followed = new HashSet<>();
            while (followed.size() < followCount) {
                int target = RNG.nextInt(n);
                if (target != i && followed.add(target))
                    g.addEdge(i, target, 1);
            }
        }

        long totalEdges = 0;
        for (int i = 0; i < n; i++) totalEdges += g.neighbors(i).size();
        System.out.printf("Social network generated: %d people, %d connections.%n", n, totalEdges);
        return g;
    }

    // -------------------------------------------------------------------------
    // AUXILIAR
    // -------------------------------------------------------------------------

    /**
     * Gera uma chave canônica para uma aresta não-direcionada {@code (u, v)}.
     * A chave é idêntica para {@code (u,v)} e {@code (v,u)}, evitando arestas duplicadas.
     *
     * @param u primeiro vértice
     * @param v segundo vértice
     * @return string no formato {@code "min-max"}
     */
    private static String edgeKey(int u, int v) {
        return Math.min(u, v) + "-" + Math.max(u, v);
    }
}
