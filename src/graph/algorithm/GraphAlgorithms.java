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

package graph.algorithm;

import graph.model.Graph;

import java.util.*;

/**
 * Algoritmos clássicos de grafos aplicados sobre uma instância de {@link Graph}.
 *
 * <p>Responsabilidade exclusiva: executar algoritmos sobre a estrutura do grafo —
 * sem I/O, sem geração de grafos e sem lógica de domínio específica da aplicação.
 *
 * <p>Algoritmos disponíveis:
 * <ul>
 *   <li><b>Conectividade</b> — BFS no grafo subjacente não-direcionado (conectividade fraca)</li>
 *   <li><b>Componentes</b> — componentes fracamente conectados via BFS</li>
 *   <li><b>Euleriano</b> — verificação por contagem de graus (in/out para direcionados)</li>
 *   <li><b>Ciclo</b> — DFS com coloração branco/cinza/preto para direcionados;
 *       rastreamento de pai para não-direcionados</li>
 *   <li><b>Centralidade de Proximidade</b> — Dijkstra a partir de cada vértice</li>
 *   <li><b>Centralidade de Intermediação</b> — Algoritmo de Brandes com Dijkstra</li>
 *   <li><b>Dijkstra</b> — caminho mínimo de fonte única com heap binário</li>
 * </ul>
 */
public class GraphAlgorithms {

    private final Graph graph;

    /**
     * Cria um serviço de algoritmos vinculado ao grafo fornecido.
     * Todos os métodos operam sobre este grafo.
     *
     * @param graph grafo sobre o qual os algoritmos serão executados
     */
    public GraphAlgorithms(Graph graph) {
        this.graph = graph;
    }

    // -------------------------------------------------------------------------
    // 1. CONECTIVIDADE
    // -------------------------------------------------------------------------

    /**
     * Verifica se o grafo é conexo.
     * Para grafos direcionados, usa conectividade fraca (ignora direção das arestas).
     *
     * @return {@code true} se o grafo for conexo (ou fracamente conexo)
     */
    public boolean isConnected() {
        if (graph.numVertices <= 1) return true;
        boolean[] visited = new boolean[graph.numVertices];
        bfsUndirected(visited);
        for (boolean v : visited) if (!v) return false;
        return true;
    }

    // -------------------------------------------------------------------------
    // 2. COMPONENTES (fracamente conectados para direcionados)
    // -------------------------------------------------------------------------

    /**
     * Identifica todos os componentes conexos do grafo.
     * Para grafos direcionados, retorna os componentes fracamente conectados
     * (trata todas as arestas como não-direcionadas).
     *
     * @return lista de componentes; cada componente é uma lista de índices de vértices
     */
    public List<List<Integer>> getComponents() {
        boolean[] visited = new boolean[graph.numVertices];
        List<List<Integer>> components = new ArrayList<>();
        for (int i = 0; i < graph.numVertices; i++) {
            if (!visited[i]) {
                List<Integer> component = new ArrayList<>();
                Queue<Integer> q = new LinkedList<>();
                visited[i] = true;
                q.add(i);
                while (!q.isEmpty()) {
                    int u = q.poll();
                    component.add(u);
                    for (int v : undirectedNeighbors(u))
                        if (!visited[v]) { visited[v] = true; q.add(v); }
                }
                components.add(component);
            }
        }
        return components;
    }

    // -------------------------------------------------------------------------
    // 3. EULERIANO
    // -------------------------------------------------------------------------

    /**
     * Determina se o grafo é Euleriano, Semi-Euleriano ou nenhum dos dois.
     *
     * <p>Para grafos <b>não-direcionados</b>: conta vértices com grau ímpar.
     * Zero vértices ímpares → Ciclo Euleriano; dois vértices ímpares → Caminho Euleriano.
     *
     * <p>Para grafos <b>direcionados</b>: compara graus de entrada e saída.
     * Todos iguais → Ciclo Euleriano; exatamente um vértice com saída−entrada=1
     * e um com entrada−saída=1 → Caminho Euleriano.
     *
     * @return {@code 2} = Euleriano (Ciclo) | {@code 1} = Semi-Euleriano (Caminho) | {@code 0} = Não Euleriano
     */
    public int isEulerian() {
        if (!isConnected()) return 0;

        if (!graph.directed) {
            int oddCount = 0;
            for (int i = 0; i < graph.numVertices; i++)
                if (graph.neighbors(i).size() % 2 != 0) oddCount++;
            if (oddCount == 0) return 2;
            if (oddCount == 2) return 1;
            return 0;
        } else {
            int[] in = new int[graph.numVertices], out = new int[graph.numVertices];
            for (int u = 0; u < graph.numVertices; u++) {
                out[u] = graph.neighbors(u).size();
                for (int[] e : graph.neighbors(u)) in[e[0]]++;
            }
            int outMore = 0, inMore = 0, equal = 0;
            for (int i = 0; i < graph.numVertices; i++) {
                if (out[i] == in[i]) equal++;
                else if (out[i] - in[i] == 1) outMore++;
                else if (in[i] - out[i] == 1) inMore++;
            }
            if (equal == graph.numVertices) return 2;
            if (outMore == 1 && inMore == 1 && equal == graph.numVertices - 2) return 1;
            return 0;
        }
    }

