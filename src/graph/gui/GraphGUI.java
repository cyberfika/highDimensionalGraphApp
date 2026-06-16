package graph.gui;

import graph.algorithm.GraphAlgorithms;
import graph.domain.SocialNetwork;
import graph.generator.GraphGenerator;
import graph.io.PajekIO;
import graph.model.Graph;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main GUI window for the GraphNet Analyzer application.
 * Replaces the console interface with a high-fidelity dark theme layout.
 */
public class GraphGUI extends JFrame {
    private Graph graph;
    private GraphAlgorithms algs;
    private SocialNetwork social;
    private boolean wasLoaded = false;
    private long lastExecutionTime = 0;

    // GUI Components
    private final GraphPanel graphPanel;

    private JTextPane txtResults;
    private JPanel pnlSelectedNode;
    private JLabel lblNodeId;
    private JLabel lblNodeName;
    private JLabel lblNodeInDegree;
    private JLabel lblNodeOutDegree;
    private JLabel lblNodeCentrality;
    private JLabel lblNodeCommunity;
    private JButton btnFocusMode;

    private DefaultListModel<String> topNodesListModel;
    private JList<String> lstTopNodes;
    private List<Integer> topNodesIndices = new ArrayList<>();

    // Colors matching the design tokens
    private static final Color COLOR_BG_DARK = new Color(13, 17, 23);
    private static final Color COLOR_BG_SIDEBAR = new Color(15, 17, 23);
    private static final Color COLOR_BG_HEADER = new Color(22, 27, 34);
    private static final Color COLOR_BORDER = new Color(48, 54, 61);
    private static final Color COLOR_TEXT_LIGHT = new Color(230, 237, 243);
    private static final Color COLOR_TEXT_MUTED = new Color(139, 148, 158);
    private static final Color COLOR_ACCENT_BLUE = new Color(88, 166, 255);
    private static final Color COLOR_ACCENT_ORANGE = new Color(251, 133, 0);

    public GraphGUI() {
        setTitle("GraphNet Analyzer");
        setSize(1250, 850);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configure system anti-aliasing properties for text rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Main Layout
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.setBackground(COLOR_BG_DARK);

        // 1. TOP HEADER
        JPanel header = createHeaderPanel();
        container.add(header, BorderLayout.NORTH);

        // 2. CENTER PANEL (GRAPH VISUALIZER)
        graphPanel = new GraphPanel();
        graphPanel.setSelectionListener(this::handleNodeSelected);
        container.add(graphPanel, BorderLayout.CENTER);

        // 3. LEFT SIDEBAR (SCROLLABLE ACCORDIONS)
        JPanel leftSidebar = createLeftSidebar();
        container.add(leftSidebar, BorderLayout.WEST);

        // 4. RIGHT SIDEBAR (SELECTED NODE, RESULTS, TOP NODES)
        JPanel rightSidebar = createRightSidebar();
        container.add(rightSidebar, BorderLayout.EAST);

        // Initialize status labels (handled dynamically in updateStats)

        // Set up the default view state
        updateStats();
        logHTML("<b>Bem-vindo ao GraphNet Analyzer!</b><br><br>Gere uma nova rede social ou carregue um grafo a partir do menu lateral para começar a análise.");
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(COLOR_BORDER);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        header.setBackground(COLOR_BG_HEADER);
        header.setPreferredSize(new Dimension(getWidth(), 64));
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // Left brand
        JPanel pnlBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlBrand.setOpaque(false);
        
        // G Logo Box
        JLabel lblLogo = new JLabel("G") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_ACCENT_ORANGE, getWidth(), getHeight(), new Color(255, 183, 3));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblLogo.setOpaque(false);
        lblLogo.setPreferredSize(new Dimension(32, 32));
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(new Color(15, 17, 23));
        pnlBrand.add(lblLogo);

