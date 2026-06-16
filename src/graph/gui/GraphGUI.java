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

import graph.model.Graph;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface gráfica principal da aplicação GraphNet Analyzer.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Orquestração da interface de usuário (UI lifecycle)</li>
 *   <li>Roteamento de eventos para handlers especializados</li>
 *   <li>Gerenciamento de layout (header, sidebar esquerdo, painel central, sidebar direito)</li>
 *   <li>Atualização de estatísticas e resultados em tempo real</li>
 * </ul>
 *
 * <p>Padrões de Design:
 * <ul>
 *   <li><b>MVC:</b> This class acts as the View, delegating Model updates to handlers</li>
 *   <li><b>Facade:</b> Exposes unified interface to handlers (GraphLoadingHandler, AlgorithmHandler, SocialNetworkHandler)</li>
 *   <li><b>Observer:</b> Receives callbacks via listener interfaces from handlers</li>
 *   <li><b>Dependency Injection:</b> Handlers receive dependencies through constructors</li>
 * </ul>
 *
 * <p>Arquitetura Refatorada:
 * A classe original (1600+ linhas) foi refatorada para ~250 linhas de orquestração pura.
 * Todo código de negócio foi extraído para:
 * <ul>
 *   <li>{@link GraphLoadingHandler} — Geração e carregamento de grafos</li>
 *   <li>{@link AlgorithmHandler} — Execução de algoritmos de análise</li>
 *   <li>{@link SocialNetworkHandler} — Operações de rede social (recomendações, seguidores)</li>
 *   <li>{@link GraphUIState} — Gerenciamento centralizado de estado</li>
 * </ul>
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see GraphUIState
 * @see GraphLoadingHandler
 * @see AlgorithmHandler
 * @see SocialNetworkHandler
 * @see GraphPanel
 * @see Theme
 */
public class GraphGUI extends JFrame {
    private final GraphUIState state = new GraphUIState();
    private GraphLoadingHandler loadingHandler;
    private AlgorithmHandler algorithmHandler;
    private SocialNetworkHandler socialHandler;

    private final GraphPanel graphPanel = new GraphPanel();
    private JTextPane txtResults;
    private DefaultListModel<String> topNodesListModel;
    private JList<String> lstTopNodes;
    private List<Integer> topNodesIndices = new ArrayList<>();

    /**
     * Inicializa a interface gráfica principal.
     *
     * <p>Cria a estrutura de janela, configura layouts (BorderLayout),
     * inicializa componentes (header, sidebars, painel central),
     * e prepara os handlers para operações de negócio.
     *
     * <p>Antialiasing é ativado para melhor qualidade de renderização de texto.
     */
    public GraphGUI() {
        setTitle("GraphNet Analyzer");
        setSize(1250, 850);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.setBackground(new Color(13, 17, 23));

        container.add(createHeader(), BorderLayout.NORTH);
        container.add(graphPanel, BorderLayout.CENTER);
        container.add(createLeftSidebar(), BorderLayout.WEST);
        container.add(createRightSidebar(), BorderLayout.EAST);

        initializeHandlers();
        updateStats();
        logHTML("<b>Bem-vindo!</b><br><br>Gere uma rede social ou carregue um grafo.");
    }