    // -------------------------------------------------------------------------
    // 4. CICLO
    // -------------------------------------------------------------------------

    /**
     * Verifica se o grafo contém pelo menos um ciclo.
     *
     * <p>Para grafos <b>não-direcionados</b>: DFS com rastreamento do vértice pai.
     * Uma aresta de retorno para um vértice que não é o pai indica ciclo.
     *
     * <p>Para grafos <b>direcionados</b>: DFS com coloração de três cores.
     * Uma aresta para um vértice cinza (ainda na pilha de chamadas) indica ciclo.
     *
     * @return {@code true} se o grafo contiver ciclo
     */
    public boolean hasCycle() {
        if (!graph.directed) {
            boolean[] visited = new boolean[graph.numVertices];
            for (int i = 0; i < graph.numVertices; i++)
                if (!visited[i] && dfsCycleUndirected(i, -1, visited)) return true;
        } else {
            // Coloração DFS: 0=branco, 1=cinza (em pilha), 2=preto (concluído)
            int[] color = new int[graph.numVertices];
            for (int i = 0; i < graph.numVertices; i++)
                if (color[i] == 0 && dfsCycleDirected(i, color)) return true;
        }
        return false;
    }

    /**
     * DFS auxiliar para detecção de ciclo em grafos não-direcionados.
     * Usa rastreamento do pai para distinguir arestas de retorno de arestas da árvore.
     *
     * @param v       vértice atual
     * @param parent  vértice pai na árvore DFS ({@code -1} para a raiz)
     * @param visited vetor de visitados
     * @return {@code true} se detectar ciclo a partir de {@code v}
     */
    private boolean dfsCycleUndirected(int v, int parent, boolean[] visited) {
        visited[v] = true;
        for (int[] e : graph.neighbors(v)) {
            int w = e[0];
            if (!visited[w]) { if (dfsCycleUndirected(w, v, visited)) return true; }
            else if (w != parent) return true;
        }
        return false;
    }

    /**
     * DFS auxiliar para detecção de ciclo em grafos direcionados usando coloração.
     * Cores: 0 = branco (não visitado), 1 = cinza (em processamento), 2 = preto (concluído).
     * Uma aresta para um vértice cinza indica back-edge, portanto ciclo.
     *
     * @param v     vértice atual
     * @param color vetor de cores de cada vértice
     * @return {@code true} se detectar ciclo a partir de {@code v}
     */
    private boolean dfsCycleDirected(int v, int[] color) {
        color[v] = 1;
        for (int[] e : graph.neighbors(v)) {
            int w = e[0];
            if (color[w] == 1) return true;
            if (color[w] == 0 && dfsCycleDirected(w, color)) return true;
        }
        color[v] = 2;
        return false;
    }

    // -------------------------------------------------------------------------
    // 5. CENTRALIDADE DE PROXIMIDADE
    // -------------------------------------------------------------------------