        JLabel lblTitle = new JLabel("GraphNet Analyzer");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_TEXT_LIGHT);
        pnlBrand.add(lblTitle);

        header.add(pnlBrand, BorderLayout.WEST);

        // Center Search Bar
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 16));
        pnlSearch.setOpaque(false);

        JTextField txtSearch = new JTextField("Buscar usuário...");
        txtSearch.setPreferredSize(new Dimension(200, 28));
        txtSearch.setBackground(COLOR_BG_DARK);
        txtSearch.setForeground(COLOR_TEXT_MUTED);
        txtSearch.setCaretColor(COLOR_TEXT_LIGHT);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Focus listeners for placeholder text
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().equals("Buscar usuário...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(COLOR_TEXT_LIGHT);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Buscar usuário...");
                    txtSearch.setForeground(COLOR_TEXT_MUTED);
                }
            }
        });

        JButton btnSearch = new JButton("Buscar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 36, 44));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSearch.setPreferredSize(new Dimension(75, 28));
        btnSearch.setForeground(COLOR_TEXT_LIGHT);
        btnSearch.setFocusPainted(false);
        btnSearch.setContentAreaFilled(false);
        btnSearch.setOpaque(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));

        Runnable searchAction = () -> {
            if (graph == null) {
                showNoGraphWarning();
                return;
            }
            String query = txtSearch.getText().trim();
            if (query.isEmpty() || query.equals("Buscar usuário...")) return;
            int foundIdx = graph.findByName(query);
            if (foundIdx != -1) {
                graphPanel.setSelectedNodeIndex(foundIdx);
                graphPanel.resetZoomAndPan();
                logHTML("<b>Busca de Vértice</b><br>Usuário <b>" + graph.getName(foundIdx) + "</b> encontrado e selecionado!");
            } else {
                logHTML("<b>Busca de Vértice</b><br><font color=\"#ff6b6b\">Usuário '" + query + "' não encontrado no grafo.</font>");
            }
        };

        btnSearch.addActionListener(e -> searchAction.run());
        txtSearch.addActionListener(e -> searchAction.run());

        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);
        header.add(pnlSearch, BorderLayout.CENTER);

        // Right Version Badge
        JPanel pnlVersion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 18));
        pnlVersion.setOpaque(false);
        JLabel lblVersion = new JLabel("v1.0") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BG_SIDEBAR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblVersion.setOpaque(false);
        lblVersion.setPreferredSize(new Dimension(48, 22));
        lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVersion.setForeground(COLOR_TEXT_MUTED);
        pnlVersion.add(lblVersion);

        header.add(pnlVersion, BorderLayout.EAST);
        return header;
    }

    private JPanel createLeftSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(COLOR_BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDER));

        // Novo Grafo Button Container
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        pnlTop.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        
        PrimaryButton btnNew = new PrimaryButton("Novo Grafo", new VectorIcon(VectorIcon.Type.PLUS, 12, new Color(15, 17, 23)));
        btnNew.addActionListener(e -> handleNewGraph());
        pnlTop.add(btnNew, BorderLayout.CENTER);
        sidebar.add(pnlTop, BorderLayout.NORTH);

        // Accordion panels inside a scrollable container
        JPanel pnlAccordion = new JPanel();
        pnlAccordion.setLayout(new BoxLayout(pnlAccordion, BoxLayout.Y_AXIS));
        pnlAccordion.setOpaque(false);

        // 1. GERACAO ACCORDION
        JPanel pnlGen = new JPanel();
        pnlGen.setLayout(new BoxLayout(pnlGen, BoxLayout.Y_AXIS));
        pnlGen.setOpaque(false);
        pnlGen.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        MenuActionButton btnGenSocial = new MenuActionButton("Gerar Rede Social (5k)");
        btnGenSocial.addActionListener(e -> generateSocialNetwork());
        MenuActionButton btnGenRandom = new MenuActionButton("Grafo Aleatório");
        btnGenRandom.addActionListener(e -> generateRandomGraph());
        MenuActionButton btnLoadPajek = new MenuActionButton("Carregar Pajek");
        btnLoadPajek.addActionListener(e -> loadPajek());
        pnlGen.add(btnGenSocial);
        pnlGen.add(btnGenRandom);
        pnlGen.add(btnLoadPajek);
        
        CollapsiblePanel accGen = new CollapsiblePanel("GERAÇÃO", new VectorIcon(VectorIcon.Type.GENERATE, 14, COLOR_ACCENT_BLUE), pnlGen, true);
        pnlAccordion.add(accGen);

        // 2. ANALISE ESTRUTURAL ACCORDION
        JPanel pnlAnal = new JPanel();
        pnlAnal.setLayout(new BoxLayout(pnlAnal, BoxLayout.Y_AXIS));
        pnlAnal.setOpaque(false);
        pnlAnal.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        MenuActionButton btnConnectivity = new MenuActionButton("Conectividade");
        btnConnectivity.addActionListener(e -> runConnectivity());
        MenuActionButton btnComponents = new MenuActionButton("Componentes");
        btnComponents.addActionListener(e -> runComponents());
        MenuActionButton btnCycles = new MenuActionButton("Detectar Ciclos");
        btnCycles.addActionListener(e -> runCycles());
        MenuActionButton btnEulerian = new MenuActionButton("Euleriano?");
        btnEulerian.addActionListener(e -> runEulerian());
        pnlAnal.add(btnConnectivity);
        pnlAnal.add(btnComponents);
        pnlAnal.add(btnCycles);
        pnlAnal.add(btnEulerian);

        CollapsiblePanel accAnal = new CollapsiblePanel("ANÁLISE ESTRUTURAL", new VectorIcon(VectorIcon.Type.ANALYZE, 14, COLOR_ACCENT_BLUE), pnlAnal, false);
        pnlAccordion.add(accAnal);

        // 3. CENTRALIDADE ACCORDION
        JPanel pnlCent = new JPanel();
        pnlCent.setLayout(new BoxLayout(pnlCent, BoxLayout.Y_AXIS));
        pnlCent.setOpaque(false);
        pnlCent.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        MenuActionButton btnCloseness = new MenuActionButton("Proximidade");
        btnCloseness.addActionListener(e -> runCloseness());
        MenuActionButton btnBetweenness = new MenuActionButton("Intermediação");
        btnBetweenness.addActionListener(e -> runBetweenness());
        pnlCent.add(btnCloseness);
        pnlCent.add(btnBetweenness);

        CollapsiblePanel accCent = new CollapsiblePanel("CENTRALIDADE", new VectorIcon(VectorIcon.Type.CENTRALITY, 14, COLOR_ACCENT_BLUE), pnlCent, false);
        pnlAccordion.add(accCent);

        // 4. REDE SOCIAL ACCORDION
        JPanel pnlSocial = new JPanel();
        pnlSocial.setLayout(new BoxLayout(pnlSocial, BoxLayout.Y_AXIS));
        pnlSocial.setOpaque(false);
        pnlSocial.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        MenuActionButton btnRecommend = new MenuActionButton("Recomendação");
        btnRecommend.addActionListener(e -> runRecommendation());
        MenuActionButton btnFollowers = new MenuActionButton("Seguidores");
        btnFollowers.addActionListener(e -> runFollowersAction());
        pnlSocial.add(btnRecommend);
        pnlSocial.add(btnFollowers);

        CollapsiblePanel accSocial = new CollapsiblePanel("REDE SOCIAL", new VectorIcon(VectorIcon.Type.SOCIAL, 14, COLOR_ACCENT_BLUE), pnlSocial, false);
        pnlAccordion.add(accSocial);

        // 5. ARQUIVOS ACCORDION
        JPanel pnlFiles = new JPanel();
        pnlFiles.setLayout(new BoxLayout(pnlFiles, BoxLayout.Y_AXIS));
        pnlFiles.setOpaque(false);
        pnlFiles.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        MenuActionButton btnExportPajek = new MenuActionButton("Exportar Pajek");
        btnExportPajek.addActionListener(e -> exportPajek());
        MenuActionButton btnSaveSession = new MenuActionButton("Salvar Sessão");
        btnSaveSession.addActionListener(e -> saveSession());
        MenuActionButton btnLoadSession = new MenuActionButton("Carregar Sessão");
        btnLoadSession.addActionListener(e -> loadSession());
        pnlFiles.add(btnExportPajek);
        pnlFiles.add(btnSaveSession);
        pnlFiles.add(btnLoadSession);

        CollapsiblePanel accFiles = new CollapsiblePanel("ARQUIVOS", new VectorIcon(VectorIcon.Type.FILE, 14, COLOR_ACCENT_BLUE), pnlFiles, false);
        pnlAccordion.add(accFiles);

        // Wrap accordion in scroll pane to prevent overflow
        JScrollPane scrollAccordion = new JScrollPane(pnlAccordion);
        scrollAccordion.setBorder(null);
        scrollAccordion.setOpaque(true);
        scrollAccordion.setBackground(COLOR_BG_SIDEBAR);
        scrollAccordion.getViewport().setOpaque(true);
        scrollAccordion.getViewport().setBackground(COLOR_BG_SIDEBAR);
        scrollAccordion.getVerticalScrollBar().setUnitIncrement(12);
        
        pnlAccordion.setOpaque(true);
        pnlAccordion.setBackground(COLOR_BG_SIDEBAR);
        
        sidebar.add(scrollAccordion, BorderLayout.CENTER);

        // STATS BAR AT BOTTOM OF LEFT SIDEBAR
        JPanel pnlStats = createStatsPanel();
        sidebar.add(pnlStats, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 6));
        panel.setBackground(COLOR_BG_SIDEBAR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        // Vertices
        JPanel pnlV = new JPanel(new BorderLayout());
        pnlV.setOpaque(false);
        JLabel iconV = new JLabel(new VectorIcon(VectorIcon.Type.VERTEX, 11, COLOR_ACCENT_BLUE));
        JLabel lblTitleV = new JLabel(" Vértices: ");
        lblTitleV.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitleV.setForeground(COLOR_TEXT_MUTED);
        JPanel leftV = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftV.setOpaque(false);
        leftV.add(iconV);
        leftV.add(lblTitleV);
        lblVerticesVal = new JLabel("0");
        lblVerticesVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVerticesVal.setForeground(COLOR_ACCENT_BLUE);
        pnlV.add(leftV, BorderLayout.WEST);
        pnlV.add(lblVerticesVal, BorderLayout.EAST);
        panel.add(pnlV);

        // Edges
        JPanel pnlE = new JPanel(new BorderLayout());
        pnlE.setOpaque(false);
        JLabel iconE = new JLabel(new VectorIcon(VectorIcon.Type.EDGE, 11, COLOR_ACCENT_BLUE));
        JLabel lblTitleE = new JLabel(" Arestas: ");
        lblTitleE.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitleE.setForeground(COLOR_TEXT_MUTED);
        JPanel leftE = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftE.setOpaque(false);
        leftE.add(iconE);
        leftE.add(lblTitleE);
        lblEdgesVal = new JLabel("0");
        lblEdgesVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEdgesVal.setForeground(COLOR_ACCENT_BLUE);
        pnlE.add(leftE, BorderLayout.WEST);
        pnlE.add(lblEdgesVal, BorderLayout.EAST);
        panel.add(pnlE);

        // Components
        JPanel pnlC = new JPanel(new BorderLayout());
        pnlC.setOpaque(false);
        JLabel iconC = new JLabel(new VectorIcon(VectorIcon.Type.COMPONENT, 11, COLOR_ACCENT_BLUE));
        JLabel lblTitleC = new JLabel(" Componentes: ");
        lblTitleC.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitleC.setForeground(COLOR_TEXT_MUTED);
        JPanel leftC = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftC.setOpaque(false);
        leftC.add(iconC);
        leftC.add(lblTitleC);
        lblComponentsVal = new JLabel("—");
        lblComponentsVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblComponentsVal.setForeground(COLOR_ACCENT_BLUE);
        pnlC.add(leftC, BorderLayout.WEST);
        pnlC.add(lblComponentsVal, BorderLayout.EAST);
        panel.add(pnlC);

        // Execution time
        JPanel pnlT = new JPanel(new BorderLayout());
        pnlT.setOpaque(false);
        JLabel iconT = new JLabel(new VectorIcon(VectorIcon.Type.CLOCK, 11, COLOR_TEXT_MUTED));
        JLabel lblTitleT = new JLabel(" Tempo: ");
        lblTitleT.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitleT.setForeground(COLOR_TEXT_MUTED);
        JPanel leftT = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftT.setOpaque(false);
        leftT.add(iconT);
        leftT.add(lblTitleT);
        lblTimeVal = new JLabel("—");
        lblTimeVal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTimeVal.setForeground(COLOR_TEXT_MUTED);
        pnlT.add(leftT, BorderLayout.WEST);
        pnlT.add(lblTimeVal, BorderLayout.EAST);
        panel.add(pnlT);

        return panel;
    }

    private JLabel lblVerticesVal;
    private JLabel lblEdgesVal;
    private JLabel lblComponentsVal;
    private JLabel lblTimeVal;

    private JPanel createRightSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(320, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDER));

        // 1. NODE SELECTION PANEL (top-pinned)
        pnlSelectedNode = createSelectedNodePanel();
        pnlSelectedNode.setVisible(false); // Hidden until a node is selected
        sidebar.add(pnlSelectedNode);

        // 2. RESULTS PANEL
        JPanel pnlResults = createResultsPanel();
        sidebar.add(pnlResults);

        // 3. TOP NODES PANEL
        JPanel pnlTopNodes = createTopNodesPanel();
        sidebar.add(pnlTopNodes);

        return sidebar;
    }

    private JPanel createSelectedNodePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_BG_SIDEBAR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        panel.setMaximumSize(new Dimension(320, 240));

        JLabel lblSectionHeader = new JLabel("NÓ SELECIONADO");
        lblSectionHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSectionHeader.setForeground(COLOR_TEXT_MUTED);
        lblSectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblSectionHeader);
        panel.add(Box.createVerticalStrut(12));

        // Avatar + Name Block
        JPanel avatarBlock = new JPanel(new BorderLayout(12, 0));
        avatarBlock.setOpaque(false);
        avatarBlock.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Rounded Avatar
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_ACCENT_ORANGE, getWidth(), getHeight(), new Color(255, 183, 3));
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(44, 44));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        avatar.setForeground(new Color(15, 17, 23));
        
        lblNodeId = new JLabel("0");
        lblNodeId.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNodeId.setForeground(new Color(15, 17, 23));
        avatar.setLayout(new BorderLayout());
        avatar.add(lblNodeId, BorderLayout.CENTER);
        lblNodeId.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel infoBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        infoBlock.setOpaque(false);
        lblNodeName = new JLabel("Nome");
        lblNodeName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNodeName.setForeground(COLOR_TEXT_LIGHT);
        
        JLabel lblDetails = new JLabel("Detalhes do Vértice");
        lblDetails.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDetails.setForeground(COLOR_TEXT_MUTED);
        
        infoBlock.add(lblNodeName);
        infoBlock.add(lblDetails);

        avatarBlock.add(avatar, BorderLayout.WEST);
        avatarBlock.add(infoBlock, BorderLayout.CENTER);
        panel.add(avatarBlock);
        panel.add(Box.createVerticalStrut(14));

        // Details Grid
        JPanel grid = new JPanel(new GridLayout(4, 2, 4, 6));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(createMutedLabel("Comunidade:"));
        lblNodeCommunity = createBoldLabel("—");
        grid.add(lblNodeCommunity);

        grid.add(createMutedLabel("Grau Saída:"));
        lblNodeOutDegree = createBoldLabel("0");
        grid.add(lblNodeOutDegree);

        grid.add(createMutedLabel("Grau Entrada:"));
        lblNodeInDegree = createBoldLabel("0");
        grid.add(lblNodeInDegree);

        grid.add(createMutedLabel("Centralidade:"));
        lblNodeCentrality = createBoldLabel("Não calc.");
        lblNodeCentrality.setForeground(COLOR_ACCENT_ORANGE);
        grid.add(lblNodeCentrality);

        panel.add(grid);
        panel.add(Box.createVerticalStrut(12));

        // Focus Neighbors Toggle Button
        btnFocusMode = new JButton("Focar Vizinhança") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 36, 44));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnFocusMode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFocusMode.setForeground(COLOR_TEXT_LIGHT);
        btnFocusMode.setFocusPainted(false);
        btnFocusMode.setContentAreaFilled(false);
        btnFocusMode.setOpaque(false);
        btnFocusMode.setBorderPainted(false);
        btnFocusMode.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFocusMode.setPreferredSize(new Dimension(288, 30));
        btnFocusMode.setMaximumSize(new Dimension(288, 30));
        btnFocusMode.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnFocusMode.addActionListener(e -> toggleFocusMode());
        panel.add(btnFocusMode);

        return panel;
    }

    private JLabel createMutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(COLOR_TEXT_MUTED);
        return l;
    }

    private JLabel createBoldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(COLOR_ACCENT_BLUE);
        return l;
    }

    private void toggleFocusMode() {
        if (graphPanel.isGlobalViewMode()) {
            graphPanel.setGlobalViewMode(false);
            btnFocusMode.setText("Ver Rede Global");
            logHTML("<b>Foco Vizinhança Ativado</b><br>Exibindo apenas conexões diretas do nó selecionado.");
        } else {
            graphPanel.setGlobalViewMode(true);
            btnFocusMode.setText("Focar Vizinhança");
            logHTML("<b>Retornado para Rede Global</b>");
        }
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG_SIDEBAR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        panel.setPreferredSize(new Dimension(320, 300));
        panel.setMaximumSize(new Dimension(320, 400));

        JLabel lblSectionHeader = new JLabel("RESULTADOS");
        lblSectionHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSectionHeader.setForeground(COLOR_TEXT_MUTED);
        panel.add(lblSectionHeader, BorderLayout.NORTH);

        txtResults = new JTextPane();
        txtResults.setContentType("text/html");
        txtResults.setEditable(false);
        txtResults.setBackground(new Color(22, 27, 34));
        txtResults.setForeground(COLOR_TEXT_LIGHT);
        txtResults.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(txtResults);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scroll.setBackground(new Color(22, 27, 34));
        scroll.getViewport().setBackground(new Color(22, 27, 34));
        
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        container.add(scroll, BorderLayout.CENTER);
        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTopNodesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG_SIDEBAR);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.setPreferredSize(new Dimension(320, 200));

        JLabel lblSectionHeader = new JLabel("TOP NÓS (CENTRALIDADE)");
        lblSectionHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSectionHeader.setForeground(COLOR_TEXT_MUTED);
        panel.add(lblSectionHeader, BorderLayout.NORTH);

        topNodesListModel = new DefaultListModel<>();
        lstTopNodes = new JList<>(topNodesListModel);
        lstTopNodes.setBackground(new Color(22, 27, 34));
        lstTopNodes.setForeground(COLOR_TEXT_LIGHT);
        lstTopNodes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lstTopNodes.setSelectionBackground(new Color(88, 166, 255, 40));
        lstTopNodes.setSelectionForeground(COLOR_ACCENT_BLUE);
        lstTopNodes.setFixedCellHeight(30);
        lstTopNodes.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

        lstTopNodes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = lstTopNodes.getSelectedIndex();
                if (idx != -1 && idx < topNodesIndices.size()) {
                    int nodeIdx = topNodesIndices.get(idx);
                    graphPanel.setSelectedNodeIndex(nodeIdx);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(lstTopNodes);
        scroll.setBorder(null);
        
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        container.add(scroll, BorderLayout.CENTER);
        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private void logHTML(String htmlContent) {
        String styledHtml = "<html><body style=\"font-family: -apple-system, Segoe UI, sans-serif; font-size: 10px; color: #e6edf3; margin: 0;\">"
                + htmlContent + "</body></html>";
        txtResults.setText(styledHtml);
        txtResults.setCaretPosition(0);
    }

    private void setGraph(Graph g, boolean loaded) {
        this.graph = g;
        this.wasLoaded = loaded;
        if (g != null) {
            this.algs = new GraphAlgorithms(g);
            this.social = new SocialNetwork(g);
        } else {
            this.algs = null;
            this.social = null;
        }

        graphPanel.setGraph(g, algs);
        updateStats();
        
        // Reset selected node panel
        pnlSelectedNode.setVisible(false);
        topNodesListModel.clear();
        topNodesIndices.clear();
    }

    private void updateStats() {
        if (graph == null) {
            lblVerticesVal.setText("0");
            lblEdgesVal.setText("0");
            lblComponentsVal.setText("—");
            lblTimeVal.setText("—");
            return;
        }

        lblVerticesVal.setText(String.format("%,d", graph.numVertices));
        
        // Count edges
        long edgeCount = 0;
        for (int i = 0; i < graph.numVertices; i++) {
            edgeCount += graph.neighbors(i).size();
        }
        if (!graph.directed) {
            edgeCount /= 2; // Undirected edges are double-represented
        }
        lblEdgesVal.setText(String.format("%,d", edgeCount));

        // Components count (quick check if computed, otherwise lazy load)
        if (algs != null) {
            lblComponentsVal.setText(String.format("%,d", algs.getComponents().size()));
        } else {
            lblComponentsVal.setText("—");
        }

        if (lastExecutionTime > 0) {
            lblTimeVal.setText(lastExecutionTime + " ms");
        } else {
            lblTimeVal.setText("—");
        }
    }

    private void handleNodeSelected(int idx) {
        if (graph == null || idx < 0 || idx >= graph.numVertices) {
            pnlSelectedNode.setVisible(false);
            return;
        }

        lblNodeId.setText(String.valueOf(idx + 1));
        lblNodeName.setText(graph.getName(idx));
        
        // Communities
        if (graphPanel.isGlobalViewMode()) {
            lblNodeCommunity.setText(String.valueOf(graphPanel.getSelectedNodeIndex() != -1 ? "ID " + (graphPanel.getSelectedNodeIndex() % 10 + 1) : "—"));
        } else {
            lblNodeCommunity.setText("—");
        }

        // Out Degree
        lblNodeOutDegree.setText(String.valueOf(graph.neighbors(idx).size()));

        // In Degree
        int inDegree = 0;
        for (int u = 0; u < graph.numVertices; u++) {
            if (u == idx) continue;
            for (int[] edge : graph.neighbors(u)) {
                if (edge[0] == idx) {
                    inDegree++;
                    break;
                }
            }
        }
        lblNodeInDegree.setText(String.valueOf(inDegree));

        // Centrality
        lblNodeCentrality.setText("Não calc.");

        pnlSelectedNode.setVisible(true);
        pnlSelectedNode.revalidate();
        pnlSelectedNode.repaint();
    }

    // -------------------------------------------------------------------------
    // ACTIONS
    // -------------------------------------------------------------------------

    private void handleNewGraph() {
        if (graph != null) {
            int option = JOptionPane.showConfirmDialog(this,
                "Deseja realmente limpar o grafo atual e reiniciar?",
                "Novo Grafo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option != JOptionPane.YES_OPTION) return;
        }
        setGraph(null, false);
        lastExecutionTime = 0;
        logHTML("<b>Grafo limpo.</b><br>Pronto para receber novos dados.");
    }

    private void generateSocialNetwork() {
        ProgressWorker worker = new ProgressWorker("Gerando Rede Social...") {
            private Graph resultGraph;
            @Override
            protected Void doInBackground() throws Exception {
                long start = System.currentTimeMillis();
                resultGraph = GraphGenerator.generateSocialNetwork("names.txt");
                lastExecutionTime = System.currentTimeMillis() - start;
                return null;
            }
            @Override
            protected void done() {
                super.done();
                if (resultGraph != null) {
                    setGraph(resultGraph, false);
                    logHTML("<b>Rede Social Gerada com Sucesso!</b><br>" +
                            "• Vértices: " + resultGraph.numVertices + "<br>" +
                            "• Tempo de geração: " + lastExecutionTime + " ms<br><br>" +
                            "Cada pessoa foi configurada para seguir entre 5 e 8 outras, simulando conexões de rede social reais.");
                } else {
                    logHTML("<font color=\"#ff6b6b\"><b>Erro ao carregar data/names.txt.</b></font><br>Verifique se o arquivo existe na pasta do projeto.");
                }
            }
        };
        worker.executeWorker();
    }

    private void generateRandomGraph() {
        // Styled inputs inside JDialog
        JDialog dlg = new JDialog(this, "Gerar Grafo Aleatório", true);
        dlg.setLayout(new GridBagLayout());
        dlg.getContentPane().setBackground(COLOR_BG_HEADER);
        dlg.setSize(320, 220);
        dlg.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 12, 8, 12);

        JLabel lblV = new JLabel("Número de Vértices:");
        lblV.setForeground(COLOR_TEXT_LIGHT);
        JTextField txtV = new JTextField("100");
        txtV.setBackground(COLOR_BG_DARK);
        txtV.setForeground(COLOR_TEXT_LIGHT);
        txtV.setCaretColor(COLOR_TEXT_LIGHT);
        txtV.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

        JLabel lblE = new JLabel("Número de Arestas:");
        lblE.setForeground(COLOR_TEXT_LIGHT);
        JTextField txtE = new JTextField("300");
        txtE.setBackground(COLOR_BG_DARK);
        txtE.setForeground(COLOR_TEXT_LIGHT);
        txtE.setCaretColor(COLOR_TEXT_LIGHT);
        txtE.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

        JCheckBox chkConn = new JCheckBox("Garantir Conectividade");
        chkConn.setSelected(true);
        chkConn.setOpaque(false);
        chkConn.setForeground(COLOR_TEXT_LIGHT);

        PrimaryButton btnConfirm = new PrimaryButton("Gerar", null);
        btnConfirm.addActionListener(e -> {
            try {
                int n = Integer.parseInt(txtV.getText().trim());
                int m = Integer.parseInt(txtE.getText().trim());
                boolean conn = chkConn.isSelected();
                dlg.dispose();

                ProgressWorker worker = new ProgressWorker("Gerando Grafo Aleatório...") {
                    private Graph resultGraph;
                    @Override
                    protected Void doInBackground() throws Exception {
                        long start = System.currentTimeMillis();
                        resultGraph = GraphGenerator.generateRandomGraph(n, m, conn);
                        lastExecutionTime = System.currentTimeMillis() - start;
                        return null;
                    }
                    @Override
                    protected void done() {
                        super.done();
                        if (resultGraph != null) {
                            setGraph(resultGraph, false);
                            logHTML("<b>Grafo Aleatório Gerado!</b><br>" +
                                    "• Vértices: " + resultGraph.numVertices + "<br>" +
                                    "• Arestas: " + m + "<br>" +
                                    "• Conexo: " + (conn ? "Sim" : "Não") + "<br>" +
                                    "• Tempo: " + lastExecutionTime + " ms");
                        } else {
                            JOptionPane.showMessageDialog(GraphGUI.this,
                                "Erro na geração do grafo. Verifique os parâmetros.\n" +
                                "(Mínimo de arestas para conectado: V-1; Máximo de arestas: V*(V-1)/2)",
                                "Erro de Geração", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.executeWorker();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Insira valores inteiros válidos.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; dlg.add(lblV, gbc);
        gbc.gridx = 1; dlg.add(txtV, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dlg.add(lblE, gbc);
        gbc.gridx = 1; dlg.add(txtE, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; dlg.add(chkConn, gbc);
        gbc.gridy = 3; dlg.add(btnConfirm, gbc);

        dlg.setVisible(true);
    }

    private void loadPajek() {
        JFileChooser chooser = new JFileChooser(new File("pajek/input"));
        chooser.setDialogTitle("Importar Grafo (Formato Pajek)");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            ProgressWorker worker = new ProgressWorker("Carregando arquivo Pajek...") {
                private Graph resultGraph;
                @Override
                protected Void doInBackground() throws Exception {
                    long start = System.currentTimeMillis();
                    // PajekIO expects just the filename inside pajek/input/
                    resultGraph = PajekIO.importFrom(file.getName());
                    lastExecutionTime = System.currentTimeMillis() - start;
                    return null;
                }
                @Override
                protected void done() {
                    super.done();
                    if (resultGraph != null) {
                        setGraph(resultGraph, true);
                        logHTML("<b>Grafo Carregado do Pajek!</b><br>" +
                                "• Arquivo: " + file.getName() + "<br>" +
                                "• Vértices: " + resultGraph.numVertices + "<br>" +
                                "• Direcionado: " + (resultGraph.directed ? "Sim" : "Não") + "<br>" +
                                "• Tempo: " + lastExecutionTime + " ms");
                    } else {
                        JOptionPane.showMessageDialog(GraphGUI.this,
                            "Erro ao carregar o arquivo Pajek. Verifique a formatação.",
                            "Erro de I/O", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.executeWorker();
        }
    }

    private void runConnectivity() {
        if (graph == null) {
            showNoGraphWarning();
            return;
        }
        long start = System.currentTimeMillis();
        boolean connected = algs.isConnected();
        lastExecutionTime = System.currentTimeMillis() - start;
        updateStats();

        String detail = connected 
            ? "O grafo é <b>fortemente conectado</b> (ou fracamente conectado para direcionados), o que significa que existe um caminho entre qualquer par de vértices."
            : "O grafo <b>não é conectado</b>. Existem múltiplos componentes separados. Use a opção 'Componentes' para investigá-los.";

        logHTML("<b>Análise de Conectividade</b><br>" +
                "• Resultado: <font color=\"" + (connected ? "#4ac997" : "#ff6b6b") + "\"><b>" + (connected ? "Conexo" : "Desconexo") + "</b></font><br>" +
                "• Tempo de análise: " + lastExecutionTime + " ms<br><br>" +
                detail);
    }

    private void runComponents() {
        if (graph == null) {
            showNoGraphWarning();
            return;
        }
        ProgressWorker worker = new ProgressWorker("Calculando Componentes...") {
            private List<List<Integer>> components;
            @Override
            protected Void doInBackground() throws Exception {
                long start = System.currentTimeMillis();
                components = algs.getComponents();
                lastExecutionTime = System.currentTimeMillis() - start;
                return null;
            }
            @Override
            protected void done() {
                super.done();
                updateStats();
                StringBuilder sb = new StringBuilder("<b>Componentes Conexos (" + components.size() + " total)</b><br>" +
                        "• Tempo: " + lastExecutionTime + " ms<br><br>");
                for (int i = 0; i < Math.min(15, components.size()); i++) {
                    List<Integer> c = components.get(i);
                    sb.append("<b>Componente ").append(i + 1).append("</b> (").append(c.size()).append(" nós): {");
                    int limit = Math.min(c.size(), 5);
                    for (int j = 0; j < limit; j++) {
                        sb.append(graph.getName(c.get(j)));
                        if (j < limit - 1) sb.append(", ");
                    }
                    if (c.size() > limit) sb.append(", ...");
                    sb.append("}<br>");
                }
                if (components.size() > 15) {
                    sb.append("... e mais ").append(components.size() - 15).append(" componentes omitidos.");
                }
                logHTML(sb.toString());
            }
        };
        worker.executeWorker();
    }

    private void runCycles() {
        if (graph == null) {
            showNoGraphWarning();
            return;
        }
        long start = System.currentTimeMillis();
        boolean hasCycle = algs.hasCycle();
        lastExecutionTime = System.currentTimeMillis() - start;
        updateStats();

        logHTML("<b>Detecção de Ciclos</b><br>" +
                "• Contém Ciclo: <font color=\"" + (hasCycle ? "#ff6b6b" : "#4ac997") + "\"><b>" + (hasCycle ? "Sim" : "Não") + "</b></font><br>" +
                "• Tempo: " + lastExecutionTime + " ms<br><br>" +
                (hasCycle ? "O grafo possui pelo menos um ciclo de conexões fechado." : "O grafo é acíclico (uma floresta/árvore)."));
    }

    private void runEulerian() {
        if (graph == null) {
            showNoGraphWarning();
            return;
        }
        long start = System.currentTimeMillis();
        int res = algs.isEulerian();
        lastExecutionTime = System.currentTimeMillis() - start;
        updateStats();

        String status = switch (res) {
            case 2 -> "<font color=\"#4ac997\"><b>Sim (Ciclo Euleriano)</b></font>";
            case 1 -> "<font color=\"#ffb703\"><b>Semi-Euleriano (Caminho Euleriano)</b></font>";
            default -> "<font color=\"#ff6b6b\"><b>Não</b></font>";
        };

        logHTML("<b>Grafo Euleriano?</b><br>" +
                "• Resultado: " + status + "<br>" +
                "• Tempo: " + lastExecutionTime + " ms<br><br>" +
                (res == 2 ? "É possível percorrer todas as arestas do grafo exatamente uma vez e voltar ao ponto de partida." 
                : (res == 1 ? "É possível percorrer todas as arestas exatamente uma vez, mas o ponto de início e fim serão diferentes." 
                : "Não há trilha euleriana. Graus dos vértices não atendem aos critérios.")));
    }

    private void runCloseness() {
        if (graph == null) {
            showNoGraphWarning();
            return;
        }

        if (graph.numVertices > 500) {
            int option = JOptionPane.showConfirmDialog(this,
                "Grafo grande (" + graph.numVertices + " vértices). O cálculo de Centralidade de Proximidade\n" +
                "requer rodar Dijkstra para todos os pares e pode levar tempo. Continuar?",
                "Cálculo Pesado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option != JOptionPane.YES_OPTION) return;
        }

        ProgressWorker worker = new ProgressWorker("Calculando Proximidade...") {
            private double[] c;
            @Override
            protected Void doInBackground() throws Exception {
                long start = System.currentTimeMillis();
                c = algs.closenessCentrality();
                lastExecutionTime = System.currentTimeMillis() - start;
                return null;
            }
            @Override
            protected void done() {
                super.done();
                updateStats();
                graphPanel.setCentralities(c, null);

                // Build Top Nodes ranking
                List<NodeRank> ranking = new ArrayList<>();
                for (int i = 0; i < graph.numVertices; i++) {
                    ranking.add(new NodeRank(i, c[i]));
                }
                Collections.sort(ranking);

                topNodesListModel.clear();
                topNodesIndices.clear();

                StringBuilder sb = new StringBuilder("<b>Centralidade de Proximidade (Top 10)</b><br>" +
                        "• Tempo: " + lastExecutionTime + " ms<br><br>");
                for (int i = 0; i < Math.min(10, ranking.size()); i++) {
                    NodeRank r = ranking.get(i);
                    sb.append(String.format("<b>#%d %s:</b> %.6f<br>", i + 1, graph.getName(r.index), r.val));
                    
                    if (i < 5) {
                        topNodesListModel.addElement(String.format("%s (%.4f)", graph.getName(r.index), r.val));
                        topNodesIndices.add(r.index);
                    }
                }
                logHTML(sb.toString());

                // Update selected node's displayed centrality if visible
                int selectedIdx = graphPanel.getSelectedNodeIndex();
                if (selectedIdx != -1 && selectedIdx < c.length) {
                    lblNodeCentrality.setText(String.format("%.6f (Prox)", c[selectedIdx]));
                }
            }
        };
        worker.executeWorker();
    }

    private void runBetweenness() {
        if (graph == null) {
            showNoGraphWarning();
            return;
        }

        if (graph.numVertices > 500) {
            int option = JOptionPane.showConfirmDialog(this,
                "Grafo grande (" + graph.numVertices + " vértices). O algoritmo de Brandes\n" +
                "pode levar vários segundos. Continuar?",
                "Cálculo Pesado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option != JOptionPane.YES_OPTION) return;
        }

        ProgressWorker worker = new ProgressWorker("Calculando Intermediação...") {
            private double[] b;
            @Override
            protected Void doInBackground() throws Exception {
                long start = System.currentTimeMillis();
                b = algs.betweennessCentrality();
                lastExecutionTime = System.currentTimeMillis() - start;
                return null;
            }
            @Override
            protected void done() {
                super.done();
                updateStats();
                graphPanel.setCentralities(null, b);

                // Build Top Nodes ranking
                List<NodeRank> ranking = new ArrayList<>();
                for (int i = 0; i < graph.numVertices; i++) {
                    ranking.add(new NodeRank(i, b[i]));
                }
                Collections.sort(ranking);

                topNodesListModel.clear();
                topNodesIndices.clear();

                StringBuilder sb = new StringBuilder("<b>Centralidade de Intermediação (Top 10)</b><br>" +
                        "• Tempo: " + lastExecutionTime + " ms<br><br>");
                for (int i = 0; i < Math.min(10, ranking.size()); i++) {
                    NodeRank r = ranking.get(i);
                    sb.append(String.format("<b>#%d %s:</b> %.2f<br>", i + 1, graph.getName(r.index), r.val));
                    
                    if (i < 5) {
                        topNodesListModel.addElement(String.format("%s (%.2f)", graph.getName(r.index), r.val));
                        topNodesIndices.add(r.index);
                    }
                }
                logHTML(sb.toString());

                // Update selected node's displayed centrality if visible
                int selectedIdx = graphPanel.getSelectedNodeIndex();
                if (selectedIdx != -1 && selectedIdx < b.length) {
                    lblNodeCentrality.setText(String.format("%.2f (Interm)", b[selectedIdx]));
                }
            }
        };
        worker.executeWorker();
    }

    private void runRecommendation() {
        if (graph == null) {
            showNoGraphWarning();
            return;
        }

        // Styled inputs inside JDialog
        JDialog dlg = new JDialog(this, "Recomendação de Seguidores", true);
        dlg.setLayout(new GridBagLayout());
        dlg.getContentPane().setBackground(COLOR_BG_HEADER);
        dlg.setSize(400, 240);
        dlg.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 12, 8, 12);

        JLabel lblSrc = new JLabel("Pessoa de Origem (De):");
        lblSrc.setForeground(COLOR_TEXT_LIGHT);
        JTextField txtSrc = new JTextField();
        txtSrc.setBackground(COLOR_BG_DARK);
        txtSrc.setForeground(COLOR_TEXT_LIGHT);
        txtSrc.setCaretColor(COLOR_TEXT_LIGHT);
        txtSrc.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

        // Autofill selected node
        int selectedIdx = graphPanel.getSelectedNodeIndex();
        if (selectedIdx != -1) {
            txtSrc.setText(graph.getName(selectedIdx));
        }

        JLabel lblTgt = new JLabel("Pessoa de Destino (Para):");
        lblTgt.setForeground(COLOR_TEXT_LIGHT);
        JTextField txtTgt = new JTextField();
        txtTgt.setBackground(COLOR_BG_DARK);
        txtTgt.setForeground(COLOR_TEXT_LIGHT);
        txtTgt.setCaretColor(COLOR_TEXT_LIGHT);
        txtTgt.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

        PrimaryButton btnRecommend = new PrimaryButton("Analisar Recomendações", null);
        btnRecommend.addActionListener(e -> {
            String src = txtSrc.getText().trim();
            String tgt = txtTgt.getText().trim();
            if (src.isEmpty() || tgt.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Ambos os nomes devem ser preenchidos.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dlg.dispose();

            long start = System.currentTimeMillis();
            String resultStr = social.recommendFollower(src, tgt);
            lastExecutionTime = System.currentTimeMillis() - start;
            updateStats();

            // Custom HTML display format for recommendations
            String htmlResult;
            if (resultStr.startsWith("Recommended!")) {
                // Parse out path
                String pathPart = resultStr.substring(resultStr.indexOf("path found:") + 11).trim();
                htmlResult = "<font color=\"#4ac997\"><b>Recomendação Aprovada!</b></font><br>" +
                        "Existe um caminho de conexões indiretas entre '" + src + "' e '" + tgt + "'.<br><br>" +
                        "<b>Caminho encontrado:</b><br><font color=\"#fb8500\">" + pathPart.replace("->", " ➔ ") + "</font>";

                // Extract path node indices to highlight
                String[] names = pathPart.split(" -> ");
                List<Integer> pathIndices = new ArrayList<>();
                for (String name : names) {
                    int id = graph.findByName(name.trim());
                    if (id != -1) pathIndices.add(id);
                }
                graphPanel.setHighlightedPath(pathIndices);
            } else {
                htmlResult = "<font color=\"#ff6b6b\"><b>Recomendação Rejeitada</b></font><br>" +
                        resultStr;
                graphPanel.setHighlightedPath(null);
            }
            logHTML(htmlResult);
        });

        gbc.gridx = 0; gbc.gridy = 0; dlg.add(lblSrc, gbc);
        gbc.gridx = 1; dlg.add(txtSrc, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dlg.add(lblTgt, gbc);
        gbc.gridx = 1; dlg.add(txtTgt, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; dlg.add(btnRecommend, gbc);

        dlg.setVisible(true);
    }

    private void runFollowersAction() {
        int idx = graphPanel.getSelectedNodeIndex();
        if (idx == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um nó no grafo ou na lista de top nós primeiro.",
                "Nenhum Nó Selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long start = System.currentTimeMillis();
        // Get followed (outgoing)
        List<int[]> outEdges = graph.neighbors(idx);
        // Get followers (incoming)
        List<Integer> inNodes = new ArrayList<>();
        for (int u = 0; u < graph.numVertices; u++) {
            if (u == idx) continue;
            for (int[] edge : graph.neighbors(u)) {
                if (edge[0] == idx) {
                    inNodes.add(u);
                    break;
                }
            }
        }
        lastExecutionTime = System.currentTimeMillis() - start;
        updateStats();

        StringBuilder sb = new StringBuilder("<b>Conexões de " + graph.getName(idx) + "</b><br>" +
                "• Tempo de busca: " + lastExecutionTime + " ms<br><br>");

        sb.append("<b>Segue (").append(outEdges.size()).append(" pessoas):</b><br>");
        if (outEdges.isEmpty()) {
            sb.append("<font color=\"#8b949e\">Ninguém</font><br>");
        } else {
            for (int i = 0; i < Math.min(10, outEdges.size()); i++) {
                sb.append("• ").append(graph.getName(outEdges.get(i)[0])).append("<br>");
            }
            if (outEdges.size() > 10) {
                sb.append("• ... e mais ").append(outEdges.size() - 10).append(" pessoas.<br>");
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
                sb.append("• ... e mais ").append(inNodes.size() - 10).append(" pessoas.<br>");
            }
        }

        logHTML(sb.toString());
    }

    private void exportPajek() {
        if (graph == null) {
            showNoGraphWarning();
            return;
        }
        if (wasLoaded) {
            JOptionPane.showMessageDialog(this,
                "Este grafo foi importado de arquivo. A exportação está desabilitada para evitar sobrescrever dados originais.",
                "Exportação Bloqueada", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String filename = JOptionPane.showInputDialog(this,
            "Digite o nome do arquivo de saída (ex: network.txt):",
            "Exportar Pajek", JOptionPane.PLAIN_MESSAGE);
        
        if (filename != null && !filename.trim().isEmpty()) {
            long start = System.currentTimeMillis();
            PajekIO.export(graph, filename.trim());
            lastExecutionTime = System.currentTimeMillis() - start;
            updateStats();
            logHTML("<b>Grafo Exportado com Sucesso!</b><br>" +
                    "• Salvo em: pajek/output/" + filename.trim() + "<br>" +
                    "• Tempo: " + lastExecutionTime + " ms");
        }
    }

    private void saveSession() {
        if (graph == null) {
            showNoGraphWarning();
            return;
        }
        long start = System.currentTimeMillis();
        // Save to standard session file inside pajek/output/
        PajekIO.export(graph, "session_save.net");
        lastExecutionTime = System.currentTimeMillis() - start;
        updateStats();
        logHTML("<b>Sessão Salva com Sucesso!</b><br>" +
                "• Arquivo: pajek/output/session_save.net<br>" +
                "• Tempo: " + lastExecutionTime + " ms");
    }

    private void loadSession() {
        ProgressWorker worker = new ProgressWorker("Carregando sessão anterior...") {
            private Graph resultGraph;
            @Override
            protected Void doInBackground() throws Exception {
                long start = System.currentTimeMillis();
                // We search inside pajek/output/ for session_save.net, but PajekIO looks in pajek/input/
                // So we check if file exists in output, and load it
                File src = new File("pajek/output/session_save.net");
                if (src.exists()) {
                    // Copy to input directory temporarily or load directly.
                    // PajekIO expects relative to pajek/input
                    // Let's load via a custom reader or temporarily copy it. Actually, PajekIO just looks in pajek/input.
                    // Let's copy it to pajek/input/session_save.net so PajekIO can load it!
                    File dest = new File("pajek/input/session_save.net");
                    java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    resultGraph = PajekIO.importFrom("session_save.net");
                }
                lastExecutionTime = System.currentTimeMillis() - start;
                return null;
            }
            @Override
            protected void done() {
                super.done();
                if (resultGraph != null) {
                    setGraph(resultGraph, true);
                    logHTML("<b>Sessão Carregada com Sucesso!</b><br>" +
                            "• Vértices: " + resultGraph.numVertices + "<br>" +
                            "• Tempo: " + lastExecutionTime + " ms");
                } else {
                    JOptionPane.showMessageDialog(GraphGUI.this,
                        "Nenhuma sessão salva encontrada em pajek/output/session_save.net.",
                        "Erro de Carregamento", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.executeWorker();
    }

    private void showNoGraphWarning() {
        JOptionPane.showMessageDialog(this,
            "Por favor, gere uma Rede Social ou Grafo Aleatório antes de executar esta operação.",
            "Nenhum Grafo Carregado", JOptionPane.WARNING_MESSAGE);
    }

    // Helper rank model for top nodes centralities ranking
    private static class NodeRank implements Comparable<NodeRank> {
        final int index;
        final double val;

        NodeRank(int index, double val) {
            this.index = index;
            this.val = val;
        }

        @Override
        public int compareTo(NodeRank o) {
            // Sort in descending order
            return Double.compare(o.val, this.val);
        }
    }

    // A background worker showing a dialog so the UI doesn't freeze
    private abstract class ProgressWorker extends SwingWorker<Void, Void> {
        private final JDialog dialog;

        ProgressWorker(String msg) {
            dialog = new JDialog(GraphGUI.this, "Processando", true);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(COLOR_BG_HEADER);
            dialog.setSize(240, 80);
            dialog.setLocationRelativeTo(GraphGUI.this);
            dialog.setUndecorated(true);
            
            JPanel p = new JPanel(new BorderLayout(8, 8));
            p.setBackground(COLOR_BG_HEADER);
            p.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
            
            JLabel l = new JLabel("  " + msg);
            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            l.setForeground(COLOR_TEXT_LIGHT);
            p.add(l, BorderLayout.CENTER);
            
            JProgressBar bar = new JProgressBar();
            bar.setIndeterminate(true);
            bar.setBackground(COLOR_BG_DARK);
            bar.setForeground(COLOR_ACCENT_ORANGE);
            bar.setBorder(null);
            p.add(bar, BorderLayout.SOUTH);
            
            dialog.add(p);
        }

        @Override
        protected void done() {
            dialog.dispose();
        }

        public void executeWorker() {
            super.execute();
            dialog.setVisible(true);
        }
    }

    // Collapsible accordion component
    private static class CollapsiblePanel extends JPanel {
        private final JButton headerButton;
        private final JPanel contentPanel;
        private boolean expanded;

        public CollapsiblePanel(String title, Icon icon, JPanel content, boolean startExpanded) {
            setLayout(new BorderLayout());
            setBackground(COLOR_BG_SIDEBAR);
            this.contentPanel = content;
            this.expanded = startExpanded;

            headerButton = new JButton(title, icon) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Paint custom dark background
                    g2.setColor(COLOR_BG_HEADER);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Draw bottom border line
                    g2.setColor(COLOR_BORDER);
                    g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                    
                    // Draw '+' or '−' indicator
                    g2.setColor(COLOR_TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    String indicator = expanded ? "−" : "+";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(indicator, getWidth() - fm.stringWidth(indicator) - 16, getHeight() / 2 + 4);
                    g2.dispose();
                    
                    super.paintComponent(g);
                }
            };
            headerButton.setHorizontalAlignment(SwingConstants.LEFT);
            headerButton.setFocusPainted(false);
            headerButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
            headerButton.setForeground(COLOR_TEXT_LIGHT);
            headerButton.setOpaque(false);
            headerButton.setContentAreaFilled(false);
            headerButton.setBorderPainted(false);
            headerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            headerButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 32)
            ));

            headerButton.addActionListener(e -> toggle());

            add(headerButton, BorderLayout.NORTH);
            add(contentPanel, BorderLayout.CENTER);
            contentPanel.setVisible(expanded);
        }

        private void toggle() {
            expanded = !expanded;
            contentPanel.setVisible(expanded);
            revalidate();
            repaint();
        }
    }

    // Menu Item Button inside Accordion
    private static class MenuActionButton extends JButton {
        private boolean hovered = false;

        public MenuActionButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(COLOR_ACCENT_BLUE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    setBorder(BorderFactory.createEmptyBorder(8, 32, 8, 8)); // slide animation
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 16));
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (hovered) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(88, 166, 255, 25)); // Faint blue background
                g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 4, 4);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    // Gradient Primary Button
    private static class PrimaryButton extends JButton {
        public PrimaryButton(String text, Icon icon) {
            super(text, icon);
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(new Color(15, 17, 23));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color c1 = COLOR_ACCENT_ORANGE;
            Color c2 = new Color(255, 183, 3);

            if (getModel().isPressed()) {
                c1 = c1.darker();
                c2 = c2.darker();
            } else if (getModel().isRollover()) {
                c1 = c1.brighter();
                c2 = c2.brighter();
            }

            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
