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

import graph.generator.GraphGenerator;
import graph.io.PajekIO;
import graph.model.Graph;

import javax.swing.*;
import java.io.File;

/**
 * Gerenciador de carregamento e geração de grafos.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Gerar rede social a partir de arquivo de nomes</li>
 *   <li>Gerar grafo aleatório com parâmetros customizáveis</li>
 *   <li>Carregar grafo no formato Pajek (.net)</li>
 *   <li>Validar operações e notificar resultado</li>
 * </ul>
 *
 * <p>Padrões de Design:
 * <ul>
 *   <li><b>Observer:</b> Callback via {@link GraphLoadListener}</li>
 *   <li><b>Single Responsibility:</b> Apenas geração e carregamento</li>
 *   <li><b>Worker Pattern:</b> Usa {@link ProgressDialog} para operações pesadas</li>
 * </ul>
 *
 * <p>Fluxo de Operação:
 * <ol>
 *   <li>Cliente chama método de geração/carregamento (ex: {@link #generateSocialNetwork(JFrame)})</li>
 *   <li>ProgressDialog exibe feedback visual enquanto operação ocorre em background</li>
 *   <li>Ao completar, listener é notificado com novo grafo via {@link GraphLoadListener#onGraphLoaded(Graph, boolean)}</li>
 *   <li>Se erro ocorre, listener é notificado com mensagem descritiva</li>
 * </ol>
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see GraphLoadListener
 * @see GraphGenerator
 * @see PajekIO
 * @see ProgressDialog
 */
public class GraphLoadingHandler {

    /**
     * Listener para callbacks de carregamento/geração de grafo.
     *
     * <p>Notifica quando operação de grafo completa com:\n * <ul>
     *   <li>Novo grafo carregado/gerado</li>
     *   <li>Flag indicando origem do grafo (arquivo vs. gerado)</li>
     * </ul>
     */
    public interface GraphLoadListener {
        /**
         * Chamado quando novo grafo é carregado com sucesso.
         *
         * @param graph novo grafo carregado/gerado
         * @param fromFile {@code true} se grafo foi importado de arquivo,
         *                  {@code false} se foi gerado programaticamente
         */
        void onGraphLoaded(Graph graph, boolean fromFile);

        /**
         * Chamado quando erro ocorre durante carregamento/geração.
         *
         * @param message descrição do erro
         */
        void onError(String message);
    }

    private final GraphLoadListener listener;

    /**
     * Inicializa o gerenciador de carregamento com listener callback.
     *
     * @param listener callback para notificações de conclusão
     */
    public GraphLoadingHandler(GraphLoadListener listener) {
        this.listener = listener;
    }

    /**
     * Gera rede social aleatória a partir de arquivo de nomes.
     *
     * <p>Utiliza {@link GraphGenerator#generateSocialNetwork(String)} para:\n * <ul>
     *   <li>Ler arquivo {@code data/names.txt} com lista de nomes</li>
     *   <li>Gerar ~5000 usuários com 25000+ arestas (estrutura realista)</li>
     *   <li>Distribuição de graus segue padrão de rede social real</li>
     * </ul>
     *
     * <p>Operação é executada em background via {@link ProgressDialog}
     * para prevenir congelamento da UI durante geração.
     *
     * <p>Ao completar, listener é notificado com {@code fromFile=false}
     * indicando que grafo foi gerado, não importado de arquivo.
     *
     * @param owner janela proprietária (para diálogo de progresso)
     * @see GraphGenerator#generateSocialNetwork(String)
     */
    public void generateSocialNetwork(JFrame owner) {
        ProgressDialog worker = new ProgressDialog(owner, "Gerando Rede Social...") {
            private Graph resultGraph;

            @Override
            protected Void doInBackground() throws Exception {
                resultGraph = GraphGenerator.generateSocialNetwork("names.txt");
                return null;
            }

            @Override
            protected void done() {
                super.done();
                if (resultGraph != null) {
                    listener.onGraphLoaded(resultGraph, false);
                } else {
                    listener.onError("Erro ao carregar data/names.txt. Verifique se o arquivo existe.");
                }
            }
        };
        worker.executeWithDialog();
    }

    /**
     * Gera grafo aleatório com parâmetros customizáveis.
     *
     * <p>Utiliza {@link GraphGenerator#generateRandomGraph(int, int, boolean)} para:\n * <ul>
     *   <li>Criar grafo com número específico de vértices</li>
     *   <li>Distribuir número específico de arestas aleatoriamente</li>
     *   <li>Opcionalmente forçar conectividade (sem nós isolados)</li>
     * </ul>
     *
     * <p>Operação é executada em background via {@link ProgressDialog}.\n *
     * <p>Ao completar, listener é notificado com {@code fromFile=false}
     * indicando que grafo foi gerado, não importado de arquivo.
     *
     * @param owner janela proprietária (para diálogo de progresso)
     * @param vertices número de vértices desejados
     * @param edges número de arestas desejadas
     * @param connected {@code true} para forçar grafo conectado,
     *                  {@code false} para permitir componentes desconexas
     * @see GraphGenerator#generateRandomGraph(int, int, boolean)
     */
    public void generateRandomGraph(JFrame owner, int vertices, int edges, boolean connected) {
        ProgressDialog worker = new ProgressDialog(owner, "Gerando Grafo Aleatório...") {
            private Graph resultGraph;

            @Override
            protected Void doInBackground() throws Exception {
                resultGraph = GraphGenerator.generateRandomGraph(vertices, edges, connected);
                return null;
            }

            @Override
            protected void done() {
                super.done();
                if (resultGraph != null) {
                    listener.onGraphLoaded(resultGraph, false);
                } else {
                    listener.onError("Erro na geração. Verifique os parâmetros.");
                }
            }
        };
        worker.executeWithDialog();
    }

    /**
     * Carrega grafo no formato Pajek (.net).
     *
     * <p>Utiliza {@link PajekIO#importFrom(String)} para:\n * <ul>
     *   <li>Ler arquivo .net da pasta {@code pajek/input}</li>
     *   <li>Parsear vértices com nomes e arestas com pesos (opcionais)</li>
     *   <li>Reconstruir estrutura de grafo em memória</li>
     * </ul>
     *
     * <p>Operação é executada em background via {@link ProgressDialog}
     * para prevenir congelamento em arquivos grandes.\n *
     * <p>Ao completar, listener é notificado com {@code fromFile=true}
     * indicando que grafo foi importado de arquivo.
     *
     * @param owner janela proprietária (para diálogo de progresso)
     * @param filename nome do arquivo (sem caminho, procura em {@code pajek/input})\n * @see PajekIO#importFrom(String)
     */
    public void loadPajek(JFrame owner, String filename) {
        ProgressDialog worker = new ProgressDialog(owner, "Carregando arquivo Pajek...") {
            private Graph resultGraph;

            @Override
            protected Void doInBackground() throws Exception {
                resultGraph = PajekIO.importFrom(filename);
                return null;
            }

            @Override
            protected void done() {
                super.done();
                if (resultGraph != null) {
                    listener.onGraphLoaded(resultGraph, true);
                } else {
                    listener.onError("Erro ao carregar o arquivo Pajek.");
                }
            }
        };
        worker.executeWithDialog();
    }
}
