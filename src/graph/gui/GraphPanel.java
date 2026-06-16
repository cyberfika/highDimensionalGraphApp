package graph.gui;

import graph.model.Graph;
import graph.algorithm.GraphAlgorithms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Painel interativo para visualização de grafos com zoom, pan e seleção de nós.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Renderizar grafo com nós e arestas em escala interativa</li>
 *   <li>Gerenciar two view modes: Global e Neighborhood Focus</li>
 *   <li>Processar interações de usuário (mouse, zoom, pan, seleção)</li>
 *   <li>Aplicar algoritmos de layout (spiral global e concêntrico local)</li>
 *   <li>Visualizar propriedades de grafo (comunidades, centralidade, caminhos)</li>
 * </ul>
 *
 * <p>Modos de Visualização:
 * <ul>
 *   <li><b>Global View:</b> Espiral dourada com todos os nós, agrupados por comunidade</li>
 *   <li><b>Neighborhood Focus:</b> Nó selecionado no centro, vizinhos em anéis concêntricos</li>
 * </ul>
 *
 * <p>Recursos Interativos:
 * <ul>
 *   <li>Zoom (scroll do mouse) com zoom limitado (0.005x a 20x)</li>
 *   <li>Pan (arrastar mouse) para mover viewport</li>
 *   <li>Seleção de nó (click) com efeitos visuais</li>\n *   <li>Hover (passar mouse) com destaque de conexões</li>
 *   <li>Arrastar nó (apenas em Global View) para reposicionar</li>
 * </ul>
 *
 * <p>Otimizações de Performance:
 * <ul>
 *   <li>Viewport culling para nós e arestas fora da tela</li>
 *   <li>Edge sampling quando muito afastado (zoom < 0.4)</li>
 *   <li>Renderização seletiva de rótulos baseada em zoom</li>
 *   <li>Antialiasing inteligente</li>
 * </ul>
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see Graph\n * @see GraphGUI\n * @see Theme
 */
public class GraphPanel extends JPanel {
    private Graph graph;
    private int[] nodeCommunities;
    private double[] closenessRank;
    private double[] betweennessRank;

    // View State
    private boolean globalViewMode = true;
    private double scale = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private Point lastMousePos;

    // Graph Layout positions in absolute coordinates
    private double[] nodeX;
    private double[] nodeY;

    // Interactive State
    private int hoveredNodeIndex = -1;
    private int selectedNodeIndex = -1;
    private int draggedNodeIndex = -1;
    private List<Integer> highlightedPath = null;

    public void setHighlightedPath(List<Integer> path) {
        this.highlightedPath = path;
        repaint();
    }

    // Callback for when a node is selected
    private NodeSelectionListener selectionListener;

    // Color Palette
    private static final Color BG_GRADIENT_START = new Color(13, 17, 23);
    private static final Color BG_GRADIENT_END = new Color(22, 27, 34);
    private static final Color BORDER_COLOR = new Color(48, 54, 61);
    private static final Color COLOR_TEXT = new Color(230, 237, 243);
    private static final Color COLOR_MUTED = new Color(139, 148, 158);
    private static final Color COLOR_ACCENT_ORANGE = new Color(251, 133, 0);
    private static final Color COLOR_ACCENT_BLUE = new Color(88, 166, 255);

    private static final Color[] PALETTE = {
        new Color(88, 166, 255),   // Blue
        new Color(251, 133, 0),    // Orange
        new Color(76, 201, 240),   // Light Blue
        new Color(74, 201, 151),   // Teal
        new Color(247, 37, 133),   // Pink
        new Color(114, 9, 183),    // Purple
        new Color(255, 209, 102),  // Yellow
        new Color(6, 214, 160),    // Mint
        new Color(239, 71, 111),   // Coral
        new Color(156, 102, 255)   // Light Purple
    };

    public interface NodeSelectionListener {
        void onNodeSelected(int nodeIndex);
    }

