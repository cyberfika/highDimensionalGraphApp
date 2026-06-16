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

/**
 * Botão de ação primária com fundo gradiente laranja.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Renderizar gradiente laranja (escuro para claro)</li>
 *   <li>Implementar efeitos visuais para estados do botão (hover, pressed)</li>
 *   <li>Fornecer aparência de botão primário com destaque visual</li>
 * </ul>
 *
 * <p>Características Visuais:
 * <ul>
 *   <li><b>Estado Padrão:</b> Gradiente de laranja escuro (#fb8500) para laranja claro (#ffb703)</li>
 *   <li><b>Estado Hover:</b> Cores ficam mais claras (brighter)</li>
 *   <li><b>Estado Pressed:</b> Cores ficam mais escuras (darker)</li>
 *   <li><b>Forma:</b> Retângulo arredondado com raio 8px</li>
 *   <li><b>Texto:</b> Negrita, 13pt, cor escura (contraste)</li>
 * </ul>
 *
 * <p>Padrões de Design:
 * <ul>
 *   <li><b>Custom Rendering:</b> Sobrescreve {@link #paintComponent(Graphics)} para gradiente</li>
 *   <li><b>Single Responsibility:</b> Apenas renderização com efeitos visuais</li>
 * </ul>
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see Theme\n * @see GraphGUI
 */
public class PrimaryButton extends JButton {
    private static final Color COLOR_ACCENT_ORANGE = new Color(251, 133, 0);
    private static final Color COLOR_ACCENT_ORANGE_LIGHT = new Color(255, 183, 3);

    /**
     * Inicializa botão primário com estilo gradiente.
     *
     * <p>Configurações aplicadas:
     * <ul>
     *   <li>Renderização customizada (fill area desabilitado)</li>
     *   <li>Focus border desabilitado para aparência limpa</li>
     *   <li>Fonte: Segoe UI, 13pt, bold</li>
     *   <li>Cor do texto: Escura (#0f1117) para contraste com gradiente</li>
     *   <li>Cursor: Mão (indicando clicabilidade)</li>
     *   <li>Padding: 10px vertical, 12px horizontal</li>
     * </ul>
     *
     * @param text rótulo do botão\n     * @param icon ícone exibido ao lado do texto (pode ser {@code null})\n     */
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

    /**
     * Renderiza botão com gradiente laranja e efeitos de estado.
     *
     * <p>Fluxo de Renderização:
     * <ol>
     *   <li>Detecta estado atual do botão (padrão, hover, ou pressed)</li>
     *   <li>Ajusta cores do gradiente baseado no estado</li>
     *   <li>Desenha retângulo arredondado com gradiente GradientPaint</li>
     *   <li>Renderiza componente padrão (texto e ícone)</li>
     * </ol>
     *
     * <p>Mudanças de Cor por Estado:\n * <ul>
     *   <li><b>Padrão:</b> Laranja escuro → Laranja claro</li>
     *   <li><b>Hover:</b> Cores ficam 20% mais claras (brighter)</li>
     *   <li><b>Pressed:</b> Cores ficam 20% mais escuras (darker)</li>
     * </ul>
     *
     * <p>Antialiasing é aplicado para bordas suaves.\n *
     * @param g contexto gráfico para renderização\n     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color c1 = COLOR_ACCENT_ORANGE;
        Color c2 = COLOR_ACCENT_ORANGE_LIGHT;

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
