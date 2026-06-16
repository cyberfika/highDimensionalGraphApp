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

package graph.model;

import java.util.*;

/**
 * Estrutura de dados central da aplicação: grafo ponderado e rotulado.
 *
 * <p>Representação interna por lista de adjacências ({@code List<int[]>[]}) onde
 * cada entrada do array {@code int[]} contém dois valores: {@code [destino, peso]}.
 *
 * <p>Responsabilidade exclusiva desta classe: armazenar vértices, arestas e rótulos.
 * Algoritmos de grafos, geração e I/O são delegados a classes dedicadas nos
 * pacotes {@code graph.algorithm}, {@code graph.generator} e {@code graph.io}.
 */
public class Graph {

    /** Número total de vértices. Imutável após a construção. */
    public final int numVertices;

    /**
     * Indica se o grafo é direcionado ({@code true}) ou não-direcionado ({@code false}).
     * Grafos não-direcionados armazenam cada aresta nos dois sentidos automaticamente.
     */
    public boolean directed;

    /** Rótulos (nomes) de cada vértice. Inicializados como "V0", "V1", etc. */
    private final String[] names;

    /**
     * Lista de adjacências. {@code adj[u]} contém todos os vizinhos de {@code u}
     * como pares {@code {destino, peso}}.
     */
    private final List<int[]>[] adj;

    /**
     * Constrói um grafo vazio com {@code numVertices} vértices.
     * Todos os vértices recebem o rótulo padrão "V{i}".
     *
     * @param numVertices número de vértices
     * @param directed    {@code true} para grafo direcionado
     */
    @SuppressWarnings("unchecked")
    public Graph(int numVertices, boolean directed) {
        this.numVertices = numVertices;
        this.directed = directed;
        this.names = new String[numVertices];
        this.adj = new ArrayList[numVertices];
        for (int i = 0; i < numVertices; i++) {
            names[i] = "V" + i;
            adj[i] = new ArrayList<>();
        }
    }

    /**
     * Define o rótulo (nome) do vértice {@code i}.
     *
     * @param i    índice do vértice (0-based)
     * @param name nome a atribuir
     */
    public void setName(int i, String name) { names[i] = name; }

    /**
     * Retorna o rótulo (nome) do vértice {@code i}.
     *
     * @param i índice do vértice (0-based)
     * @return nome do vértice
     */
    public String getName(int i) { return names[i]; }

    /**
     * Adiciona uma aresta ponderada de {@code u} para {@code v}.
     * Para grafos não-direcionados, a aresta inversa ({@code v → u}) é adicionada automaticamente.
     *
     * @param u      vértice de origem (0-based)
     * @param v      vértice de destino (0-based)
     * @param weight peso da aresta
     */
    public void addEdge(int u, int v, int weight) {
        adj[u].add(new int[]{v, weight});
        if (!directed) adj[v].add(new int[]{u, weight});
    }

    /**
     * Busca um vértice pelo nome (comparação sem distinção de maiúsculas/minúsculas).
     *
     * @param name nome a buscar
     * @return índice do vértice, ou {@code -1} se não encontrado
     */
    public int findByName(String name) {
        for (int i = 0; i < numVertices; i++)
            if (names[i].equalsIgnoreCase(name)) return i;
        return -1;
    }

    /**
     * Retorna a lista de adjacências do vértice {@code u}.
     * Cada elemento é um array {@code {destino, peso}}.
     *
     * <p><b>Atenção:</b> retorna a lista interna diretamente (sem cópia defensiva)
     * por motivos de desempenho. Não modifique a lista retornada.
     *
     * @param u índice do vértice (0-based)
     * @return lista de arestas saindo de {@code u}
     */
    public List<int[]> neighbors(int u) { return adj[u]; }
}
