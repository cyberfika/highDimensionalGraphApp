package graph.domain;

import graph.model.Graph;

import java.util.*;

/**
 * Lógica de domínio da rede social modelada pelo grafo.
 *
 * <p>Responsabilidade exclusiva: implementar as regras de negócio específicas
 * da rede social — sem algoritmos genéricos de grafos, sem I/O e sem geração.
 *
 * <p>Funcionalidade central: <b>Sistema de Recomendação de Seguidores</b>.
 * Dado um par (origem, destino), verifica via DFS se existe um caminho de conexões
 * e decide se recomenda que a origem siga o destino.
 */
public class SocialNetwork {

    private final Graph graph;

    /**
     * Cria o serviço de domínio vinculado ao grafo da rede social.
     *
     * @param graph grafo direcionado representando as relações de "seguir"
     */
    public SocialNetwork(Graph graph) {
        this.graph = graph;
    }

    /**
     * Verifica se existe um caminho de conexões entre origem e destino e,
     * caso exista, recomenda que a pessoa de origem siga a de destino.
     *
     * <p>Regras aplicadas:
     * <ol>
     *   <li>Se qualquer dos nomes não for encontrado no grafo → erro</li>
     *   <li>Se origem e destino forem a mesma pessoa → recusa</li>
     *   <li>Se origem já segue destino diretamente → informa</li>
     *   <li>Se existe caminho indireto via DFS → recomenda e exibe o caminho</li>
     *   <li>Se não existe caminho → não recomenda</li>
     * </ol>
     *
     * @param sourceName nome da pessoa de origem
     * @param targetName nome da pessoa de destino
     * @return mensagem de recomendação com o caminho encontrado, ou motivo da recusa
     */
    public String recommendFollower(String sourceName, String targetName) {
        int u = graph.findByName(sourceName);
        int v = graph.findByName(targetName);
        if (u == -1 || v == -1) return "Person(s) not found in the graph.";
        if (u == v) return "Cannot recommend a person to themselves.";
        for (int[] e : graph.neighbors(u))
            if (e[0] == v) return "'" + sourceName + "' already follows '" + targetName + "'.";

        List<Integer> path = new ArrayList<>();
        boolean[] visited = new boolean[graph.numVertices];
        if (dfsPath(u, v, visited, path)) {
            StringBuilder sb = new StringBuilder("Recommended! Path found:\n  ");
            for (int i = 0; i < path.size(); i++) {
                sb.append(graph.getName(path.get(i)));
                if (i < path.size() - 1) sb.append(" -> ");
            }
            return sb.toString();
        }
        return "Not recommended. No path exists between '" + sourceName + "' and '" + targetName + "'.";
    }

    /**
     * DFS auxiliar que rastreia o caminho completo entre {@code current} e {@code dest}.
     * Ao encontrar o destino, o caminho construído em {@code path} é o resultado final.
     * Ao retroceder (backtrack), o último vértice adicionado é removido da lista.
     *
     * @param current vértice atual da DFS
     * @param dest    vértice de destino
     * @param visited vetor de visitados para evitar ciclos
     * @param path    lista que acumula o caminho percorrido
     * @return {@code true} se o destino foi alcançado
     */
    private boolean dfsPath(int current, int dest, boolean[] visited, List<Integer> path) {
        visited[current] = true;
        path.add(current);
        if (current == dest) return true;
        for (int[] e : graph.neighbors(current)) {
            if (!visited[e[0]] && dfsPath(e[0], dest, visited, path)) return true;
        }
        path.remove(path.size() - 1);
        return false;
    }
}
