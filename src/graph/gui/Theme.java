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

import java.awt.*;

/**
 * Centraliza todos os tokens de design da aplicação GraphNet Analyzer.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Definir paleta de cores (backgrounds, borders, text, accents)</li>
 *   <li>Padronizar tipografia (fontes, tamanhos)</li>
 *   <li>Centralizar espaçamento e sizing</li>
 *   <li>Fornecer constantes de interação (zoom, pan)</li>
 *   <li>Disponibilizar métodos utilitários para criação de fontes e manipulação de cores</li>
 * </ul>
 *
 * <p>Esta classe atua como fonte única de verdade para o tema visual.
 * Alterações aqui afetam toda a interface, facilitando mudanças de tema
 * (ex: dark/light mode) sem refatorar a aplicação.
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see GraphGUI
 * @see GraphPanel
 */
public class Theme {

    // =========================================================================
    // COLOR PALETTE — Background
    // =========================================================================

    public static final Color BG_DARK = new Color(13, 17, 23);
    public static final Color BG_SIDEBAR = new Color(15, 17, 23);
    public static final Color BG_HEADER = new Color(22, 27, 34);
    public static final Color BG_SURFACE = new Color(30, 36, 44);

    // =========================================================================
    // COLOR PALETTE — Borders & Dividers
    // =========================================================================

    public static final Color BORDER = new Color(48, 54, 61);

    // =========================================================================
    // COLOR PALETTE — Text
    // =========================================================================

    public static final Color TEXT_LIGHT = new Color(230, 237, 243);
    public static final Color TEXT_MUTED = new Color(139, 148, 158);

    // =========================================================================
    // COLOR PALETTE — Accents (Brand Colors)
    // =========================================================================

    public static final Color ACCENT_BLUE = new Color(88, 166, 255);
    public static final Color ACCENT_ORANGE = new Color(251, 133, 0);
    public static final Color ACCENT_ORANGE_LIGHT = new Color(255, 183, 3);

    // =========================================================================
    // COLOR PALETTE — Semantic Colors (Graph Visualization)
    // =========================================================================

    public static final Color SUCCESS_GREEN = new Color(76, 201, 151);
    public static final Color ERROR_RED = new Color(255, 107, 107);
    public static final Color WARNING_YELLOW = new Color(255, 209, 102);
    public static final Color INFO_TEAL = new Color(76, 201, 240);

    // =========================================================================
    // COLOR PALETTE — Node Colors (Community Palette)
    // =========================================================================

    public static final Color[] NODE_PALETTE = {
        new Color(88, 166, 255),      // Blue
        new Color(251, 133, 0),       // Orange
        new Color(76, 201, 240),      // Light Blue
        new Color(74, 201, 151),      // Teal
        new Color(247, 37, 133),      // Pink
        new Color(114, 9, 183),       // Purple
        new Color(255, 209, 102),     // Yellow
        new Color(6, 214, 160),       // Mint
        new Color(239, 71, 111),      // Coral
        new Color(156, 102, 255)      // Light Purple
    };

    // =========================================================================
    // TYPOGRAPHY — Font Families
    // =========================================================================

    public static final String FONT_FAMILY_PRIMARY = "Segoe UI";
    public static final String FONT_FAMILY_MONO = "Consolas";

    // =========================================================================
    // TYPOGRAPHY — Font Sizes
    // =========================================================================

    public static final int FONT_SIZE_LARGE = 18;
    public static final int FONT_SIZE_MEDIUM = 13;
    public static final int FONT_SIZE_NORMAL = 12;
    public static final int FONT_SIZE_SMALL = 11;
    public static final int FONT_SIZE_TINY = 10;

    // =========================================================================
    // SPACING & SIZING
    // =========================================================================

    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 12;
    public static final int SPACING_LG = 16;
    public static final int SPACING_XL = 24;

    public static final int SIDEBAR_WIDTH_LEFT = 280;
    public static final int SIDEBAR_WIDTH_RIGHT = 320;
    public static final int HEADER_HEIGHT = 64;

    // =========================================================================
    // BORDER RADIUS & STROKE
    // =========================================================================

    public static final int BORDER_RADIUS_SM = 4;
    public static final int BORDER_RADIUS_MD = 6;
    public static final int BORDER_RADIUS_LG = 8;

    public static final float STROKE_THIN = 1.0f;
    public static final float STROKE_NORMAL = 1.5f;
    public static final float STROKE_THICK = 2.0f;
    public static final float STROKE_VERY_THICK = 2.5f;

