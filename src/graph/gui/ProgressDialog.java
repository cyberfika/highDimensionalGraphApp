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
 * Diálogo de progresso para operações de longa duração em background.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Executar tarefa assíncrona sem congelar UI (Event Dispatch Thread)</li>
 *   <li>Exibir feedback visual (modal dialog + barra de progresso indeterminada)</li>
 *   <li>Fechar diálogo quando operação completa</li>
 * </ul>
 *
 * <p>Padrões de Design:
 * <ul>
 *   <li><b>Template Method:</b> Subclass sobrescreve {@link #doInBackground()} com lógica específica</li>
 *   <li><b>SwingWorker:</b> Thread-safe background execution no Swing</li>
 *   <li><b>Modal Dialog:</b> Bloqueia interação com janela proprietária durante execução</li>
 * </ul>
 *
 * <p>Uso típico:\n * <pre>
 * ProgressDialog worker = new ProgressDialog(frame, \"Carregando...\") {
 *     @Override\n *     protected Void doInBackground() throws Exception {
 *         // Operação longa (ex: ler arquivo, processar dados)\n *         return null;
 *     }\n *     @Override\n *     protected void done() {
 *         super.done();  // Fecha diálogo\n *         // Handle result\n *     }
 * };
 * worker.executeWithDialog();  // Exibe diálogo e executa\n * </pre>
 *
 * @author Jafte Carneiro Fagundes da Silva
 * @author Nicolas Hrescak
 * @see SwingWorker
 * @see GraphLoadingHandler
 * @see AlgorithmHandler
 */
public abstract class ProgressDialog extends SwingWorker<Void, Void> {
    private static final Color COLOR_BG_HEADER = new Color(22, 27, 34);
    private static final Color COLOR_BG_DARK = new Color(13, 17, 23);
    private static final Color COLOR_BORDER = new Color(48, 54, 61);
    private static final Color COLOR_TEXT_LIGHT = new Color(230, 237, 243);
    private static final Color COLOR_ACCENT_ORANGE = new Color(251, 133, 0);

    private final JDialog dialog;

    /**
     * Inicializa diálogo de progresso com mensagem customizável.
     *
     * <p>Cria um JDialog modal e não decorado com:
     * <ul>
     *   <li>Mensagem descritiva da operação</li>
     *   <li>Barra de progresso indeterminada (animação contínua)</li>
     *   <li>Tamanho fixo 240x80 pixels, centralizado na janela proprietária</li>
     * </ul>
     *
     * @param owner janela proprietária (proprietária do diálogo modal)
     * @param message mensagem descritiva da operação (ex: \"Carregando arquivo...\")\n     */
    public ProgressDialog(JFrame owner, String message) {
        dialog = new JDialog(owner, "Processando", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(COLOR_BG_HEADER);
        dialog.setSize(240, 80);
        dialog.setLocationRelativeTo(owner);
        dialog.setUndecorated(true);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(COLOR_BG_HEADER);
        p.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

        JLabel l = new JLabel("  " + message);
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

    /**
     * Chamado automaticamente quando {@link #doInBackground()} completa.
     *
     * <p>Subclasses podem sobrescrever para adicionar lógica pós-execução:\n * <pre>
     * @Override\n * protected void done() {
     *     super.done();  // Importante: fecha diálogo\n *     // Seu código aqui\n * }
     * </pre>
     */
    @Override
    protected void done() {
        dialog.dispose();
    }

    /**
     * Executa a tarefa e exibe o diálogo de progresso.
     *
     * <p>Responsabilidades:
     * <ul>
     *   <li>Inicia execução assíncrona via {@link #execute()} (herança de SwingWorker)</li>
     *   <li>Torna diálogo visível e modal, bloqueando janela proprietária</li>
     *   <li>Quando tarefa completa, {@link #done()} fecha o diálogo automaticamente</li>
     * </ul>
     *
     * <p>É modal, portanto código executado APÓS este método será
     * chamado apenas após o diálogo ser fechado.\n     */
    public void executeWithDialog() {
        super.execute();
        dialog.setVisible(true);
    }
}
