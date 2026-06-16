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

package graph.gui;

import graph.algorithm.GraphAlgorithms;
import graph.domain.SocialNetwork;
import graph.model.Graph;

/**
 * Centraliza o gerenciamento de estado da aplicação.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Manter referência ao grafo carregado ({@link Graph})</li>
 *   <li>Manter referência aos algoritmos associados ({@link GraphAlgorithms})</li>
 *   <li>Manter referência à rede social ({@link SocialNetwork})</li>
 *   <li>Rastrear metadata (origem do grafo, tempo de execução)</li>
 * </ul>
 *
 * <p>Design Pattern:
 * <ul>
 *   <li><b>Model:</b> Representa o estado da aplicação (parte do MVC)</li>\n *   <li><b>Single Responsibility:</b> Apenas gerencia estado, não lógica de negócio</li>
 * </ul>
 *
 * <p>Benefícios:\n * <ul>
 *   <li>Fonte única de verdade para estado da aplicação</li>
 *   <li>Facilita testes unitários (injeção de dependência)</li>
 *   <li>Permite resetar estado facilmente ({@link #clear()})</li>
 * </ul>
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see GraphGUI
 * @see Graph
 * @see GraphAlgorithms
 * @see SocialNetwork
 */
public class GraphUIState {
    private Graph graph;
    private GraphAlgorithms algorithms;
    private SocialNetwork socialNetwork;
    private boolean wasLoadedFromFile;
    private long lastExecutionTime;

    /**
     * Define o grafo carregado e inicializa estruturas associadas.
     *
     * <p>Quando um grafo é definido (g != null), automaticamente\n * cria instâncias de {@link GraphAlgorithms} e {@link SocialNetwork}\n * para oferecer uma interface unificada.
     *
     * <p>Quando grafo é nulo (g == null), limpa algoritmos e rede social.\n *
     * @param g grafo a ser carregado (pode ser nulo para limpar)
     * @param fromFile {@code true} se grafo foi carregado de arquivo,
     *                  {@code false} se foi gerado programaticamente
     */
    public void setGraph(Graph g, boolean fromFile) {
        this.graph = g;
        this.wasLoadedFromFile = fromFile;
        if (g != null) {
            this.algorithms = new GraphAlgorithms(g);
            this.socialNetwork = new SocialNetwork(g);
        } else {
            this.algorithms = null;
            this.socialNetwork = null;
        }
        this.lastExecutionTime = 0;
    }

    /**
     * Obtém o grafo atualmente carregado.
     *
     * @return grafo carregado, ou {@code null} se nenhum grafo foi carregado
     */
    public Graph getGraph() { return graph; }

    /**
     * Obtém os algoritmos associados ao grafo carregado.
     *
     * @return instância de {@link GraphAlgorithms}, ou {@code null} se nenhum grafo foi carregado
     */
    public GraphAlgorithms getAlgorithms() { return algorithms; }

    /**
     * Obtém a rede social associada ao grafo carregado.
     *
     * @return instância de {@link SocialNetwork}, ou {@code null} se nenhum grafo foi carregado
     */
    public SocialNetwork getSocialNetwork() { return socialNetwork; }

    /**
     * Verifica se grafo atual foi carregado de arquivo.
     *
     * @return {@code true} se grafo foi importado de arquivo,
     *         {@code false} se foi gerado programaticamente
     */
    public boolean isFromFile() { return wasLoadedFromFile; }

    /**
     * Obtém o tempo de execução da última operação (em milissegundos).
     *
     * @return tempo em ms, ou 0 se nenhuma operação foi executada ainda
     */
    public long getLastExecutionTime() { return lastExecutionTime; }

    /**
     * Define o tempo de execução da última operação.
     *
     * @param time tempo em milissegundos
     */
    public void setLastExecutionTime(long time) { this.lastExecutionTime = time; }

    /**
     * Limpa todo o estado da aplicação.
     *
     * <p>Reseta:\n * <ul>
     *   <li>Grafo carregado</li>
     *   <li>Algoritmos associados</li>
     *   <li>Rede social associada</li>
     *   <li>Flag de origem do arquivo</li>
     *   <li>Tempo de execução</li>
     * </ul>
     */
    public void clear() {
        graph = null;
        algorithms = null;
        socialNetwork = null;
        wasLoadedFromFile = false;
        lastExecutionTime = 0;
    }
}
