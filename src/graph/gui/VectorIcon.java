package graph.gui;

import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Custom vector icons drawn using Graphics2D to ensure premium design
 * and compatibility across platforms without relying on system font emoji support.
 */
public class VectorIcon implements Icon {
    public enum Type {
        PLUS, GENERATE, ANALYZE, CENTRALITY, SOCIAL, FILE, VERTEX, EDGE, COMPONENT, CLOCK
    }

    private final Type type;
    private final int size;
    private final Color color;

    public VectorIcon(Type type, int size, Color color) {
        this.type = type;
        this.size = size;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        switch (type) {
            case PLUS -> {
                int thickness = Math.max(2, size / 6);
                g2.fillRect(x + (size - thickness) / 2, y + 2, thickness, size - 4);
                g2.fillRect(x + 2, y + (size - thickness) / 2, size - 4, thickness);
            }
            case GENERATE -> {
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x + 2, y + 2, size - 5, size - 5);
                g2.fillOval(x + size / 2 - 2, y + 2, 4, 4);
                g2.fillOval(x + size / 2 - 2, y + size - 6, 4, 4);
                g2.fillOval(x + 2, y + size / 2 - 2, 4, 4);
                g2.fillOval(x + size - 6, y + size / 2 - 2, 4, 4);
            }
            case ANALYZE -> {
                int r = size * 2 / 3 - 2;
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(x + 2, y + 2, r, r);
                g2.drawLine(x + r - 2, y + r - 2, x + size - 3, y + size - 3);
            }
            case CENTRALITY -> {
                Path2D star = new Path2D.Double();
                star.moveTo(x + size / 2.0, y + 2);
                star.lineTo(x + size * 0.63, y + size * 0.35);
                star.lineTo(x + size * 0.95, y + size * 0.35);
                star.lineTo(x + size * 0.70, y + size * 0.57);
                star.lineTo(x + size * 0.79, y + size * 0.92);
                star.lineTo(x + size / 2.0, y + size * 0.72);
                star.lineTo(x + size * 0.21, y + size * 0.92);
                star.lineTo(x + size * 0.30, y + size * 0.57);
                star.lineTo(x + size * 0.05, y + size * 0.35);
                star.lineTo(x + size * 0.37, y + size * 0.35);
                star.closePath();
                g2.fill(star);
            }
            case SOCIAL -> {
                g2.setStroke(new BasicStroke(1.5f));
                // Center node
                g2.fillOval(x + size / 2 - 3, y + size / 2 - 3, 6, 6);
                // Surrounding nodes
                g2.fillOval(x + 2, y + 2, 4, 4);
                g2.fillOval(x + size - 6, y + 3, 4, 4);
                g2.fillOval(x + 3, y + size - 7, 4, 4);
                g2.fillOval(x + size - 7, y + size - 7, 4, 4);
                // Connections
                g2.drawLine(x + 4, y + 4, x + size / 2, y + size / 2);
                g2.drawLine(x + size - 4, y + 5, x + size / 2, y + size / 2);
                g2.drawLine(x + 5, y + size - 5, x + size / 2, y + size / 2);
                g2.drawLine(x + size - 5, y + size - 5, x + size / 2, y + size / 2);
            }
            case FILE -> {
                g2.setStroke(new BasicStroke(1.5f));
                // Floppy disk representation
                g2.drawRect(x + 2, y + 2, size - 4, size - 4);
                g2.fillRect(x + 5, y + 2, size - 10, 4);
                g2.drawRect(x + 4, y + size - 6, size - 8, 4);
            }
            case VERTEX -> {
                g2.fillOval(x + 3, y + 3, size - 6, size - 6);
                g2.setColor(color.darker());
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawOval(x + 3, y + 3, size - 6, size - 6);
            }
            case EDGE -> {
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawLine(x + 2, y + size / 2, x + size - 2, y + size / 2);
            }
            case COMPONENT -> {
                g2.fillOval(x + 2, y + 3, size / 2 - 2, size / 2 - 2);
                g2.fillOval(x + size / 2 + 1, y + size / 2 + 1, size / 2 - 2, size / 2 - 2);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawLine(x + size / 4 + 1, y + size / 4 + 1, x + 3 * size / 4, y + 3 * size / 4);
            }
            case CLOCK -> {
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x + 2, y + 2, size - 4, size - 4);
                g2.drawLine(x + size / 2, y + size / 2, x + size / 2, y + size / 2 - 4);
                g2.drawLine(x + size / 2, y + size / 2, x + size / 2 + 3, y + size / 2);
            }
        }

        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
