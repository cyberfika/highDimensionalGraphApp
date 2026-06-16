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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Painel colapsável reutilizável com cabeçalho expansível e conteúdo variável.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Renderizar cabeçalho com ícone, título e indicador (+ ou −)</li>
 *   <li>Gerenciar estado expandido/colapsado</li>
 *   <li>Animar visibilidade de conteúdo ao expandir/colar</li>
 *   <li>Fornecer efeito visual de painel accordion</li>
 * </ul>
 *
 * <p>Características Visuais:
 * <ul>
 *   <li><b>Cabeçalho:</b> Fundo escuro, borda inferior, ícone à esquerda, indicador à direita</li>
 *   <li><b>Indicador:</b> \"+\" quando colapsado, \"−\" quando expandido</li>
 *   <li><b>Conteúdo:</b> Painel interno ocultável com visibilidade toggleada</li>
 *   <li><b>Cursor:</b> Mão sobre cabeçalho, indicando clicabilidade</li>
 * </ul>
 *
 * <p>Padrões de Design:
 * <ul>
 *   <li><b>Composite:</b> Combina cabeçalho customizado com painel de conteúdo</li>
 *   <li><b>Single Responsibility:</b> Apenas UI de accordion, não lógica</li>
 * </ul>
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see Theme\n * @see GraphGUI
 */
public class CollapsiblePanel extends JPanel {
    private static final Color COLOR_BG_SIDEBAR = new Color(15, 17, 23);
    private static final Color COLOR_BG_HEADER = new Color(22, 27, 34);
    private static final Color COLOR_BORDER = new Color(48, 54, 61);
    private static final Color COLOR_TEXT_LIGHT = new Color(230, 237, 243);
    private static final Color COLOR_TEXT_MUTED = new Color(139, 148, 158);

    private final JButton headerButton;
    private final JPanel contentPanel;
    private boolean expanded;

    /**
     * Inicializa painel colapsável com cabeçalho e conteúdo.
     *
     * <p>Cria estrutura de accordion com:\n * <ul>
     *   <li>Cabeçalho clicável com ícone e título</li>
     *   <li>Painel de conteúdo que pode ser ocultado/exibido</li>
     *   <li>Indicador visual (+/−) para estado colapsado/expandido</li>
     * </ul>
     *
     * <p>Estado inicial pode ser configurado via {@code startExpanded}.\n *
     * <p>Cliques no cabeçalho alternam visibilidade do conteúdo
     * via {@link #toggle()}.
     *
     * @param title texto do cabeçalho\n     * @param icon ícone exibido à esquerda do título (pode ser {@code null})\n     * @param content painel interno de conteúdo (pode ser qualquer JPanel com componentes)\n     * @param startExpanded {@code true} para iniciar expandido, {@code false} para colapsado\n     */
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

                g2.setColor(COLOR_BG_HEADER);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(COLOR_BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

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

    /**
     * Alterna o estado de expansão/colapso do painel.
     *
     * <p>Executa:\n * <ul>
     *   <li>Inverte flag {@code expanded}</li>
     *   <li>Atualiza visibilidade do painel de conteúdo</li>
     *   <li>Revalida layout (recalcula preferências de tamanho)</li>
     *   <li>Repinta cabeçalho (indicador muda de +/−)</li>
     * </ul>
     *
     * <p>Chamado automaticamente ao clicar no cabeçalho.\n     */
    private void toggle() {
        expanded = !expanded;
        contentPanel.setVisible(expanded);
        revalidate();
        repaint();
    }
}
