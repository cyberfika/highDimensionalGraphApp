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

import graph.domain.SocialNetwork;
import graph.model.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador de operações específicas de rede social.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Recomendação de seguidores (DFS para encontrar caminhos)</li>
 *   <li>Exibição de conexões de um nó (seguidores/seguindo)</li>
 *   <li>Formatação de resultados em HTML</li>
 * </ul>
 *
 * <p>Padrões de Design:
 * <ul>
 *   <li><b>Observer:</b> Callback via {@link SocialNetworkListener}</li>
 *   <li><b>Single Responsibility:</b> Apenas lógica de rede social</li>
 *   <li><b>Dependency Injection:</b> Recebe Graph e SocialNetwork via construtor</li>
 * </ul>
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see SocialNetwork
 * @see GraphGUI
 * @see Graph
 */
public class SocialNetworkHandler {

    /**
     * Listener para callbacks de operações de rede social.
     *
     * <p>Notifica quando operação é concluída com:
     * <ul>
     *   <li>Resultado formatado em HTML</li>
     *   <li>Lista de índices de nós a destacar na visualização</li>
     * </ul>
     */
    public interface SocialNetworkListener {
        /**
         * Chamado quando operação de rede social é concluída.
         *
         * @param htmlResult resultado formatado em HTML
         * @param highlightedPath lista de índices de nós para destacar (ex: caminho recomendado)
         */
        void onResult(String htmlResult, List<Integer> highlightedPath);
    }

    private final Graph graph;
    private final SocialNetwork socialNetwork;
    private final SocialNetworkListener listener;

    /**
     * Inicializa o gerenciador de rede social com injeção de dependências.
     *
     * @param graph grafo com estrutura de rede social
     * @param socialNetwork instância de {@link SocialNetwork} com operações de rede
     * @param listener callback para notificações de conclusão
     */
    public SocialNetworkHandler(Graph graph, SocialNetwork socialNetwork, SocialNetworkListener listener) {
        this.graph = graph;
        this.socialNetwork = socialNetwork;
        this.listener = listener;
    }

    /**
     * Recomenda seguidor entre dois usuários em rede social.
     *
     * <p>Utiliza {@link SocialNetwork#recommendFollower(String, String)} para:\n * <ul>
     *   <li>Validar se recomendação é segura (sem ciclos de preferência)</li>
     *   <li>Encontrar caminho DFS entre origem e destino</li>
     *   <li>Formatar resultado em HTML com highlighting visual</li>
     * </ul>
     *
     * <p>Resultado é notificado ao listener com lista de índices
     * de nós no caminho recomendado para destacar na visualização.
     *
     * @param sourceName nome da pessoa origem
     * @param targetName nome da pessoa destino
     */
    public void recommendFollower(String sourceName, String targetName) {
        String resultStr = socialNetwork.recommendFollower(sourceName, targetName);

        String htmlResult;
        List<Integer> pathIndices = new ArrayList<>();

        if (resultStr.startsWith("Recommended!")) {
            String pathPart = resultStr.substring(resultStr.indexOf("path found:") + 11).trim();
            htmlResult = "<font color=\"#4ac997\"><b>Recomendação Aprovada!</b></font><br>" +
                    "<b>Caminho encontrado:</b><br><font color=\"#fb8500\">" +
                    pathPart.replace("->", " ➔ ") + "</font>";

            String[] names = pathPart.split(" -> ");
            for (String name : names) {
                int id = graph.findByName(name.trim());
                if (id != -1) pathIndices.add(id);
            }
        } else {
            htmlResult = "<font color=\"#ff6b6b\"><b>Recomendação Rejeitada</b></font><br>" + resultStr;
        }

        listener.onResult(htmlResult, pathIndices);
    }

    /**
     * Exibe todas as conexões (seguidores e seguindo) de um nó.
     *
     * <p>Calcula e exibe:\n * <ul>
     *   <li><b>Segue:</b> Pessoas que este nó segue (arestas de saída, primeiras 10)</li>
     *   <li><b>Seguido por:</b> Pessoas que seguem este nó (arestas de entrada, primeiras 10)</li>
     * </ul>
     *
     * <p>Se houver mais de 10 conexões de um tipo, exibe mensagem
     * "... e mais X pessoas".
     *
     * @param nodeIndex índice do nó cuja conexões serão exibidas
     */
    public void showFollowers(int nodeIndex) {
        List<int[]> outEdges = graph.neighbors(nodeIndex);
        List<Integer> inNodes = new ArrayList<>();

        for (int u = 0; u < graph.numVertices; u++) {
            if (u == nodeIndex) continue;
            for (int[] edge : graph.neighbors(u)) {
                if (edge[0] == nodeIndex) {
                    inNodes.add(u);
                    break;
                }
            }
        }

        StringBuilder sb = new StringBuilder("<b>Conexões de " + graph.getName(nodeIndex) + "</b><br>");

        sb.append("<b>Segue (").append(outEdges.size()).append(" pessoas):</b><br>");
        if (outEdges.isEmpty()) {
            sb.append("<font color=\"#8b949e\">Ninguém</font><br>");
        } else {
            for (int i = 0; i < Math.min(10, outEdges.size()); i++) {
                sb.append("• ").append(graph.getName(outEdges.get(i)[0])).append("<br>");
            }
            if (outEdges.size() > 10) {
                sb.append("• ... e mais ").append(outEdges.size() - 10).append(" pessoas.");
            }
        }

        sb.append("<br><b>Seguido por (").append(inNodes.size()).append(" pessoas):</b><br>");
        if (inNodes.isEmpty()) {
            sb.append("<font color=\"#8b949e\">Ninguém</font><br>");
        } else {
            for (int i = 0; i < Math.min(10, inNodes.size()); i++) {
                sb.append("• ").append(graph.getName(inNodes.get(i))).append("<br>");
            }
            if (inNodes.size() > 10) {
                sb.append("• ... e mais ").append(inNodes.size() - 10).append(" pessoas.");
            }
        }

        listener.onResult(sb.toString(), new ArrayList<>());
    }
}