    /**
     * Cria o cabeçalho (header) da aplicação.
     *
     * <p>Contém:
     * <ul>
     *   <li>Título "GraphNet Analyzer"</li>
     *   <li>Fundo escuro com altura fixa de 64px</li>
     * </ul>
     *
     * @return painel configurado com header
     */
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(22, 27, 34));
        panel.setPreferredSize(new Dimension(getWidth(), 64));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        JLabel title = new JLabel("GraphNet Analyzer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(230, 237, 243));
        panel.add(title, BorderLayout.WEST);

        return panel;
    }

    /**
     * Cria a barra lateral esquerda (sidebar esquerdo).
     *
     * <p>Contém todos os botões de ação organizados em grupos:
     * <ul>
     *   <li><b>Geração/Carregamento:</b> Gerar Rede Social, Grafo Aleatório, Carregar Pajek</li>
     *   <li><b>Análise Estrutural:</b> Conectividade, Componentes, Ciclos, Euleriano</li>
     *   <li><b>Centralidade:</b> Proximidade, Intermediação</li>
     *   <li><b>Rede Social:</b> Recomendação, Seguidores</li>
     *   <li><b>Utilidades:</b> Exportar, Salvar/Carregar Sessão, Novo</li>
     * </ul>
     *
     * <p>Responsabilidades:
     * <ul>
     *   <li>Arranjo visual dos botões</li>
     *   <li>Roteamento de cliques para handlers apropriados</li>
     *   <li>Validação de pré-condições (ex: grafo carregado?)</li>
     * </ul>
     *
     * @return painel configurado com sidebar esquerdo
     */
    private JPanel createLeftSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 17, 23));
        panel.setPreferredSize(new Dimension(280, getHeight()));

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        buttons.add(createButton("Gerar Rede Social", e -> loadingHandler.generateSocialNetwork(this)));
        buttons.add(createButton("Grafo Aleatório", e -> showRandomGraphDialog()));
        buttons.add(createButton("Carregar Pajek", e -> showLoadPajekDialog()));
        buttons.add(Box.createVerticalStrut(16));
        buttons.add(createButton("Conectividade", e -> run(() -> algorithmHandler.checkConnectivity(this))));
        buttons.add(createButton("Componentes", e -> run(() -> algorithmHandler.checkComponents(this))));
        buttons.add(createButton("Ciclos", e -> run(() -> algorithmHandler.checkCycles(this))));
        buttons.add(createButton("Euleriano", e -> run(() -> algorithmHandler.checkEulerian(this))));
        buttons.add(Box.createVerticalStrut(16));
        buttons.add(createButton("Centralidade Proximidade", e -> run(() -> algorithmHandler.calculateCloseness(this))));
        buttons.add(createButton("Centralidade Intermediação", e -> run(() -> algorithmHandler.calculateBetweenness(this))));
        buttons.add(Box.createVerticalStrut(16));
        buttons.add(createButton("Recomendação", e -> { if (checkGraph()) showRecommendationDialog(); }));
        buttons.add(createButton("Seguidores", e -> { if (checkGraph()) showFollowersDialog(); }));
        buttons.add(Box.createVerticalStrut(16));
        buttons.add(createButton("Exportar", e -> { if (checkGraph() && !state.isFromFile()) showExportDialog(); }));
        buttons.add(createButton("Salvar Sessão", e -> { if (checkGraph()) performSaveSession(); }));
        buttons.add(createButton("Carregar Sessão", e -> performLoadSession()));
        buttons.add(createButton("Novo", e -> handleNewGraph()));

        JScrollPane scroll = new JScrollPane(buttons);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Cria a barra lateral direita (sidebar direito).
     *
     * <p>Contém:
     * <ul>
     *   <li>Painel de resultados com HTML renderizado (txtResults)</li>
     *   <li>Lista de nós de topo destacados por algoritmos (lstTopNodes)</li>
     * </ul>
     *
     * <p>Layout:
     * <ul>
     *   <li>JSplitPane vertical para redimensionamento dinâmico</li>
     *   <li>Scroll para ambos os painéis em caso de overflow</li>
     * </ul>
     *
     * @return painel configurado com sidebar direito
     */
    private JPanel createRightSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 17, 23));
        panel.setPreferredSize(new Dimension(320, getHeight()));

        txtResults = new JTextPane();
        txtResults.setContentType("text/html");
        txtResults.setEditable(false);
        txtResults.setBackground(new Color(22, 27, 34));
        txtResults.setForeground(new Color(230, 237, 243));

        JScrollPane scroll = new JScrollPane(txtResults);
        scroll.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        topNodesListModel = new DefaultListModel<>();
        lstTopNodes = new JList<>(topNodesListModel);
        lstTopNodes.setBackground(new Color(22, 27, 34));
        lstTopNodes.setForeground(new Color(230, 237, 243));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, new JScrollPane(lstTopNodes));
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Factory method para criar botões de ação estilizados.
     *
     * @param text rótulo do botão
     * @param listener callback quando botão é clicado
     * @return botão configurado do tipo {@link MenuActionButton}
     */
    private MenuActionButton createButton(String text, java.awt.event.ActionListener listener) {
        MenuActionButton btn = new MenuActionButton(text);
        btn.addActionListener(listener);
        return btn;
    }

    /**
     * Helper para executar tarefas de negócio com validação de pré-condição.
     *
     * <p>Verifica se um grafo está carregado antes de executar a tarefa.
     *
     * @param task tarefa a ser executada (Runnable)
     * @see #checkGraph()
     */
    private void run(Runnable task) {
        if (checkGraph()) task.run();
    }

    /**
     * Valida se um grafo está carregado atualmente.
     *
     * <p>Exibe mensagem de erro no painel de resultados se nenhum grafo foi carregado.
     *
     * @return {@code true} se grafo está carregado, {@code false} caso contrário
     */
    private boolean checkGraph() {
        if (state.getGraph() == null) {
            logHTML("<font color=\"#ff6b6b\">Carregue um grafo primeiro.</font>");
            return false;
        }
        return true;
    }

    /**
     * Inicializa os handlers de operação (GraphLoadingHandler, AlgorithmHandler, SocialNetworkHandler).
     *
     * <p>Cria um {@link GraphLoadingHandler} com listener que:
     * <ul>
     *   <li>Atualiza estado da aplicação ({@link GraphUIState}) quando grafo é carregado</li>
     *   <li>Reinicializa handlers de algoritmo e rede social com novo grafo</li>
     *   <li>Atualiza visualização de painel (GraphPanel)</li>
     *   <li>Atualiza estatísticas exibidas</li>
     * </ul>
     *
     * @see GraphLoadingHandler
     * @see GraphUIState#setGraph(Graph, boolean)
     */
    private void initializeHandlers() {
        loadingHandler = new GraphLoadingHandler(new GraphLoadingHandler.GraphLoadListener() {
            public void onGraphLoaded(Graph g, boolean fromFile) {
                state.setGraph(g, fromFile);
                algorithmHandler = new AlgorithmHandler(g, state.getAlgorithms(),
                    (html, topIndices) -> { logHTML(html); updateTopNodes(topIndices); });
                socialHandler = new SocialNetworkHandler(g, state.getSocialNetwork(),
                    (html, path) -> { logHTML(html); graphPanel.setHighlightedPath(path); });
                graphPanel.setGraph(g, state.getAlgorithms());
                graphPanel.setSelectionListener(idx -> {/* handle selection */});
                updateStats();
            }
            public void onError(String msg) { logHTML("<font color=\"#ff6b6b\">" + msg + "</font>"); }
        });
    }

    /**
     * Atualiza a lista de nós destacados (top nodes) no painel direito.
     *
     * <p>Popula {@link #lstTopNodes} com nomes dos nós fornecidos,
     * mantendo mapeamento de índices em {@link #topNodesIndices}
     * para referência futura (ex: seleção na visualização).
     *
     * @param indices lista de índices de vértices a destacar (pode ser nulo)
     */
    private void updateTopNodes(List<Integer> indices) {
        topNodesListModel.clear();
        topNodesIndices.clear();
        if (indices != null && state.getGraph() != null) {
            for (Integer idx : indices) {
                topNodesListModel.addElement(state.getGraph().getName(idx));
                topNodesIndices.add(idx);
            }
        }
    }

    /**
     * Atualiza e exibe estatísticas do grafo carregado.
     *
     * <p>Calcula e exibe:
     * <ul>
     *   <li>Número de vértices</li>
     *   <li>Número de arestas (soma dos graus de saída)</li>
     * </ul>
     *
     * <p>Esta informação é exibida no painel de resultados (sidebar direito).
     */
    private void updateStats() {
        if (state.getGraph() == null) return;
        long edges = 0;
        for (int i = 0; i < state.getGraph().numVertices; i++) {
            edges += state.getGraph().neighbors(i).size();
        }
        logHTML("<b>Grafo Carregado</b><br>Vértices: " + state.getGraph().numVertices + "<br>Arestas: " + edges);
    }

    /**
     * Exibe conteúdo HTML no painel de resultados.
     *
     * <p>Envolve HTML fornecido em tags padrão (html, body) com estilo consistente
     * (fonte: Segoe UI, cor: #e6edf3).
     *
     * @param html conteúdo HTML a exibir (pode incluir tags de formatação)
     */
    private void logHTML(String html) {
        txtResults.setText("<html><body style=\"font-family: Segoe UI; color: #e6edf3;\">" + html + "</body></html>");
    }

    /**
     * Limpa o grafo carregado e reseta a interface.
     *
     * <p>Executa {@link GraphUIState#clear()} e remove visualização
     * do painel central.
     */
    private void handleNewGraph() {
        state.clear();
        graphPanel.setGraph(null, null);
        logHTML("<b>Grafo limpo.</b>");
    }

    /**
     * Exibe diálogo para gerar grafo aleatório.
     *
     * <p>Solicita ao usuário:
     * <ul>
     *   <li>Número de vértices (padrão: 100)</li>
     *   <li>Número de arestas (padrão: 300)</li>
     * </ul>
     *
     * <p>Delega geração ao {@link GraphLoadingHandler}.
     */
    private void showRandomGraphDialog() {
        String[] opts = {"100", "300"};
        String n = JOptionPane.showInputDialog(this, "Vértices:", "100");
        String m = JOptionPane.showInputDialog(this, "Arestas:", "300");
        if (n != null && m != null) {
            loadingHandler.generateRandomGraph(this, Integer.parseInt(n), Integer.parseInt(m), true);
        }
    }

    /**
     * Exibe diálogo de arquivo para carregar grafo no formato Pajek (.net).
     *
     * <p>Abre JFileChooser na pasta {@code pajek/input}.
     * Delega carregamento ao {@link GraphLoadingHandler}.
     */
    private void showLoadPajekDialog() {
        JFileChooser fc = new JFileChooser("pajek/input");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadingHandler.loadPajek(this, fc.getSelectedFile().getName());
        }
    }

    /**
     * Exibe diálogo para recomendação de seguidor.
     *
     * <p>Solicita:
     * <ul>
     *   <li>Nome da pessoa origem</li>
     *   <li>Nome da pessoa destino</li>
     * </ul>
     *
     * <p>Delega processamento ao {@link SocialNetworkHandler}.
     */
    private void showRecommendationDialog() {
        String src = JOptionPane.showInputDialog(this, "Pessoa origem:");
        String tgt = JOptionPane.showInputDialog(this, "Pessoa destino:");
        if (src != null && tgt != null) socialHandler.recommendFollower(src, tgt);
    }

    /**
     * Exibe conexões (seguidores e seguindo) de um nó selecionado.
     *
     * <p>Pré-condição: Um nó deve estar selecionado na visualização.
     * Delega processamento ao {@link SocialNetworkHandler}.
     */
    private void showFollowersDialog() {
        int idx = graphPanel.getSelectedNodeIndex();
        if (idx == -1) { logHTML("Selecione um nó."); return; }
        socialHandler.showFollowers(idx);
    }

    /**
     * Exibe diálogo para exportar grafo carregado.
     *
     * <p>Solicita nome de arquivo e exporta para formato Pajek
     * na pasta {@code pajek/output} via {@link graph.io.PajekIO}.
     *
     * @see graph.io.PajekIO#export(Graph, String)
     */
    private void showExportDialog() {
        String fn = JOptionPane.showInputDialog(this, "Nome do arquivo:");
        if (fn != null) {
            graph.io.PajekIO.export(state.getGraph(), fn);
            logHTML("<b>Exportado:</b> pajek/output/" + fn);
        }
    }

    /**
     * Salva sessão atual (grafo carregado) para arquivo.
     *
     * <p>Arquivo é salvo como {@code pajek/output/session_save.net}
     * usando {@link graph.io.PajekIO}.
     */
    private void performSaveSession() {
        graph.io.PajekIO.export(state.getGraph(), "session_save.net");
        logHTML("<b>Sessão salva.</b>");
    }

    /**
     * Carrega sessão anterior (grafo salvo).
     *
     * <p>Copia arquivo de sessão salva ({@code pajek/output/session_save.net})
     * para pasta de entrada ({@code pajek/input}) e importa via
     * {@link graph.io.PajekIO}.
     *
     * <p>Se arquivo não existir ou erro ocorrer, exibe mensagem de erro.
     */
    private void performLoadSession() {
        try {
            File src = new File("pajek/output/session_save.net");
            if (src.exists()) {
                File dest = new File("pajek/input/session_save.net");
                java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                Graph g = graph.io.PajekIO.importFrom("session_save.net");
                if (g != null) { state.setGraph(g, true); graphPanel.setGraph(g, state.getAlgorithms()); }
            }
        } catch (Exception e) { logHTML("<font color=\"#ff6b6b\">Erro ao carregar.</font>"); }
    }

    /**
     * Método principal (entry point) da aplicação.
     *
     * <p>Cria instância de {@link GraphGUI} e a torna visível.
     * Execução é feita em Event Dispatch Thread (EDT) via
     * {@link SwingUtilities#invokeLater(Runnable)} para thread-safety.
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphGUI().setVisible(true));
    }
}
