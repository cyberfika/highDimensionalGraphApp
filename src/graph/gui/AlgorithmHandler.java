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
import graph.model.Graph;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gerenciador de execução de algoritmos de análise de grafos.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Executar algoritmos de análise estrutural (conectividade, componentes, ciclos)</li>
 *   <li>Executar algoritmos de centralidade (proximidade, intermediação)</li>
 *   <li>Detectar propriedades especiais (grafo euleriano)</li>
 *   <li>Formatar resultados em HTML</li>
 *   <li>Ranking de nós por relevância</li>
 * </ul>
 *
 * <p>Padrões de Design:
 * <ul>
 *   <li><b>Observer:</b> Callback via {@link AlgorithmListener}</li>
 *   <li><b>Single Responsibility:</b> Apenas execução e formatação de algoritmos</li>
 *   <li><b>Dependency Injection:</b> Recebe Graph e GraphAlgorithms via construtor</li>
 *   <li><b>Worker Pattern:</b> Usa {@link ProgressDialog} para operações pesadas</li>
 * </ul>
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see GraphAlgorithms
 * @see GraphGUI
 * @see ProgressDialog
 */
public class AlgorithmHandler {

    /**
     * Listener para callbacks de execução de algoritmos.
     *
     * <p>Notifica quando algoritmo completa com resultado formatado
     * e lista de nós destacados.
     */
    public interface AlgorithmListener {
        /**
         * Chamado quando algoritmo completa com sucesso.
         *
         * @param htmlResults resultado formatado em HTML
         * @param topNodeIndices lista de índices de nós de topo (para highlighting)
         */
        void onResultsReady(String htmlResults, List<Integer> topNodeIndices);

        /**
         * Chamado quando erro ocorre durante execução.
         *
         * @param message descrição do erro
         */
        void onError(String message);
    }

    private final Graph graph;
    private final GraphAlgorithms algorithms;
    private final AlgorithmListener listener;

    /**
     * Inicializa o gerenciador de algoritmos com injeção de dependências.
     *
     * @param graph grafo a ser analisado
     * @param algorithms instância de {@link GraphAlgorithms} com operações
     * @param listener callback para notificações de conclusão
     */
    public AlgorithmHandler(Graph graph, GraphAlgorithms algorithms, AlgorithmListener listener) {
        this.graph = graph;
        this.algorithms = algorithms;
        this.listener = listener;
    }

    /**
     * Verifica conectividade do grafo.
     *
     * <p>Identifica se o grafo é:\n * <ul>
     *   <li><b>Fortemente conectado:</b> Caminho existe entre todos os pares de nós</li>
     *   <li><b>Desconexo:</b> Existem nós ou componentes isolados</li>
     * </ul>
     *
     * <p>Resultado é exibido em cor verde (conectado) ou vermelha (desconexo).
     *
     * @param owner janela proprietária (para diálogos modais)
     */
    public void checkConnectivity(JFrame owner) {
        boolean connected = algorithms.isConnected();
        String detail = connected
            ? "O grafo é <b>fortemente conectado</b>."
            : "O grafo <b>não é conectado</b>.";

        String html = "<b>Análise de Conectividade</b><br>" +
                "• Resultado: <font color=\"" + (connected ? "#4ac997" : "#ff6b6b") + "\"><b>" +
                (connected ? "Conexo" : "Desconexo") + "</b></font><br><br>" + detail;

        listener.onResultsReady(html, new ArrayList<>());
    }

