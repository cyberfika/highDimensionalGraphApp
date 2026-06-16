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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Botão estilizado para menus de ação em accordion (sidebar).
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Renderizar botão com cor de acento azul (Segoe UI, 12pt)</li>
 *   <li>Implementar efeito hover com fundo semi-transparente</li>
 *   <li>Ajustar padding e cursor durante hover para feedback visual</li>
 *   <li>Alinhar texto à esquerda (para layout de menu)</li>
 * </ul>
 *
 * <p>Características Visuais:
 * <ul>
 *   <li><b>Estado Padrão:</b> Texto azul (#58a6ff), sem fundo</li>
 *   <li><b>Estado Hover:</b> Fundo azul semi-transparente (10% opacidade), padding aumentado</li>
 *   <li><b>Cursor:</b> Muda para HAND_CURSOR indicando clicabilidade</li>
 * </ul>
 *
 * <p>Padrões de Design:
 * <ul>
 *   <li><b>Custom Component:</b> Estende JButton com pintura customizada</li>
 *   <li><b>Single Responsibility:</b> Apenas aparência e estado hover, não lógica</li>
 * </ul>
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see Theme\n * @see GraphGUI
 */
public class MenuActionButton extends JButton {
    private static final Color COLOR_BG_SIDEBAR = new Color(15, 17, 23);
    private static final Color COLOR_ACCENT_BLUE = new Color(88, 166, 255);

    private boolean hovered = false;

    /**
     * Inicializa botão de menu com estilo customizado.
     *
     * <p>Configurações aplicadas:
     * <ul>
     *   <li>Content area preenchimento desabilitado (custom paint)</li>
     *   <li>Focus border desabilitado (aparência limpa)</li>
     *   <li>Alinhamento de texto à esquerda</li>
     *   <li>Fonte: Segoe UI, 12pt, normal</li>
     *   <li>Cor: Azul (#58a6ff)</li>
     *   <li>Cursor: Mão (indicando clicabilidade)</li>
     * </ul>
     *
     * <p>Listeners de mouse implementados para hover:\n * <ul>
     *   <li>Entrada: Aumenta padding esquerdo e desenha fundo azul</li>
     *   <li>Saída: Restaura padding original e remove fundo</li>
     * </ul>
     *
     * @param text rótulo do botão exibido ao usuário
     */
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
                setBorder(BorderFactory.createEmptyBorder(8, 32, 8, 8));
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

    /**
     * Renderiza componente com efeito hover customizado.
     *
     * <p>Se mouse está hovering sobre botão:\n * <ul>
     *   <li>Desenha retângulo arredondado com fundo azul (10% opacidade)</li>
     *   <li>Raio de borda: 4px para visual suave</li>
     * </ul>
     *
     * <p>Antialiasing é aplicado para suavizar bordas.\n * Renderização padrão do botão é preservada (texto, etc).
     *
     * @param g contexto gráfico para renderização
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (hovered) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(88, 166, 255, 25));
            g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 4, 4);
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