    public GraphPanel() {
        setBackground(BG_GRADIENT_START);
        setOpaque(true);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
                if (graph == null) return;

                int clicked = findNodeAt(e.getPoint());
                if (clicked != -1) {
                    setSelectedNodeIndex(clicked);
                    if (globalViewMode) {
                        draggedNodeIndex = clicked;
                    }
                } else {
                    draggedNodeIndex = -1;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedNodeIndex = -1;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (graph == null) return;

                if (draggedNodeIndex != -1 && globalViewMode) {
                    Point2D.Double p = toGraphCoords(e.getPoint());
                    nodeX[draggedNodeIndex] = p.x;
                    nodeY[draggedNodeIndex] = p.y;
                    repaint();
                } else {
                    // Pan
                    double dx = (e.getX() - lastMousePos.x) / scale;
                    double dy = (e.getY() - lastMousePos.y) / scale;
                    offsetX += dx;
                    offsetY += dy;
                    lastMousePos = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (graph == null) return;
                int hover = findNodeAt(e.getPoint());
                if (hover != hoveredNodeIndex) {
                    hoveredNodeIndex = hover;
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (graph == null) return;
                
                double rotation = e.getPreciseWheelRotation();
                if (rotation == 0.0) return;
                
                // Exponential zoom for smooth scaling (rotation < 0 is zoom in, rotation > 0 is zoom out)
                double zoomFactor = Math.pow(0.85, rotation);

                Point mouse = e.getPoint();
                Point2D.Double graphMouse = toGraphCoords(mouse);
                
                scale *= zoomFactor;
                scale = Math.max(0.005, Math.min(scale, 20.0));

                // Recalculate offsets to zoom on mouse cursor
                offsetX = (mouse.x - getWidth() / 2.0) / scale - graphMouse.x;
                offsetY = (mouse.y - getHeight() / 2.0) / scale - graphMouse.y;
                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    public void setSelectionListener(NodeSelectionListener listener) {
        this.selectionListener = listener;
    }

    /**
     * Carrega novo grafo e recomputa layout e estruturas de visualização.
     *
     * <p>Operações realizadas:\n * <ul>
     *   <li>Armazena referências de grafo e algoritmos</li>
     *   <li>Detecta comunidades (componentes conexos) para colorização</li>
     *   <li>Computa layout global (espiral dourada)</li>
     *   <li>Reseta zoom e pan para enquadrar grafo todo</li>
     *   <li>Limpa seleção de nó e estado de hover</li>
     * </ul>
     *
     * <p>Se grafo é nulo, painel exibe placeholder \"Nenhum grafo carregado\".\n *
     * @param graph novo grafo a visualizar (pode ser {@code null})\n     * @param algs algoritmos para detectar comunidades (componentes conexos)\n     */
    public void setGraph(Graph graph, GraphAlgorithms algs) {
        this.graph = graph;
        this.hoveredNodeIndex = -1;
        this.selectedNodeIndex = -1;
        this.draggedNodeIndex = -1;
        this.closenessRank = null;
        this.betweennessRank = null;
        this.highlightedPath = null;

        if (graph == null) {
            this.nodeX = null;
            this.nodeY = null;
            this.nodeCommunities = null;
            repaint();
            return;
        }

        // Determine community structures based on connected components
        this.nodeCommunities = new int[graph.numVertices];
        Arrays.fill(nodeCommunities, 0);
        if (algs != null) {
            List<List<Integer>> components = algs.getComponents();
            for (int compId = 0; compId < components.size(); compId++) {
                for (int node : components.get(compId)) {
                    nodeCommunities[node] = compId;
                }
            }
        }

        // Compute layout coordinates
        computeGlobalLayout();
        resetZoomAndPan();
    }

    public void setCentralities(double[] closeness, double[] betweenness) {
        this.closenessRank = closeness;
        this.betweennessRank = betweenness;
        repaint();
    }

    public void resetZoomAndPan() {
        this.scale = 1.0;
        this.offsetX = 0.0;
        this.offsetY = 0.0;
        if (graph != null) {
            // Find bounding box and scale to fit
            double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
            double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            
            double[] currentX = globalViewMode ? nodeX : getNeighborhoodX();
            double[] currentY = globalViewMode ? nodeY : getNeighborhoodY();

            if (currentX != null && currentX.length > 0) {
                for (int i = 0; i < graph.numVertices; i++) {
                    if (!globalViewMode && !isNeighborhoodMember(i)) continue;
                    minX = Math.min(minX, currentX[i]);
                    maxX = Math.max(maxX, currentX[i]);
                    minY = Math.min(minY, currentY[i]);
                    maxY = Math.max(maxY, currentY[i]);
                }
                
                double width = maxX - minX;
                double height = maxY - minY;
                
                if (width > 0 && height > 0) {
                    double pad = 60;
                    double scaleW = (getWidth() - pad * 2) / width;
                    double scaleH = (getHeight() - pad * 2) / height;
                    this.scale = Math.min(scaleW, scaleH);
                    this.scale = Math.max(0.01, Math.min(this.scale, 2.0));
                    
                    this.offsetX = -(minX + width / 2.0);
                    this.offsetY = -(minY + height / 2.0);
                }
            }
        }
        repaint();
    }

    /**
     * Alterna entre modo de visão global e foco na vizinhança.
     *
     * <p><b>Global View:</b> Exibe todos os nós em layout de espiral com comunidades coloridas.\n * <b>Neighborhood Focus:</b> Exibe nó selecionado no centro com vizinhos em anéis.\n *
     * <p>Ao alternar modo, layout é recalculado e zoom/pan são resetados.\n *
     * @param global {@code true} para Global View, {@code false} para Neighborhood Focus\n     */
    public void setGlobalViewMode(boolean global) {
        if (this.globalViewMode != global) {
            this.globalViewMode = global;
            resetZoomAndPan();
        }
    }

    public boolean isGlobalViewMode() {
        return this.globalViewMode;
    }

    /**
     * Seleciona um nó e notifica listener de seleção.
     *
     * <p>Efeitos:\n * <ul>
     *   <li>Marca nó como selecionado (ampliar visual, cor laranja)</li>
     *   <li>Se em Neighborhood Focus, recomputa layout com novo nó como centro</li>
     *   <li>Dispara callback de {@link NodeSelectionListener#onNodeSelected(int)}</li>
     *   <li>Redesenha painel</li>
     * </ul>
     *
     * @param index índice do nó a selecionar (-1 para desselecionar)\n     */
    public void setSelectedNodeIndex(int index) {
        this.selectedNodeIndex = index;
        if (selectionListener != null) {
            selectionListener.onNodeSelected(index);
        }
        if (!globalViewMode) {
            // Recompute neighborhood layout since focal node changed
            resetZoomAndPan();
        }
        repaint();
    }

    public int getSelectedNodeIndex() {
        return this.selectedNodeIndex;
    }

    /**
     * Compute a clean spiral/concentric grid layout for all nodes.
     */
    private void computeGlobalLayout() {
        if (graph == null) return;
        int n = graph.numVertices;
        nodeX = new double[n];
        nodeY = new double[n];

        // We use a spiral layout that scales based on community and node count
        // Spiral layout puts the central nodes at the center, spreading out nicely
        double goldenAngle = 137.5 * Math.PI / 180.0;
        double spacing = 18.0;
        if (n > 1000) {
            spacing = 28.0;
        }

        for (int i = 0; i < n; i++) {
            double r = spacing * Math.sqrt(i + 1);
            double theta = i * goldenAngle;
            
            // Add slight jitter/variation by community to create visual groupings
            int community = nodeCommunities[i];
            double offsetR = (community % 3) * (spacing * 1.5);
            double offsetAngle = (community % 7) * (Math.PI / 4.0);
            
            nodeX[i] = (r + offsetR) * Math.cos(theta + offsetAngle);
            nodeY[i] = (r + offsetR) * Math.sin(theta + offsetAngle);
        }
    }

    // Neighborhood View layout coordinates generator
    private double[] getNeighborhoodX() {
        if (graph == null || selectedNodeIndex == -1) return nodeX;
        double[] x = new double[graph.numVertices];
        
        // Selected node at center
        x[selectedNodeIndex] = 0.0;

        List<Integer> outNodes = getOutNeighbors(selectedNodeIndex);
        List<Integer> inNodes = getInNeighbors(selectedNodeIndex);

        // Position out-neighbors on inner circle
        double r1 = 200.0;
        for (int i = 0; i < outNodes.size(); i++) {
            double angle = (2 * Math.PI * i) / Math.max(1, outNodes.size());
            x[outNodes.get(i)] = r1 * Math.cos(angle);
        }

        // Position in-neighbors on outer circle
        double r2 = 380.0;
        for (int i = 0; i < inNodes.size(); i++) {
            double angle = (2 * Math.PI * i) / Math.max(1, inNodes.size()) + (Math.PI / 8.0); // offset slightly
            x[inNodes.get(i)] = r2 * Math.cos(angle);
        }

        return x;
    }

    private double[] getNeighborhoodY() {
        if (graph == null || selectedNodeIndex == -1) return nodeY;
        double[] y = new double[graph.numVertices];
        
        // Selected node at center
        y[selectedNodeIndex] = 0.0;

        List<Integer> outNodes = getOutNeighbors(selectedNodeIndex);
        List<Integer> inNodes = getInNeighbors(selectedNodeIndex);

        // Position out-neighbors on inner circle
        double r1 = 200.0;
        for (int i = 0; i < outNodes.size(); i++) {
            double angle = (2 * Math.PI * i) / Math.max(1, outNodes.size());
            y[outNodes.get(i)] = r1 * Math.sin(angle);
        }

        // Position in-neighbors on outer circle
        double r2 = 380.0;
        for (int i = 0; i < inNodes.size(); i++) {
            double angle = (2 * Math.PI * i) / Math.max(1, inNodes.size()) + (Math.PI / 8.0);
            y[inNodes.get(i)] = r2 * Math.sin(angle);
        }

        return y;
    }

    private boolean isNeighborhoodMember(int idx) {
        if (idx == selectedNodeIndex) return true;
        // Check if out-neighbor
        for (int[] edge : graph.neighbors(selectedNodeIndex)) {
            if (edge[0] == idx) return true;
        }
        // Check if in-neighbor (costly, but bounded since we query the adjacency list)
        for (int u = 0; u < graph.numVertices; u++) {
            for (int[] edge : graph.neighbors(u)) {
                if (edge[0] == idx && u == selectedNodeIndex) return true;
            }
        }
        // Since we want in-neighbors, we search where destination is selectedNodeIndex
        for (int[] edge : graph.neighbors(idx)) {
            if (edge[0] == selectedNodeIndex) return true;
        }
        return false;
    }

    private List<Integer> getOutNeighbors(int node) {
        List<Integer> res = new ArrayList<>();
        for (int[] edge : graph.neighbors(node)) {
            res.add(edge[0]);
        }
        return res;
    }

    private List<Integer> getInNeighbors(int node) {
        List<Integer> res = new ArrayList<>();
        for (int u = 0; u < graph.numVertices; u++) {
            if (u == node) continue;
            for (int[] edge : graph.neighbors(u)) {
                if (edge[0] == node) {
                    res.add(u);
                    break;
                }
            }
        }
        return res;
    }

    private int findNodeAt(Point screenPt) {
        if (graph == null) return -1;
        Point2D.Double graphPt = toGraphCoords(screenPt);

        double[] currentX = globalViewMode ? nodeX : getNeighborhoodX();
        double[] currentY = globalViewMode ? nodeY : getNeighborhoodY();

        double clickRadius = 14.0 / scale; // Screen-relative size
        clickRadius = Math.max(8.0, Math.min(clickRadius, 30.0));

        // Iterate backwards so we pick top-rendered nodes first
        for (int i = graph.numVertices - 1; i >= 0; i--) {
            if (!globalViewMode && !isNeighborhoodMember(i)) continue;
            double dx = graphPt.x - currentX[i];
            double dy = graphPt.y - currentY[i];
            double distSq = dx * dx + dy * dy;
            if (distSq <= clickRadius * clickRadius) {
                return i;
            }
        }
        return -1;
    }

    private Point2D.Double toGraphCoords(Point p) {
        double x = (p.x - getWidth() / 2.0) / scale - offsetX;
        double y = (p.y - getHeight() / 2.0) / scale - offsetY;
        return new Point2D.Double(x, y);
    }

    private Point toScreenCoords(double gx, double gy) {
        int x = (int) Math.round((gx + offsetX) * scale + getWidth() / 2.0);
        int y = (int) Math.round((gy + offsetY) * scale + getHeight() / 2.0);
        return new Point(x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw premium background gradient
        GradientPaint gp = new GradientPaint(0, 0, BG_GRADIENT_START, 0, getHeight(), BG_GRADIENT_END);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (graph == null) {
            drawPlaceholder(g2);
            return;
        }

        // Apply Zoom & Pan Transform
        AffineTransform oldTx = g2.getTransform();
        g2.translate(getWidth() / 2.0, getHeight() / 2.0);
        g2.scale(scale, scale);
        g2.translate(offsetX, offsetY);

        // Get coordinates depending on view mode
        double[] currentX = globalViewMode ? nodeX : getNeighborhoodX();
        double[] currentY = globalViewMode ? nodeY : getNeighborhoodY();

        // 1. Draw Edges
        drawEdges(g2, currentX, currentY);

        // 2. Draw Nodes
        drawNodes(g2, currentX, currentY);

        // Restore transform
        g2.setTransform(oldTx);

        // 3. Draw overlays (Legend, Scale indicators, interaction hints)
        drawOverlayHUD(g2);
    }

    private void drawPlaceholder(Graphics2D g2) {
        g2.setColor(COLOR_MUTED);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        String icon = "▤"; // A nice chart representation instead of emoji
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(icon, (getWidth() - fm.stringWidth(icon)) / 2, getHeight() / 2 - 30);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(COLOR_TEXT);
        String line1 = "Nenhum grafo carregado";
        fm = g2.getFontMetrics();
        g2.drawString(line1, (getWidth() - fm.stringWidth(line1)) / 2, getHeight() / 2 + 20);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g2.setColor(COLOR_MUTED);
        String line2 = "Clique em \"Gerar Rede Social\" ou \"Grafo Aleatório\" para começar";
        fm = g2.getFontMetrics();
        g2.drawString(line2, (getWidth() - fm.stringWidth(line2)) / 2, getHeight() / 2 + 45);
    }

    private void drawEdges(Graphics2D g2, double[] xs, double[] ys) {
        if (graph == null) return;

        // Optimized edge rendering:
        // For large graphs, we avoid drawing edges if we're zoomed out too far, or only draw selected node's edges.
        // This maintains smooth FPS and prevents clutter.
        boolean zoomedOut = scale < 0.15;
        boolean superZoomedOut = scale < 0.04;
        
        g2.setStroke(new BasicStroke(1.0f));

        if (!globalViewMode) {
            // Neighborhood Focus: only draw edges connecting to/from the center node
            g2.setStroke(new BasicStroke(1.5f));
            for (int u = 0; u < graph.numVertices; u++) {
                if (!isNeighborhoodMember(u)) continue;
                for (int[] edge : graph.neighbors(u)) {
                    int v = edge[0];
                    if (!isNeighborhoodMember(v)) continue;

                    boolean isImportant = (u == selectedNodeIndex || v == selectedNodeIndex);
                    if (!isImportant) continue; // Only draw direct links to focal node

                    if (u == selectedNodeIndex) {
                        g2.setColor(new Color(251, 133, 0, 180)); // outgoing follows (orange)
                    } else {
                        g2.setColor(new Color(88, 166, 255, 180)); // incoming followers (blue)
                    }
                    drawDirectedEdge(g2, xs[u], ys[u], xs[v], ys[v], 12.0);
                }
            }
            return;
        }

        // Global Mode
        if (superZoomedOut) return; // Do not draw edges when extremely zoomed out

        // Draw regular edges with high transparency
        g2.setColor(new Color(230, 237, 243, zoomedOut ? 10 : 35));
        
        // To speed up rendering on massive 25k graphs, we sample edges if zoomed out,
        // but draw ALL edges if zoomed in.
        int step = 1;
        if (graph.numVertices > 1000 && scale < 0.4) {
            step = 3; // Render 1/3 of edges for speed
        }
        if (graph.numVertices > 3000 && scale < 0.2) {
            step = 6; // Render 1/6 of edges
        }

        for (int u = 0; u < graph.numVertices; u += step) {
            // Highlighted nodes get their edges drawn in the next pass
            if (u == hoveredNodeIndex || u == selectedNodeIndex) continue;

            for (int[] edge : graph.neighbors(u)) {
                int v = edge[0];
                if (v == hoveredNodeIndex || v == selectedNodeIndex) continue;
                
                // Frustum culling: check if edge is visible in screen viewport
                if (!isEdgeInViewport(xs[u], ys[u], xs[v], ys[v])) continue;

                if (graph.directed) {
                    drawDirectedEdge(g2, xs[u], ys[u], xs[v], ys[v], 4.0);
                } else {
                    g2.drawLine((int)xs[u], (int)ys[u], (int)xs[v], (int)ys[v]);
                }
            }
        }

        // Draw hovered/selected node edges on top in bright colors
        if (hoveredNodeIndex != -1) {
            g2.setStroke(new BasicStroke(2.0f));
            g2.setColor(COLOR_ACCENT_BLUE);
            for (int[] edge : graph.neighbors(hoveredNodeIndex)) {
                int v = edge[0];
                drawDirectedEdge(g2, xs[hoveredNodeIndex], ys[hoveredNodeIndex], xs[v], ys[v], 8.0);
            }
            // Draw incoming links for hovered too
            g2.setColor(new Color(74, 201, 151)); // green for incoming
            for (int u = 0; u < graph.numVertices; u++) {
                for (int[] edge : graph.neighbors(u)) {
                    if (edge[0] == hoveredNodeIndex) {
                        drawDirectedEdge(g2, xs[u], ys[u], xs[hoveredNodeIndex], ys[hoveredNodeIndex], 8.0);
                    }
                }
            }
        }

        if (selectedNodeIndex != -1) {
            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(COLOR_ACCENT_ORANGE);
            for (int[] edge : graph.neighbors(selectedNodeIndex)) {
                int v = edge[0];
                drawDirectedEdge(g2, xs[selectedNodeIndex], ys[selectedNodeIndex], xs[v], ys[v], 9.0);
            }
            g2.setColor(COLOR_ACCENT_BLUE);
            for (int u = 0; u < graph.numVertices; u++) {
                for (int[] edge : graph.neighbors(u)) {
                    if (edge[0] == selectedNodeIndex) {
                        drawDirectedEdge(g2, xs[u], ys[u], xs[selectedNodeIndex], ys[selectedNodeIndex], 9.0);
                    }
                }
            }
        }

        // Draw highlighted path edges on top
        if (highlightedPath != null && highlightedPath.size() > 1) {
            g2.setStroke(new BasicStroke(4.0f));
            g2.setColor(new Color(255, 209, 102)); // Bright gold/yellow
            for (int i = 0; i < highlightedPath.size() - 1; i++) {
                int u = highlightedPath.get(i);
                int v = highlightedPath.get(i + 1);
                drawDirectedEdge(g2, xs[u], ys[u], xs[v], ys[v], 12.0);
            }
        }
    }

    private void drawDirectedEdge(Graphics2D g2, double x1, double y1, double x2, double y2, double nodeSize) {
        if (!graph.directed) {
            g2.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
            return;
        }

        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx*dx + dy*dy);
        if (len < 5.0) return;

        // Shrink line end to stop at the node border
        double nodeRadius = nodeSize / 2.0;
        double stopX = x2 - (dx / len) * nodeRadius;
        double stopY = y2 - (dy / len) * nodeRadius;

        g2.drawLine((int)x1, (int)y1, (int)stopX, (int)stopY);

        // Draw small arrowhead
        double arrowSize = Math.max(3.0, 6.0 / Math.sqrt(scale));
        arrowSize = Math.min(arrowSize, 12.0);

        double angle = Math.atan2(dy, dx);
        double arrowAngle = Math.PI / 6.0;

        int ax1 = (int) (stopX - arrowSize * Math.cos(angle - arrowAngle));
        int ay1 = (int) (stopY - arrowSize * Math.sin(angle - arrowAngle));
        int ax2 = (int) (stopX - arrowSize * Math.cos(angle + arrowAngle));
        int ay2 = (int) (stopY - arrowSize * Math.sin(angle + arrowAngle));

        g2.drawLine((int)stopX, (int)stopY, ax1, ay1);
        g2.drawLine((int)stopX, (int)stopY, ax2, ay2);
    }

    private boolean isEdgeInViewport(double x1, double y1, double x2, double y2) {
        // Simple viewport clipping
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);

        Point topLeft = toScreenCoords(minX, minY);
        Point bottomRight = toScreenCoords(maxX, maxY);

        return (bottomRight.x >= 0 && topLeft.x <= getWidth() &&
                bottomRight.y >= 0 && topLeft.y <= getHeight());
    }

    private void drawNodes(Graphics2D g2, double[] xs, double[] ys) {
        if (graph == null) return;
        boolean drawLabels = scale >= 0.6;

        for (int i = 0; i < graph.numVertices; i++) {
            if (!globalViewMode && !isNeighborhoodMember(i)) continue;

            double gx = xs[i];
            double gy = ys[i];

            // Viewport culling for nodes
            Point screenPt = toScreenCoords(gx, gy);
            if (screenPt.x < -20 || screenPt.x > getWidth() + 20 ||
                screenPt.y < -20 || screenPt.y > getHeight() + 20) {
                continue;
            }

            // Determine size based on selection, centrality, or degrees
            double baseRadius = 6.0;
            if (graph.numVertices < 200) {
                baseRadius = 9.0;
            }

            // If we have centrality data, scale nodes accordingly
            if (closenessRank != null && i < closenessRank.length) {
                baseRadius += closenessRank[i] * 12.0;
            } else if (betweennessRank != null && i < betweennessRank.length) {
                // Betweenness is often skewed, let's log scale or root scale it
                baseRadius += Math.sqrt(betweennessRank[i]) * 1.5;
            }

            double diameter = baseRadius * 2.0;
            if (i == hoveredNodeIndex) {
                diameter += 6.0;
            }
            if (i == selectedNodeIndex) {
                diameter += 8.0;
            }

            boolean isHighlightedPathNode = (highlightedPath != null && highlightedPath.contains(i));
            if (isHighlightedPathNode) {
                diameter += 4.0;
            }

            // Choose Node Color
            Color nodeColor;
            if (i == selectedNodeIndex) {
                nodeColor = COLOR_ACCENT_ORANGE;
            } else if (i == hoveredNodeIndex) {
                nodeColor = COLOR_ACCENT_BLUE;
            } else if (isHighlightedPathNode) {
                nodeColor = new Color(255, 209, 102); // Gold
            } else {
                if (!globalViewMode) {
                    // Neighborhood: differentiate followed vs followers
                    boolean isFollowed = false;
                    for (int[] edge : graph.neighbors(selectedNodeIndex)) {
                        if (edge[0] == i) { isFollowed = true; break; }
                    }
                    nodeColor = isFollowed ? PALETTE[1] : PALETTE[0];
                } else {
                    int comm = nodeCommunities[i];
                    nodeColor = PALETTE[comm % PALETTE.length];
                }
            }

            // Render node circle
            g2.setColor(nodeColor);
            g2.fillOval((int)(gx - diameter / 2.0), (int)(gy - diameter / 2.0), (int)diameter, (int)diameter);

            // Node border
            if (i == selectedNodeIndex) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval((int)(gx - diameter / 2.0), (int)(gy - diameter / 2.0), (int)diameter, (int)diameter);
            } else {
                g2.setColor(new Color(0, 0, 0, 180));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawOval((int)(gx - diameter / 2.0), (int)(gy - diameter / 2.0), (int)diameter, (int)diameter);
            }

            // Draw Node Text
            boolean forceLabel = (i == hoveredNodeIndex || i == selectedNodeIndex);
            if (forceLabel || (drawLabels && graph.numVertices < 200) || (scale >= 1.5 && drawLabels)) {
                g2.setFont(new Font("Segoe UI", forceLabel ? Font.BOLD : Font.PLAIN, forceLabel ? 12 : 10));
                g2.setColor(forceLabel ? Color.WHITE : COLOR_TEXT);
                
                String name = graph.getName(i);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (int)gx - fm.stringWidth(name) / 2;
                int ty = (int)(gy - diameter / 2.0 - 4);
                
                // Faint drop shadow for node labels
                g2.setColor(new Color(0, 0, 0, 200));
                g2.drawString(name, tx + 1, ty + 1);
                
                g2.setColor(forceLabel ? Color.WHITE : COLOR_TEXT);
                g2.drawString(name, tx, ty);
            }
        }
    }

    private void drawOverlayHUD(Graphics2D g2) {
        // Draw interaction hint in the bottom-left corner
        g2.setColor(new Color(22, 27, 34, 220));
        g2.setStroke(new BasicStroke(1));
        g2.fillRoundRect(20, getHeight() - 50, 480, 32, 6, 6);
        g2.setColor(BORDER_COLOR);
        g2.drawRoundRect(20, getHeight() - 50, 480, 32, 6, 6);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(COLOR_MUTED);
        String hint = "Arrastar mouse para mover | Scroll para Zoom | Click em nó para selecionar";
        if (graph.numVertices >= 500) {
            hint += " | [Modo: " + (globalViewMode ? "Visão Global]" : "Foco Vizinhança]");
        }
        g2.drawString(hint, 32, getHeight() - 30);

        // Draw graph mode watermark on top right
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(230, 237, 243, 60));
        String modeText = globalViewMode ? "GLOBAL VIEW" : "NEIGHBORHOOD FOCUS";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(modeText, getWidth() - fm.stringWidth(modeText) - 20, 25);
    }
}
