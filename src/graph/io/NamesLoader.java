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

package graph.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Carrega uma lista de nomes a partir de um arquivo de texto (um nome por linha).
 * Responsabilidade exclusiva: leitura de arquivo de nomes — sem lógica de grafo.
 */
public class NamesLoader {

    private NamesLoader() {}

    /**
     * Tenta carregar o arquivo a partir de múltiplos caminhos relativos.
     *
     * @param filename nome do arquivo (ex: "names.txt")
     * @return lista de nomes, ou null em caso de erro
     */
    public static List<String> load(String filename) {
        String[] attempts = {
            filename,
            "data/" + filename
        };
        for (String attempt : attempts) {
            File f = new File(attempt);
            if (f.exists()) {
                List<String> names = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null)
                        if (!line.isBlank()) names.add(line.trim());
                } catch (IOException e) {
                    System.err.println("Error reading names file: " + e.getMessage());
                    return null;
                }
                System.out.println("Names loaded from: " + f.getPath());
                return names;
            }
        }
        System.err.println("Names file not found: " + filename);
        System.err.println("Place '" + filename + "' in the data/ folder.");
        return null;
    }
}
