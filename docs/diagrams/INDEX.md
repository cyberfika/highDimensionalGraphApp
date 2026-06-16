# Índice de Diagramas UML — GraphNet Analyzer

> **Formato:** PlantUML (.puml)
> **Visualizadores:** Online em https://www.plantuml.com/plantuml/uml/ ou VS Code + Plugin PlantUML

---

## 📊 Diagramas de Classe

### 1. **Camada de Modelo & Algoritmos** `class-diagram-core.puml`
Estrutura das classes de domínio, sem dependências GUI.

```
Graph (data structure)
├── GraphAlgorithms (7 algorithms)
├── SocialNetwork (business logic)
└── I/O & Generator (file handling)
```

**Complexidade:** O(V + E)
**Tamanho:** ~600 linhas de código
**Responsabilidade:** Análise independente de GUI

---

### 2. **Camada GUI & Componentes** `class-diagram-gui.puml`
Todos os 12 componentes Swing customizados + handlers.

```
GraphGUI (orchestrator)
├── GraphLoadingHandler (file I/O, generation)
├── AlgorithmHandler (algorithm execution)
├── SocialNetworkHandler (recommendations)
│
├── GraphPanel (canvas rendering)
├── GraphUIState (centralized state)
│
└── Visual Components
    ├── Theme (design tokens)
    ├── PrimaryButton (custom button)
    ├── MenuActionButton (icon + label button)
    ├── CollapsiblePanel (expandable)
    ├── ProgressDialog (long operations)
    └── VectorIcon (scalable icons)
```

**Padrões:** MVC, Facade, Observer, Dependency Injection

---

## 🔄 Diagramas de Sequência (Flows)

### 3. **Inicialização da Aplicação** `sequence-startup.puml`
Escolha entre GUI (recomendado) e CLI (legado) na startup.

```
JVM.main()
  ├─ Menu.chooseConsoleMode()
  ├─ User selects [1] GUI → GraphGUI.init()
  └─ User selects [2] CLI → Main.showMenu()
```

**Tempo:** < 1 segundo

---

### 4. **Geração de Rede Social** `sequence-gui-generate-network.puml`
Construção completa da rede de 5.000 pessoas + 32.500 arestas.

```
User clicks "Gerar Rede Social"
  ├─ ProgressDialog.show()
  ├─ GraphGenerator.generateSocialNetwork()
  │   ├─ NamesLoader.load() → 5000 names
  │   └─ Graph.addEdge() × 32500
  ├─ ProgressDialog.updateProgress()
  ├─ GraphUIState.setGraph()
  └─ GraphPanel.setGraph() → render
```

**Tempo:** 5-10 segundos (com progresso)

---

### 5. **Execução de Algoritmo Pesado** `sequence-gui-algorithm.puml`
Cálculo de centralidade (O(V³) — operação longa).

```
User clicks "Centralidade de Proximidade"
  ├─ AlgorithmHandler.checkGraphSize() [V > 500?]
  ├─ ProgressDialog.show()
  ├─ GraphAlgorithms.closenessCentrality()
  │   ├─ Dijkstra() × V vertices
  │   └─ updateProgress() × V iterations
  ├─ Sort by centrality
  └─ Display top 10 nodes
```

**Tempo:** 1-5 minutos (grafo 5000 vértices)
**Complexidade:** O(V × (V + E) log V)

---

### 6. **Sistema de Recomendação** `sequence-gui-recommendation.puml`
DFS para encontrar caminho entre dois usuários.

```
User enters source + target names
  ├─ SocialNetwork.recommendFollower()
  │   ├─ Graph.findByName() × 2
  │   ├─ Check if already follows
  │   └─ DFS to find path (if not)
  ├─ Display recommendation:
  │   ├─ "Já segue" → direct follow
  │   ├─ Path → connection found
  │   └─ "Sem recomendação" → no path
  └─ Highlight path on graph
```

**Tempo:** < 1 segundo
**Complexidade:** O(V + E)

---

## 🏗️ Diagramas de Arquitetura

### 7. **Componentes da Aplicação** `component-diagram.puml`
Visão de alto nível dos subsistemas.

```
┌─ Java Standard Library (java.util, javax.swing)
│
├─ Model Layer
│  └─ Graph (no dependencies)
│
├─ Business Logic Layer
│  ├─ GraphAlgorithms → uses Graph
│  ├─ SocialNetwork → uses Graph
│  ├─ GraphGenerator → creates Graph
│  └─ I/O (Pajek, NamesLoader) → uses Graph
│
├─ GUI Layer
│  ├─ Handlers (Loading, Algorithm, SocialNetwork)
│  ├─ Components (Panel, Theme, Buttons, etc)
│  └─ GraphGUI (orchestrates all)
│
└─ Application Entry
   ├─ Main (GUI + legacy CLI)
   └─ Menu (startup selector)
```

---

### 8. **Packages e Dependências** `package-diagram.puml`
Mapa de dependências entre todos os pacotes.