    /**
     * Calcula componentes conexos do grafo.
     *
     * <p>Agrupa nós em componentes máximos onde todos os nós
     * de um componente estão conectados.\n *
     * <p>Exibe quantidade total de componentes e tamanho
     * dos primeiros 15 componentes (para não sobrecarregar).
     *
     * <p>Operação é executada em background via {@link ProgressDialog}
     * para prevenir congelamento da UI.
     *
     * @param owner janela proprietária (para diálogos modais)
     */
    public void checkComponents(JFrame owner) {
        ProgressDialog worker = new ProgressDialog(owner, "Calculando Componentes...") {
            private List<List<Integer>> components;

            @Override
            protected Void doInBackground() throws Exception {
                components = algorithms.getComponents();
                return null;
            }

            @Override
            protected void done() {
                super.done();
                StringBuilder sb = new StringBuilder("<b>Componentes Conexos (" + components.size() + " total)</b><br>");
                for (int i = 0; i < Math.min(15, components.size()); i++) {
                    List<Integer> c = components.get(i);
                    sb.append("<b>Componente ").append(i + 1).append("</b> (").append(c.size()).append(" nós)<br>");
                }
                listener.onResultsReady(sb.toString(), new ArrayList<>());
            }
        };
        worker.executeWithDialog();
    }

    /**
     * Detecta presença de ciclos no grafo.
     *
     * <p>Um ciclo é um caminho que começa e termina no mesmo nó.\n * Resultado exibido em:\n * <ul>
     *   <li><b>Verde:</b> Sem ciclos (DAG - Directed Acyclic Graph)</li>
     *   <li><b>Vermelho:</b> Contém ciclos</li>
     * </ul>
     *
     * @param owner janela proprietária (para diálogos modais)
     */
    public void checkCycles(JFrame owner) {
        boolean hasCycle = algorithms.hasCycle();
        String html = "<b>Detecção de Ciclos</b><br>" +
                "• Contém Ciclo: <font color=\"" + (hasCycle ? "#ff6b6b" : "#4ac997") + "\"><b>" +
                (hasCycle ? "Sim" : "Não") + "</b></font>";

        listener.onResultsReady(html, new ArrayList<>());
    }

    /**
     * Verifica propriedades eulerianas do grafo.
     *
     * <p>Classifica grafo como:\n * <ul>
     *   <li><b>Euleriano:</b> Contém ciclo euleriano (todos nós tem grau par)</li>
     *   <li><b>Semi-euleriano:</b> Contém caminho euleriano (exatamente 2 nós com grau ímpar)</li>
     *   <li><b>Não-euleriano:</b> Nenhum</li>
     * </ul>
     *
     * <p>Resultado é exibido em cor verde (sim), amarela (semi) ou vermelha (não).
     *
     * @param owner janela proprietária (para diálogos modais)
     */
    public void checkEulerian(JFrame owner) {
        int res = algorithms.isEulerian();
        String status = switch (res) {
            case 2 -> "<font color=\"#4ac997\"><b>Sim (Ciclo Euleriano)</b></font>";
            case 1 -> "<font color=\"#ffb703\"><b>Semi-Euleriano</b></font>";
            default -> "<font color=\"#ff6b6b\"><b>Não</b></font>";
        };

        String html = "<b>Grafo Euleriano?</b><br>• Resultado: " + status;
        listener.onResultsReady(html, new ArrayList<>());
    }