    // =========================================================================
    // COMPONENT SIZING
    // =========================================================================

    public static final int BUTTON_HEIGHT = 30;
    public static final int BUTTON_SMALL_HEIGHT = 28;
    public static final int LOGO_SIZE = 32;
    public static final int AVATAR_SIZE = 44;
    public static final int ICON_SIZE_SM = 11;
    public static final int ICON_SIZE_MD = 12;
    public static final int ICON_SIZE_LG = 14;

    // =========================================================================
    // GRAPH VISUALIZATION CONSTANTS
    // =========================================================================

    public static final double NODE_BASE_RADIUS_SMALL = 6.0;
    public static final double NODE_BASE_RADIUS_MEDIUM = 9.0;
    public static final double NEIGHBORHOOD_RADIUS_INNER = 200.0;
    public static final double NEIGHBORHOOD_RADIUS_OUTER = 380.0;
    public static final double SPIRAL_SPACING_SMALL = 18.0;
    public static final double SPIRAL_SPACING_LARGE = 28.0;

    // =========================================================================
    // INTERACTION CONSTANTS
    // =========================================================================

    public static final double ZOOM_FACTOR = 0.85;
    public static final double ZOOM_MIN = 0.005;
    public static final double ZOOM_MAX = 20.0;
    public static final double ZOOM_THRESHOLD_LABELS = 0.6;
    public static final double ZOOM_THRESHOLD_EDGES = 0.15;
    public static final double ZOOM_THRESHOLD_SUPER = 0.04;
    public static final double ZOOM_THRESHOLD_SAMPLE = 0.4;
    public static final double ZOOM_THRESHOLD_SAMPLE_2 = 0.2;

    public static final double EDGE_SAMPLE_STEP_1 = 3;
    public static final double EDGE_SAMPLE_STEP_2 = 6;

    // =========================================================================
    // MÉTODOS UTILITÁRIOS — Criação de Fontes
    // =========================================================================

    /**
     * Cria uma fonte com a família primária (Segoe UI).
     *
     * @param style estilo da fonte (Font.PLAIN, Font.BOLD, etc)
     * @param size tamanho em pontos
     * @return objeto Font configurado
     */
    public static Font createFont(int style, int size) {
        return new Font(FONT_FAMILY_PRIMARY, style, size);
    }

    /**
     * Cria uma fonte com família customizável.
     *
     * @param family nome da família de fontes
     * @param style estilo da fonte
     * @param size tamanho em pontos
     * @return objeto Font configurado
     */
    public static Font createFont(String family, int style, int size) {
        return new Font(family, style, size);
    }

    /** @return Fonte grande (18pt, negrita) */
    public static Font fontLarge() {
        return createFont(Font.BOLD, FONT_SIZE_LARGE);
    }

    /** @return Fonte média (13pt, normal) */
    public static Font fontMedium() {
        return createFont(Font.PLAIN, FONT_SIZE_MEDIUM);
    }

    /** @return Fonte padrão (12pt, normal) */
    public static Font fontNormal() {
        return createFont(Font.PLAIN, FONT_SIZE_NORMAL);
    }

    /** @return Fonte pequena (11pt, normal) */
    public static Font fontSmall() {
        return createFont(Font.PLAIN, FONT_SIZE_SMALL);
    }

    /** @return Fonte minúscula (10pt, normal) */
    public static Font fontTiny() {
        return createFont(Font.PLAIN, FONT_SIZE_TINY);
    }

    /**
     * Cria fonte negrita com tamanho customizável.
     *
     * @param size tamanho em pontos
     * @return Fonte negrita
     */
    public static Font fontBold(int size) {
        return createFont(Font.BOLD, size);
    }

    // =========================================================================
    // MÉTODOS UTILITÁRIOS — Manipulação de Cores
    // =========================================================================

    /**
     * Cria uma cor com alfa customizável baseada em cor existente.
     *
     * @param color cor base
     * @param alpha transparência (0-255, onde 0 é invisível e 255 é opaco)
     * @return nova cor com alpha aplicado
     */
    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * Obtém cor de comunidade da paleta baseada em ID.
     *
     * <p>Útil para colorir nós de comunidades diferentes no grafo.
     *
     * @param communityId identificador único da comunidade
     * @return cor correspondente à comunidade (ciclada pela paleta)
     */
    public static Color getCommunityColor(int communityId) {
        return NODE_PALETTE[communityId % NODE_PALETTE.length];
    }
}
