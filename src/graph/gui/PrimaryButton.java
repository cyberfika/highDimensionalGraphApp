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
 * Primary action button with gradient background.
 * Respects Single Responsibility Principle — only handles button rendering with gradient.
 */
public class PrimaryButton extends JButton {
    private static final Color COLOR_ACCENT_ORANGE = new Color(251, 133, 0);
    private static final Color COLOR_ACCENT_ORANGE_LIGHT = new Color(255, 183, 3);

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
