# GraphNet Analyzer — Aplicação de Grafos de Alta Dimensionalidade

![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Swing GUI](https://img.shields.io/badge/Interface-Swing-blue?style=flat-square)
![Licença](https://img.shields.io/badge/Licença-Acadêmica-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Completo-brightgreen?style=flat-square)
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

**GraphNet Analyzer** é uma aplicação completa para análise e visualização de grafos de alta dimensionalidade, modelando a **Rede de Co-aparições Marvel** com **7.153 heróis** e mais de **166.000 conexões reais** extraídas de quadrinhos publicados.

O grafo é **não-direcionado** e **ponderado**: cada aresta liga dois heróis que aparecem juntos em pelo menos um quadrinho, e o peso indica o número de co-aparições. Suporta todas as operações exigidas pelo professor.

### Características Principais

- ✅ **Interface Gráfica (GUI)** com visualização interativa de grafos
- ✅ **Modo Console (CLI)** para operações em lote
- ✅ **Sistema de Recomendação de Seguidores** via DFS
- ✅ **Análise de Redes**: conectividade, ciclos, componentes, centralidade
- ✅ **Importação/Exportação** no formato Pajek
- ✅ **Componentes Customizados**: botões temáticos, painéis colapsáveis, diálogos de progresso
- ✅ **Design Token System** (Theme) para consistência visual

---

## Problema Modelado: Rede de Co-aparições Marvel

Cada vértice representa um **herói Marvel** (nome do personagem nos quadrinhos).
Cada aresta representa uma **co-aparição**: os dois heróis aparecem juntos em pelo menos um quadrinho publicado.
O **peso** da aresta indica quantas vezes eles aparecem juntos — quanto maior o peso, mais forte a conexão.

**Busca de Conexão entre Heróis:**
- Dado um par (origem, destino), o sistema verifica via DFS se existe um caminho de conexões
- Se existe: exibe o caminho completo de co-aparições entre os dois heróis
- Se já estão diretamente conectados: informa
- Se não há caminho: informa que não há conexão

**Dataset:** Marvel Universe Social Network — `data/archive/hero-network.csv`
- **7.153 heróis** (nós) — acima do mínimo de 5.000
- **166.000+ pares únicos** (arestas) — acima do mínimo de 20.000
- Relações reais extraídas de publicações Marvel

---

## Estrutura do Projeto

```
highDimensionalGraphApp/
├── src/
│   └── graph/
│       ├── model/
│       │   └── Graph.java                    # Estrutura de dados (lista de adjacências)
│       ├── algorithm/
│       │   └── GraphAlgorithms.java          # 7 algoritmos + 7 helpers
│       ├── domain/
│       │   └── SocialNetwork.java            # Lógica de recomendação
│       ├── io/
│       │   ├── PajekIO.java                  # Import/export Pajek
│       │   └── HeroNetworkLoader.java        # Carregamento do dataset Marvel (CSV)
│       ├── generator/
│       │   └── GraphGenerator.java           # Geração de grafos
│       ├── gui/
│       │   ├── GraphGUI.java                 # Frame principal (orquestração)
│       │   ├── GraphPanel.java               # Renderização de grafos
│       │   ├── GraphUIState.java             # Gerenciamento centralizado de estado
│       │   ├── GraphLoadingHandler.java      # Carregamento/geração
│       │   ├── AlgorithmHandler.java         # Execução de algoritmos
│       │   ├── SocialNetworkHandler.java     # Operações de rede social
│       │   ├── ProgressDialog.java           # Diálogo de progresso
│       │   ├── Theme.java                    # Design tokens (cores, fontes, espaçamento)
│       │   ├── PrimaryButton.java            # Botão primário customizado
│       │   ├── MenuActionButton.java         # Botão de ação customizado
│       │   ├── CollapsiblePanel.java         # Painel colapsável
│       │   └── VectorIcon.java               # Ícones vetoriais
│       ├── Menu.java                         # Seletor GUI/CLI
│       └── Main.java                         # Ponto de entrada
├── data/
│   └── archive/
│       └── hero-network.csv                  # Dataset Marvel (7.153 heróis, 574k+ co-aparições)
├── pajek/
│   ├── input/                                # Arquivos para importação
│   └── output/                               # Arquivos exportados
├── docs/
│   ├── design.md                             # Diagramas UML
│   ├── change_control/
│   │   └── CHANGELOG.md                      # Histórico de mudanças
│   └── specs/
├── out/                                      # Arquivos compilados
├── .gitignore
└── README.md
```

---

## Pré-requisitos

- **JDK 8 ou superior** — `javac` e `java` devem estar disponíveis no PATH
- O arquivo `data/archive/hero-network.csv` deve estar presente (já incluído no repositório)

Verifique sua instalação Java:

```bash
java -version
javac -version
```

---

## Como Compilar e Executar

### Compilação Completa (GUI + CLI)

A partir da raiz do projeto (`highDimensionalGraphApp/`):

#### Linux/macOS

```bash
javac -encoding UTF-8 -d out \
  src/graph/model/Graph.java \
  src/graph/io/HeroNetworkLoader.java \
  src/graph/io/PajekIO.java \
  src/graph/algorithm/GraphAlgorithms.java \
  src/graph/domain/SocialNetwork.java \
  src/graph/generator/GraphGenerator.java \
  src/graph/gui/Theme.java \
  src/graph/gui/VectorIcon.java \
  src/graph/gui/GraphUIState.java \
  src/graph/gui/ProgressDialog.java \
  src/graph/gui/PrimaryButton.java \
  src/graph/gui/MenuActionButton.java \
  src/graph/gui/CollapsiblePanel.java \
  src/graph/gui/GraphPanel.java \
  src/graph/gui/GraphLoadingHandler.java \
  src/graph/gui/AlgorithmHandler.java \
  src/graph/gui/SocialNetworkHandler.java \
  src/graph/gui/GraphGUI.java \
  src/graph/Menu.java \
  src/graph/Main.java
```

#### Windows (Command Prompt ou PowerShell)

```bat
javac -encoding UTF-8 -d out ^
  src\graph\model\Graph.java ^
  src\graph\io\HeroNetworkLoader.java ^
  src\graph\io\PajekIO.java ^
  src\graph\algorithm\GraphAlgorithms.java ^
  src\graph\domain\SocialNetwork.java ^
  src\graph\generator\GraphGenerator.java ^
  src\graph\gui\Theme.java ^
  src\graph\gui\VectorIcon.java ^
  src\graph\gui\GraphUIState.java ^
  src\graph\gui\ProgressDialog.java ^
  src\graph\gui\PrimaryButton.java ^
  src\graph\gui\MenuActionButton.java ^
  src\graph\gui\CollapsiblePanel.java ^
  src\graph\gui\GraphPanel.java ^
  src\graph\gui\GraphLoadingHandler.java ^
  src\graph\gui\AlgorithmHandler.java ^
  src\graph\gui\SocialNetworkHandler.java ^
  src\graph\gui\GraphGUI.java ^
  src\graph\Menu.java ^
  src\graph\Main.java
```

### Execução

```bash
java -cp out graph.Main
```

Ao iniciar, um menu permite escolher entre:
- **[1] Interface Gráfica (GUI)** — Recomendado (padrão)
- **[2] Modo CLI** — Legado, interativo

---

## Interface Gráfica (GUI) — GraphNet Analyzer

A GUI oferece uma experiência moderna com visualização em tempo real de grafos.

### Componentes Principais

#### 1. **Header** (Barra Superior)
- Título da aplicação
- Botão de carregamento de grafo
- Informações de status (vértices, arestas, conectividade)

#### 2. **Sidebar Esquerdo** (Operações de Grafo)
- **Carregamento**: Rede Marvel, Grafo Aleatório
- **Importação**: Formato Pajek
- **Análise**: Conectividade, Componentes, Ciclos, Euleriano

#### 3. **Painel Central** (GraphPanel)
- Renderização visual do grafo com nós e arestas
- Suporte para pan e zoom interativo
- Cores e estilos customizados

#### 4. **Sidebar Direito** (Resultados)
- Exibição de estatísticas
- Top nós por centralidade
- Resultados de recomendações
- Caminhos encontrados

### Componentes Customizados

| Classe | Responsabilidade |
|--------|------------------|
| **Theme** | Paleta de cores, tipografia, espaçamento (design tokens) |
| **PrimaryButton** | Botão principal com estados (hover, ativo) |
| **MenuActionButton** | Botão de ação com ícones e labels |
| **CollapsiblePanel** | Painel expansível/colapsável com animação |
| **ProgressDialog** | Diálogo de progresso para operações longas |
| **VectorIcon** | Ícones vetoriais escaláveis |
| **GraphPanel** | Canvas para renderização de grafos |

### Handlers (Padrão Facade)

| Handler | Responsabilidade |
|---------|------------------|
| **GraphLoadingHandler** | Carregamento/geração de grafos, import/export |
| **AlgorithmHandler** | Execução de algoritmos de análise |
| **SocialNetworkHandler** | Recomendações, busca de caminhos |

---

## Interface de Linha de Comando (CLI)

Para usuários que preferem modo legado ou operações em lote:

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

Após carregar um grafo:

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

> ⚠️ As opções [5] e [6] são operações pesadas: O(V × (V + E) log V).
> Alertam antes de executar em grafos > 500 vértices.

---

## Formato Pajek

Importar/exportar grafos em formato Pajek.

**Diretórios:**
- Importação: `pajek/input/`
- Exportação: `pajek/output/`

**Exemplo de arquivo:**

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

Use `*Edges` para grafos não-direcionados.

---

## Arquitetura e Design

A aplicação é refatorada segundo os princípios **SOLID**:

### 1. Responsabilidade Única (SRP)

| Classe | Responsabilidade |
|--------|-----------------|
| **Graph** | Estrutura de dados (lista de adjacências) |
| **GraphAlgorithms** | 7 algoritmos de análise + 7 helpers privados |
| **SocialNetwork** | Lógica de domínio (recomendações) |
| **HeroNetworkLoader** | Carregamento do dataset Marvel (CSV com parser próprio) |
| **PajekIO** | Import/export no formato Pajek |
| **GraphGenerator** | Construção de grafos |
| **GraphGUI** | Orquestração de UI (250 linhas) |
| **GraphLoadingHandler** | Carregamento/geração |
| **AlgorithmHandler** | Execução de algoritmos |
| **SocialNetworkHandler** | Operações de rede social |
| **Theme** | Design tokens centralizados |

### 2. Baixo Acoplamento

- **Handlers** recebem dependências via injeção de construtor
- **GraphPanel** trabalha apenas com `Graph` (sem conhecer handlers)
- **Theme** é imutável e fornece constantes estáticas
- Padrão **Facade** em `GraphGUI` para roteamento de eventos

### 3. Padrões de Design

- **MVC**: GraphGUI (View) + Handlers (Controllers) + Graph (Model)
- **Facade**: GraphGUI expõe interface unificada para handlers
- **Observer**: Callbacks via listener interfaces
- **Factory**: GraphGenerator para criação de grafos
- **Strategy**: Múltiplos algoritmos em GraphAlgorithms

---

## Documentação

- **`docs/design.md`** — Diagramas UML, fluxos de dados, decisões arquiteturais
- **`docs/change_control/CHANGELOG.md`** — Histórico completo de refatorações
- **Javadoc em português** em todas as classes (100% de cobertura)

---

## Notas de Implementação

- **Conectividade** de grafos direcionados usa o grafo subjacente não-direcionado (componentes fracamente conectados).
- **Detecção de ciclo** em direcionado usa coloração DFS (branco/cinza/preto).
- **Centralidade de Intermediação** implementa o **Algoritmo de Brandes** com Dijkstra para grafos ponderados.
- **Rede Marvel** carregada de `data/archive/hero-network.csv`: pares de heróis agregados em arestas ponderadas (peso = número de co-aparições em quadrinhos).
- Parser CSV próprio em `HeroNetworkLoader` — sem biblioteca externa — trata campos com vírgula entre aspas duplas.
- Nenhuma biblioteca externa — apenas **Java puro** (`java.util.*` + `javax.swing`), conforme exigido.

---

## Fonte do Dataset

**Marvel Universe Social Network** — disponível publicamente no Kaggle:
https://www.kaggle.com/datasets/csanhueza/the-marvel-universe-social-network

O arquivo `hero-network.csv` contém pares de heróis que co-aparecem em quadrinhos Marvel.
A estrutura do grafo (nós, arestas, pesos) é construída inteiramente pela aplicação a partir desses dados brutos.

---

## Histórico de Refatoração

### Sessão 3 — SRP & Coupling (2026-06-16)

**Violações Corrigidas:**

1. **Graph.java** — God Class de 340 linhas
   - ✅ Extraído: algoritmos → `GraphAlgorithms`
   - ✅ Extraído: lógica de domínio → `SocialNetwork`
   - ✅ Extraído: apresentação → `Main.printGraph()`

2. **GraphGenerator.java** — Responsabilidade misturada
   - ✅ Extraído: I/O de nomes → `NamesLoader`
   - ✅ Refatorado: uso de API pública `addEdge()` em vez de acesso direto a `adj`

3. **Encapsulamento**
   - ✅ `adj` mudou de package-private para private
   - ✅ Novo accessor `neighbors(int u)` para leitura segura

### Sessão 4 — GUI Refactoring (2026-06-16)

**Novas Adições:**

1. **GraphGUI.java** — Interface gráfica principal (250 linhas de orquestração)
2. **Handlers Especializados** (3 classes):
   - `GraphLoadingHandler` — Carregamento/geração
   - `AlgorithmHandler` — Execução de algoritmos
   - `SocialNetworkHandler` — Operações de rede social
3. **Componentes Customizados** (7 classes):
   - `Theme`, `VectorIcon`, `GraphPanel`, `PrimaryButton`, `MenuActionButton`, `CollapsiblePanel`, `ProgressDialog`
4. **State Management**:
   - `GraphUIState` — Gerenciamento centralizado de estado
5. **Menu**:
   - `Menu.chooseConsoleMode()` — Seletor GUI/CLI

**Documentação:**
- ✅ Javadoc em português 100%
- ✅ Design tokens e arquitetura documentados

### Sessão 5 — Dataset Marvel & Fix de Performance (2026-06-19)

**Substituição do Dataset:**

1. **Problema anterior** — `GraphGenerator.generateSocialNetwork()` gerava relações aleatórias entre nomes externos, sem modelar nenhum fenômeno real (vetado pelo professor).
2. **Solução** — Substituído pelo dataset público **Marvel Universe Social Network**:
   - `HeroNetworkLoader.java` criado em `graph.io` — parser CSV próprio sem dependências externas
   - `GraphGenerator.generateSocialNetwork()` removido
   - `GraphLoadingHandler.loadHeroNetwork()` substitui o método anterior
   - Grafo agora é **não-direcionado ponderado** (co-aparições reais, peso = frequência)

**Fix de Performance — Focar Vizinhança:**

- `isNeighborhoodMember()` executava O(V²) por frame de repaint na EDT → freeze com 7.153 nós
- `getNeighborhoodX/Y()` recalculavam `getInNeighbors()` (O(V×E)) a cada repaint e evento de mouse
- **Solução:** cache `neighborhoodCache` (HashSet) + `cachedNeighborhoodX/Y` (arrays), computados uma única vez ao trocar de nó ou ativar o modo
- `isNeighborhoodMember` virou `neighborhoodCache.contains(idx)` → O(1)

---

## Licença

Trabalho acadêmico desenvolvido para a disciplina **Trabalho Colaborativo II** (PUCPR).

Sob supervisão do Prof. **Fabrício Enembreck**.