```
graph.gui ──┐
            ├─→ graph.algorithm ──┐
            ├─→ graph.domain      ├─→ graph.model
            ├─→ graph.generator ──┤
            └─→ graph.io ─────────┘

graph.Main ──→ graph.gui
graph.Menu ──→ (no dependencies)
```

**Propriedade:** Acíclico (DAG)

---

### 9. **Deployment** `deployment-diagram.puml`
Como a aplicação é compilada e executada.

```
Source Code + Config Files
    ↓ (javac)
Compiled Classes (.class files)
    ↓
JVM Runtime Environment (JDK 8+)
    ├─ Class Loader
    ├─ Memory Management
    ├─ Garbage Collector
    └─ Application Process
          ├─ Model + Algorithm
          ├─ GUI Components
          └─ I/O Handlers
             ↓
          System Resources
          ├─ data/names.txt
          ├─ pajek/ (import/export)
          ├─ Display (Swing)
          └─ Console (legacy CLI)
```

**Requisito:** JDK 8+

---

## 🔀 Diagramas de Estado

### 10. **State Machine da Aplicação** `state-diagram.puml`
Máquina de estados para GUI e CLI.

```
┌─ GUI Mode ─────────────────┐
│  Startup                    │
│    ├─ No Graph Loaded       │
│    │   ├─ Generate Network  │
│    │   ├─ Generate Random   │
│    │   └─ Import Pajek      │
│    │        ↓               │
│    │  Graph Loaded          │
│    │   ├─ Run Algorithm     │
│    │   ├─ Recommend         │
│    │   ├─ Export Pajek      │
│    │   └─ View Graph        │
│    │        ↓               │
│    └─ Display Results       │
│
└─ CLI Mode ─────────────────┐
   Menu-driven Console
   (Legacy Interface)
   Same functionality, different UI
```

---

## 📦 Estruturas de Dados

### 11. **Graph Data Structure** `data-structures.puml`
Representação interna do grafo em memória.

```
Graph
├─ names[]: String[]  [5000 entries]
│   [0] = "Alice"
│   [1] = "Bob"
│   [2] = "Carol"
│   ...
│
└─ adj[]: List<int[]>[]  [5000 lists]
   [0] = [{1, 1}, {2, 1}, ...]  // Alice → Bob, Carol
   [1] = [{2, 1}, ...]           // Bob → Carol
   [2] = []                      // Carol → nobody
   ...

Memory: O(V + E) ≈ 37,500 integers = ~150 KB
```

**Renderização:**
- Vértices: círculos (cor = grau/centralidade)
- Arestas: setas (directed)
- Layout: força-direcionada (para 5000 nós)

---

## 📋 Resumo de Artefatos

| Artefato | Tipo | Descrição | Linhas |
|----------|------|-----------|--------|
| class-diagram-core.puml | Class | Model + algorithms | ~100 |
| class-diagram-gui.puml | Class | GUI + components | ~200 |
| sequence-startup.puml | Sequence | Inicialização | ~30 |
| sequence-gui-generate-network.puml | Sequence | Geração rede | ~30 |
| sequence-gui-algorithm.puml | Sequence | Execução algoritmo | ~25 |
| sequence-gui-recommendation.puml | Sequence | Recomendação | ~35 |
| component-diagram.puml | Component | Subsistemas | ~80 |
| deployment-diagram.puml | Deployment | Ambiente execução | ~60 |
| package-diagram.puml | Package | Dependências | ~90 |
| state-diagram.puml | State | Máquina de estados | ~70 |
| data-structures.puml | Class | Estruturas internas | ~60 |
| **README.md** | Documentação | Guia dos diagramas | ~300 |

**Total:** 11 diagramas + documentação

---

## 🔗 Como Usar

### 1. Visualizar Online
```
Copie o conteúdo de um arquivo .puml
→ https://www.plantuml.com/plantuml/uml/
→ Veja a renderização em tempo real
```

### 2. Editor Local (VS Code)
```bash
# Instale PlantUML Extension
ext install jebbs.plantuml

# Abra um arquivo .puml
# Pressione Alt+D para preview
```

### 3. Renderizar em PNG/SVG
```bash
# Com Docker
docker run --rm -v $(pwd):/workspace \
  plantuml/plantuml -v docs/diagrams/*.puml -o png

# Com Java local
java -jar plantuml.jar docs/diagrams/*.puml
```

---

## 📚 Referências Cruzadas

Todos os diagramas estão documentados em:
- **docs/design.md** — Explicações de cada diagrama
- **README.md** — Overview do projeto
- **Javadoc** — Código fonte (100% em português)

---

## ✍️ Autores

- Jafte Carneiro Fagundes da Silva (@cyberfika)
- Nicolas Hrescak (@NicolasHrescak)

**Professor:** Fabrício Enembreck — PUCPR
**Disciplina:** Trabalho Colaborativo II — Ciência da Computação

---

**Última atualização:** 2026-06-16
**Status:** Completo (11 diagramas)
