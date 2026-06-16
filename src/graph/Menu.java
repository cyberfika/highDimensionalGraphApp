package graph;

import java.util.Scanner;

/**
 * Menu de inicialização para permitir a escolha do modo de execução da aplicação.
 */
public class Menu {

    private Menu() {}

    /**
     * Apresenta um menu no console para o usuário escolher entre a nova GUI
     * e o modo legado (CLI).
     *
     * @return {@code true} se o usuário escolheu o modo legado (CLI);
     *         {@code false} caso tenha escolhido a interface gráfica (GUI).
     */
    public static boolean chooseConsoleMode() {
        System.out.println("+================================================+");
        System.out.println("|       High-Dimensional Graph Application       |");
        System.out.println("+================================================+");
        System.out.println("| Escolha o modo de execução:                    |");
        System.out.println("|   [1] Interface Gráfica (GUI) - Recomendado    |");
        System.out.println("|   [2] Modo Linha de Comando (CLI) - Legado     |");
        System.out.println("+================================================+");
        System.out.print("Opção [1]: ");

        // Não fechamos este Scanner para não fechar o System.in subjacente,
        // o que quebraria a execução posterior do menu clássico.
        Scanner tempScanner = new Scanner(System.in, "UTF-8");
        String choice = tempScanner.nextLine().trim();

        if (choice.equals("2")) {
            System.out.println("\nIniciando no modo CLI Legado...");
            return true;
        }

        System.out.println("\nIniciando no modo GUI...");
        return false;
    }
}