    /**
     * Calcula a Centralidade de Proximidade (Closeness Centrality) de cada vértice.
     *
     * <p>Fórmula: {@code C(v) = alcançáveis(v) / soma_distâncias(v)}
     *
     * <p>Usa o algoritmo de Dijkstra a partir de cada vértice para calcular
     * as distâncias mínimas. Vértices sem vizinhos alcançáveis recebem valor {@code 0.0}.
     *
     * <p>Complexidade: O(V × (V + E) log V)
     *
     * @return array com o valor de centralidade de proximidade de cada vértice
     */
    public double[] closenessCentrality() {
        double[] result = new double[graph.numVertices];
        for (int i = 0; i < graph.numVertices; i++) {
            int[] dist = dijkstra(i);
            double sum = 0;
            int reachable = 0;
            for (int d : dist)
                if (d != Integer.MAX_VALUE && d > 0) { sum += d; reachable++; }
            result[i] = reachable > 0 ? reachable / sum : 0.0;
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // 6. CENTRALIDADE DE INTERMEDIAÇÃO (Algoritmo de Brandes com Dijkstra)
    // -------------------------------------------------------------------------

    /**
     * Calcula a Centralidade de Intermediação (Betweenness Centrality) de cada vértice
     * usando o Algoritmo de Brandes com Dijkstra para suporte a grafos ponderados.
     *
     * <p>O valor de um vértice {@code v} é a soma, sobre todos os pares (s, t),
     * da fração dos caminhos mínimos de {@code s} a {@code t} que passam por {@code v}.
     *
     * <p>Complexidade: O(V × (V + E) log V)
     *
     * @return array com o valor de centralidade de intermediação de cada vértice
     */
    public double[] betweennessCentrality() {
        double[] betweenness = new double[graph.numVertices];

        for (int s = 0; s < graph.numVertices; s++) {
            Stack<Integer> stack = new Stack<>();
            @SuppressWarnings("unchecked")
            List<Integer>[] pred = new ArrayList[graph.numVertices];
            double[] sigma = new double[graph.numVertices];
            int[] dist = new int[graph.numVertices];
            double[] delta = new double[graph.numVertices];

            for (int i = 0; i < graph.numVertices; i++) {
                pred[i] = new ArrayList<>();
                dist[i] = Integer.MAX_VALUE;
            }
            dist[s] = 0;
            sigma[s] = 1;

            PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
            pq.add(new int[]{s, 0});

            while (!pq.isEmpty()) {
                int[] cur = pq.poll();
                int v = cur[0], d = cur[1];
                if (d > dist[v]) continue;
                stack.push(v);
                for (int[] e : graph.neighbors(v)) {
                    int w = e[0], weight = e[1];
                    int newDist = dist[v] + weight;
                    if (newDist < dist[w]) {
                        dist[w] = newDist;
                        sigma[w] = sigma[v];
                        pred[w].clear();
                        pred[w].add(v);
                        pq.add(new int[]{w, dist[w]});
                    } else if (newDist == dist[w]) {
                        sigma[w] += sigma[v];
                        pred[w].add(v);
                    }
                }
            }

            while (!stack.isEmpty()) {
                int w = stack.pop();
                for (int v : pred[w])
                    delta[v] += (sigma[v] / sigma[w]) * (1.0 + delta[w]);
                if (w != s) betweenness[w] += delta[w];
            }
        }
        return betweenness;
    }

    // -------------------------------------------------------------------------
    // DIJKSTRA
    // -------------------------------------------------------------------------

    /**
     * Algoritmo de Dijkstra — calcula a distância mínima de {@code source} a todos
     * os demais vértices usando um heap binário (fila de prioridade).
     *
     * <p>Vértices inalcançáveis recebem o valor {@link Integer#MAX_VALUE}.
     *
     * @param source vértice de origem (0-based)
     * @return array {@code dist} onde {@code dist[v]} é a menor distância de {@code source} a {@code v}
     */
    public int[] dijkstra(int source) {
        int[] dist = new int[graph.numVertices];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.add(new int[]{source, 0});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[0], d = cur[1];
            if (d > dist[u]) continue;
            for (int[] e : graph.neighbors(u)) {
                int v = e[0], w = e[1];
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    pq.add(new int[]{v, dist[v]});
                }
            }
        }
        return dist;
    }

    // -------------------------------------------------------------------------
    // AUXILIARES INTERNOS
    // -------------------------------------------------------------------------

    /**
     * BFS no grafo visto como não-direcionado a partir do vértice 0.
     * Marca como visitados todos os vértices alcançáveis, ignorando a direção das arestas.
     * Usado por {@link #isConnected()} para verificar conectividade fraca.
     *
     * @param visited vetor de visitados (modificado in-place)
     */
    private void bfsUndirected(boolean[] visited) {
        Queue<Integer> q = new LinkedList<>();
        visited[0] = true;
        q.add(0);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : undirectedNeighbors(u))
                if (!visited[v]) { visited[v] = true; q.add(v); }
        }
    }

    /**
     * Retorna os vizinhos do vértice {@code u} tratando o grafo como não-direcionado.
     * Para grafos direcionados, inclui tanto os sucessores quanto os predecessores de {@code u}.
     *
     * @param u índice do vértice
     * @return lista de índices de vértices adjacentes (sem repetição)
     */
    private List<Integer> undirectedNeighbors(int u) {
        Set<Integer> neighbors = new LinkedHashSet<>();
        for (int[] e : graph.neighbors(u)) neighbors.add(e[0]);
        // Para grafos direcionados, adiciona arestas inversas
        if (graph.directed) {
            for (int i = 0; i < graph.numVertices; i++)
                for (int[] e : graph.neighbors(i)) if (e[0] == u) neighbors.add(i);
        }
        return new ArrayList<>(neighbors);
    }
}