    /**
     * Calcula centralidade de proximidade de todos os nós.
     *
     * <p>Proximidade mede quão perto (em passos) um nó está de todos os outros.\n * Nós com alta proximidade são \"centrais\" na rede.\n *
     * <p>Para grafos grandes (>500 nós), exibe confirmação pois cálculo
     * pode levar tempo.\n *
     * <p>Resultado exibe top 10 nós com maior proximidade,\n * com os 5 primeiros destacados na visualização.
     *
     * @param owner janela proprietária (para diálogos modais e confirmação)
     */
    public void calculateCloseness(JFrame owner) {
        if (graph.numVertices > 500) {
            int option = JOptionPane.showConfirmDialog(owner,
                "Grafo grande. O cálculo pode levar tempo. Continuar?",
                "Cálculo Pesado", JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION) return;
        }

        ProgressDialog worker = new ProgressDialog(owner, "Calculando Proximidade...") {
            private double[] closeness;

            @Override
            protected Void doInBackground() throws Exception {
                closeness = algorithms.closenessCentrality();
                return null;
            }

            @Override
            protected void done() {
                super.done();
                List<NodeRank> ranking = new ArrayList<>();
                for (int i = 0; i < graph.numVertices; i++) {
                    ranking.add(new NodeRank(i, closeness[i]));
                }
                Collections.sort(ranking);

                StringBuilder sb = new StringBuilder("<b>Centralidade de Proximidade (Top 10)</b><br>");
                List<Integer> topIndices = new ArrayList<>();
                for (int i = 0; i < Math.min(10, ranking.size()); i++) {
                    NodeRank r = ranking.get(i);
                    sb.append(String.format("<b>#%d %s:</b> %.6f<br>", i + 1, graph.getName(r.index), r.value));
                    if (i < 5) topIndices.add(r.index);
                }
                listener.onResultsReady(sb.toString(), topIndices);
            }
        };
        worker.executeWithDialog();
    }

    /**
     * Calcula centralidade de intermediação de todos os nós.
     *
     * <p>Intermediação mede quantos caminhos mínimos passam por um nó.\n * Nós com alta intermediação são \"bridges\" (pontes) na rede.\n *
     * <p>Para grafos grandes (>500 nós), exibe confirmação pois cálculo
     * pode levar tempo.\n *
     * <p>Resultado exibe top 10 nós com maior intermediação,\n * com os 5 primeiros destacados na visualização.
     *
     * @param owner janela proprietária (para diálogos modais e confirmação)
     */
    public void calculateBetweenness(JFrame owner) {
        if (graph.numVertices > 500) {
            int option = JOptionPane.showConfirmDialog(owner,
                "Grafo grande. O cálculo pode levar tempo. Continuar?",
                "Cálculo Pesado", JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION) return;
        }

        ProgressDialog worker = new ProgressDialog(owner, "Calculando Intermediação...") {
            private double[] betweenness;

            @Override
            protected Void doInBackground() throws Exception {
                betweenness = algorithms.betweennessCentrality();
                return null;
            }

            @Override
            protected void done() {
                super.done();
                List<NodeRank> ranking = new ArrayList<>();
                for (int i = 0; i < graph.numVertices; i++) {
                    ranking.add(new NodeRank(i, betweenness[i]));
                }
                Collections.sort(ranking);

                StringBuilder sb = new StringBuilder("<b>Centralidade de Intermediação (Top 10)</b><br>");
                List<Integer> topIndices = new ArrayList<>();
                for (int i = 0; i < Math.min(10, ranking.size()); i++) {
                    NodeRank r = ranking.get(i);
                    sb.append(String.format("<b>#%d %s:</b> %.2f<br>", i + 1, graph.getName(r.index), r.value));
                    if (i < 5) topIndices.add(r.index);
                }
                listener.onResultsReady(sb.toString(), topIndices);
            }
        };
        worker.executeWithDialog();
    }

    /**
     * Classe interna para armazenar ranking de nó.
     *
     * <p>Usada para ordenar nós por valor de centralidade.\n * Implementa {@link Comparable} para ordenação decrescente.
     */
    private static class NodeRank implements Comparable<NodeRank> {
        /** Índice do nó no grafo */
        final int index;
        /** Valor de centralidade (proximidade ou intermediação) */
        final double value;

        /**
         * Cria entrada de ranking de nó.
         *
         * @param index índice do nó
         * @param value valor de centralidade
         */
        NodeRank(int index, double value) {
            this.index = index;
            this.value = value;
        }

        /**
         * Compara dois rankings de nó em ordem decrescente (maior valor primeiro).
         *
         * @param o outro NodeRank para comparação
         * @return valor negativo se este < outro, 0 se igual, positivo se este > outro
         */
        @Override
        public int compareTo(NodeRank o) {
            return Double.compare(o.value, this.value);
        }
    }
}
