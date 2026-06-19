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

import graph.model.Graph;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Carrega a rede de co-aparições de heróis Marvel a partir de um arquivo CSV.
 *
 * <p>Formato esperado: duas colunas {@code hero1,hero2} (com cabeçalho).
 * Nomes com vírgula devem estar entre aspas duplas (formato CSV padrão).
 * Pares repetidos são agregados como peso da aresta (número de co-aparições).
 *
 * <p>O grafo resultante é não-direcionado e ponderado:
 * cada aresta representa uma co-aparição em quadrinhos Marvel,
 * e o peso indica quantas vezes os dois heróis aparecem juntos.
 */
public class HeroNetworkLoader {

    private HeroNetworkLoader() {}

    /**
     * Carrega o arquivo CSV e constrói o grafo de co-aparições.
     *
     * @param filename nome do arquivo (buscado em {@code .} e em {@code data/archive/})
     * @return grafo não-direcionado ponderado, ou {@code null} em caso de erro
     */
    public static Graph load(String filename) {
        File f = resolveFile(filename);
        if (f == null) {
            System.err.println("Hero network file not found: " + filename);
            return null;
        }

        Map<String, Integer> nameToIndex = new LinkedHashMap<>();
        Map<String, Integer> edgeWeights = new HashMap<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            br.readLine(); // pula cabeçalho
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length < 2) continue;
                String h1 = parts[0].trim();
                String h2 = parts[1].trim();
                if (h1.isEmpty() || h2.isEmpty() || h1.equalsIgnoreCase(h2)) continue;

                nameToIndex.putIfAbsent(h1, nameToIndex.size());
                nameToIndex.putIfAbsent(h2, nameToIndex.size());

                int i1 = nameToIndex.get(h1);
                int i2 = nameToIndex.get(h2);
                String key = Math.min(i1, i2) + "-" + Math.max(i1, i2);
                edgeWeights.merge(key, 1, Integer::sum);
            }
        } catch (IOException e) {
            System.err.println("Error reading hero network: " + e.getMessage());
            return null;
        }

        int n = nameToIndex.size();
        Graph g = new Graph(n, false);
        for (Map.Entry<String, Integer> entry : nameToIndex.entrySet()) {
            g.setName(entry.getValue(), entry.getKey());
        }
        for (Map.Entry<String, Integer> entry : edgeWeights.entrySet()) {
            String[] idx = entry.getKey().split("-");
            int u = Integer.parseInt(idx[0]);
            int v = Integer.parseInt(idx[1]);
            g.addEdge(u, v, entry.getValue());
        }

        System.out.printf("Hero network loaded: %d heroes, %d unique connections.%n",
                n, edgeWeights.size());
        return g;
    }

    private static File resolveFile(String filename) {
        for (String path : new String[]{ filename, "data/archive/" + filename }) {
            File f = new File(path);
            if (f.exists()) return f;
        }
        return null;
    }

    /**
     * Faz o parse de uma linha CSV respeitando aspas duplas (campos com vírgula interna).
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
