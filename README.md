# Aplicação de Grafos de Alta Dimensionalidade

![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Licença](https://img.shields.io/badge/Licença-Acadêmica-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Concluído-brightgreen?style=flat-square)
![PUCPR](https://img.shields.io/badge/PUCPR-Ciência%20da%20Computação-003087?style=flat-square)

## Trabalho Colaborativo II — Aplicação de Grafos de Alta Dimensionalidade

Professor: **Fabrício Enembreck** — PUCPR · Bacharelado em Ciência da Computação

---

## Equipe

| Aluno/Autores                    | GitHub |
|----------------------------------|--------|
| Jafte Carneiro Fagundes da Silva | [@cyberfika](https://github.com/cyberfika) |
| Nicolas Hrescak                  | [@NicolasHrescak](https://github.com/NicolasHrescak) |

---

## Sobre o Projeto

Implementação de uma aplicação de grafos de alta dimensionalidade modelando uma **Rede Social** com **5.000 usuários** e mais de **25.000 conexões** de "seguir".

O grafo é **direcionado** (A segue B ≠ B segue A), ponderado (peso 1 por conexão) e suporta todas as operações exigidas pelo professor.

---

## Problema Modelado: Rede Social

Cada vértice representa uma **pessoa** (com nome brasileiro real).
Cada aresta direcionada representa uma relação de **seguir** (como Instagram/Twitter).

**Funcionalidade principal — Sistema de Recomendação de Seguidores:**
- Dado um par (origem, destino), o sistema verifica via DFS se existe um caminho de conexões
- Se existe: recomenda seguir e exibe o caminho completo
- Se já se seguem: informa
- Se não há caminho: não recomenda

---

## Estrutura do Projeto

```
highDimensionalGraphApp/
├── src/
│   └── graph/
│       ├── model/
│       │   └── Graph.java              # Estrutura de dados
│       ├── algorithm/
│       │   └── GraphAlgorithms.java    # Todos os algoritmos
│       ├── domain/
│       │   └── SocialNetwork.java      # Lógica de domínio
│       ├── io/
│       │   ├── PajekIO.java            # I/O formato Pajek
│       │   └── NamesLoader.java        # Carregamento de nomes
│       ├── generator/
│       │   └── GraphGenerator.java     # Geração de grafos
│       └── Main.java                   # Menu principal
├── data/
│   └── names.txt                       # 5.000 nomes brasileiros
├── pajek/
│   ├── input/                          # Arquivos para importação
│   └── output/                         # Arquivos exportados
├── docs/
│   ├── AGENT_HANDOFF.md
│   ├── design.md
│   ├── change_control/
│   │   └── CHANGELOG.md
│   └── specs/
├── out/                                # Arquivos compilados
├── AGENTS.md
├── .gitignore
└── README.md
```

---

## Pré-requisitos

- **JDK 8 ou superior** — `javac` e `java` devem estar disponíveis no PATH
- O arquivo `data/names.txt` deve estar presente (já incluído no repositório)

Verifique sua instalação Java:

```bash
java -version
javac -version
```

---

## Como Compilar e Executar

### 1. Compilar

A partir da raiz do projeto (`highDimensionalGraphApp/`):

```bash
javac -encoding UTF-8 -d out src/graph/model/Graph.java src/graph/io/NamesLoader.java src/graph/io/PajekIO.java src/graph/algorithm/GraphAlgorithms.java src/graph/domain/SocialNetwork.java src/graph/generator/GraphGenerator.java src/graph/Main.java
```

Os arquivos compilados (`.class`) serão colocados em `out/`.

### 2. Executar

```bash
java -cp out graph.Main
```

### Windows (one-liner)

```bat
javac -encoding UTF-8 -d out src\graph\model\Graph.java src\graph\io\NamesLoader.java src\graph\io\PajekIO.java src\graph\algorithm\GraphAlgorithms.java src\graph\domain\SocialNetwork.java src\graph\generator\GraphGenerator.java src\graph\Main.java && java -cp out graph.Main
```

---

## Fluxo de Uso

Ao iniciar, o menu principal é exibido:

```
+================================================+
|   Aplicação de Grafos de Alta Dimensionalidade|
+================================================+
|  [1] Gerar Rede Social (5.000 pessoas)         |
|  [2] Gerar Grafo Aleatório                     |
|  [3] Carregar Grafo (formato Pajek)            |
|  [0] Sair                                      |
+================================================+
```

Após carregar ou gerar um grafo, o menu do grafo fica disponível:

```
 [1] Verificar conectividade
 [2] Exibir componentes
 [3] Verificar Euleriano
 [4] Verificar ciclo
 [5] Centralidade de Proximidade
 [6] Centralidade de Intermediação
 [7] Sistema de Recomendação de Seguidores
 [8] Imprimir grafo (primeiros 20 vértices)
 [9] Exportar para Pajek
 [0] Voltar ao menu principal
```

> As opções [5] e [6] avisam antes de executarem em grafos grandes (> 500 vértices) porque
> ambas têm complexidade O(V × (V + E) log V) e podem demorar vários minutos na rede de 5.000 nós.

---

## Formato Pajek

Arquivos para importação devem ser colocados em `pajek/input/`. Arquivos exportados são salvos em `pajek/output/`.

```
*Vertices 3
1 "Alice"
2 "Bob"
3 "Carol"
*Arcs
1 2 1
2 3 1
1 3 1
```

Use `*Edges` em vez de `*Arcs` para grafos não-direcionados.

---

## Notas de Implementação

- **Conectividade** de grafos direcionados usa o grafo subjacente não-direcionado (componentes fracamente conectados), conforme especificado pelo professor.
- **Detecção de ciclo** em direcionado usa coloração DFS (branco/cinza/preto) — mais correto que a abordagem de "pai" usada para não-direcionados.
- **Centralidade de Intermediação** implementa o **Algoritmo de Brandes** com Dijkstra para suportar grafos ponderados.
- A **Rede Social** gera entre 25.000 e 40.000 arestas (cada pessoa segue 5 a 8 outras), superando o requisito mínimo de 20.000.
- Nenhuma biblioteca externa de grafos é usada — apenas Java puro (`java.util.*`), conforme exigido pela disciplina.

---

## Arquitetura e Design

A aplicação segue os princípios de **Responsabilidade Única (SRP)** e **Baixo Acoplamento**:

- **`graph.model.Graph`** — estrutura de dados pura (lista de adjacências)
- **`graph.algorithm.GraphAlgorithms`** — todos os algoritmos (sem I/O ou lógica de domínio)
- **`graph.domain.SocialNetwork`** — regras de negócio da rede social
- **`graph.io.NamesLoader` + `graph.io.PajekIO`** — leitura/escrita de arquivos
- **`graph.generator.GraphGenerator`** — construção de grafos
- **`graph.Main`** — interface de console (sem lógica de negócio)

Veja `docs/design.md` para diagramas UML completos.
