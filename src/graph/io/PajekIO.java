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
import java.util.Scanner;

/**
 * Serialização e desserialização de grafos no formato Pajek (.txt / .net).
 *
 * <p>Responsabilidade exclusiva: ler e escrever arquivos no formato Pajek —
 * sem algoritmos de grafos e sem lógica de domínio.
 *
 * <p>Formato Pajek suportado:
 * <pre>
 *   *Vertices N
 *   1 "Name 1"
 *   2 "Name 2"
 *   *Edges         (grafo não-direcionado)
 *   *Arcs          (grafo direcionado)
 *   u v weight
 * </pre>
 *
 * <p>Arquivos para importação devem ser colocados em {@value #INPUT_DIR}.
 * Arquivos exportados são salvos em {@value #OUTPUT_DIR}.
 */
public class PajekIO {

    /** Diretório onde os arquivos Pajek para importação devem ser colocados. */
    private static final String INPUT_DIR  = "pajek/input";

    /** Diretório onde os arquivos Pajek exportados são salvos. */
    private static final String OUTPUT_DIR = "pajek/output";

    /** Construtor privado — classe utilitária, não deve ser instanciada. */
    private PajekIO() {}

    /**
     * Exporta o grafo para um arquivo no formato Pajek.
     * Grafos direcionados usam a seção {@code *Arcs}; não-direcionados usam {@code *Edges}.
     * Para grafos não-direcionados, cada aresta é escrita uma única vez (u {@literal <} v).
     *
     * @param g        grafo a exportar
     * @param filename nome do arquivo de saída (dentro de {@value #OUTPUT_DIR})
     */
    public static void export(Graph g, String filename) {
        File dir = new File(OUTPUT_DIR);
        if (!dir.mkdirs() && !dir.exists()) {
            System.err.println("Error: could not create output directory " + OUTPUT_DIR);
            return;
        }
        File file = new File(dir, filename);

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {

            pw.println("*Vertices " + g.numVertices);
            for (int i = 0; i < g.numVertices; i++)
                pw.println((i + 1) + " \"" + g.getName(i) + "\"");

            pw.println(g.directed ? "*Arcs" : "*Edges");
            for (int u = 0; u < g.numVertices; u++) {
                for (int[] e : g.neighbors(u)) {
                    // Para não-direcionado evita duplicar (u < destino)
                    if (g.directed || u < e[0])
                        pw.println((u + 1) + " " + (e[0] + 1) + " " + e[1]);
                }
            }

            System.out.println("Graph exported to: " + file.getPath());
        } catch (IOException e) {
            System.err.println("Error exporting Pajek: " + e.getMessage());
        }
    }

    /**
     * Importa um grafo a partir de um arquivo no formato Pajek.
     * O tipo do grafo (direcionado/não-direcionado) é detectado automaticamente
     * pela presença de {@code *Arcs} (direcionado) ou {@code *Edges} (não-direcionado).
     * Linhas em branco e comentários iniciados com {@code %} são ignorados.
     *
     * @param filename nome do arquivo dentro de {@value #INPUT_DIR}
     * @return grafo importado, ou {@code null} em caso de erro ou arquivo não encontrado
     */
    public static Graph importFrom(String filename) {
        File file = new File(INPUT_DIR, filename);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getPath());
            return null;
        }

        try (Scanner sc = new Scanner(file, StandardCharsets.UTF_8)) {
            Graph g = null;
            boolean readingVertices = false;
            boolean readingEdges    = false;

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty() || line.startsWith("%")) continue;
                String low = line.toLowerCase();

                if (low.startsWith("*vertices")) {
                    int n = Integer.parseInt(line.split("\\s+")[1]);
                    g = new Graph(n, false); // direcionado definido abaixo
                    readingVertices = true;
                    readingEdges    = false;
                } else if (low.startsWith("*arcs")) {
                    if (g != null) g.directed = true;
                    readingVertices = false;
                    readingEdges    = true;
                } else if (low.startsWith("*edges")) {
                    readingVertices = false;
                    readingEdges    = true;
                } else if (readingVertices && g != null) {
                    String[] parts = line.split(" ", 2);
                    int idx = Integer.parseInt(parts[0]) - 1;
                    String name = parts.length > 1 ? parts[1].replace("\"", "").trim() : "V" + idx;
                    g.setName(idx, name);
                } else if (readingEdges) {
                    // g não pode ser null aqui: *vertices sempre precede *arcs/*edges
                    String[] parts = line.split("\\s+");
                    int u      = Integer.parseInt(parts[0]) - 1;
                    int v      = Integer.parseInt(parts[1]) - 1;
                    int weight = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;
                    g.addEdge(u, v, weight);
                }
            }
            return g;
        } catch (Exception e) {
            System.err.println("Error importing Pajek: " + e.getMessage());
            return null;
        }
    }
}
