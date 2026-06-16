# UML Diagrams — GraphNet Analyzer

Este diretório contém todos os diagramas UML da aplicação em formato PlantUML (.puml).

## Estrutura dos Diagramas

### Diagramas de Classe

#### 1. **class-diagram-core.puml**
Diagrama da camada de domínio e modelo de dados.

**Inclui:**
- `graph.model.Graph` — Estrutura de dados
- `graph.algorithm.GraphAlgorithms` — Algoritmos
- `graph.domain.SocialNetwork` — Lógica de negócio
- `graph.io.*` — I/O (Pajek, NamesLoader)
- `graph.generator.GraphGenerator` — Geração de grafos

**Relações:**
- Algoritmos dependem de Graph
- Social Network depende de Graph
- Generator cria Graph

---

#### 2. **class-diagram-gui.puml**
Diagrama completo da camada GUI com todos os componentes.

**Inclui:**
- `GraphGUI` — Frame principal (orquestrador)
- `GraphUIState` — Gerenciamento centralizado de estado
- **Handlers (Controllers):**
  - `GraphLoadingHandler` — Carregamento/geração
  - `AlgorithmHandler` — Execução de algoritmos
  - `SocialNetworkHandler` — Operações de rede
- **Componentes Visuais:**
  - `GraphPanel` — Canvas de renderização
  - `Theme` — Design tokens
  - `PrimaryButton`, `MenuActionButton` — Botões customizados
  - `CollapsiblePanel` — Painel colapsável
  - `ProgressDialog` — Diálogo de progresso
  - `VectorIcon` — Ícones vetoriais

**Relações:**
- GraphGUI orquestra todos os handlers
- Handlers atualizam GraphUIState
- Componentes usam Theme

---

### Diagramas de Sequência

#### 3. **sequence-startup.puml**
Fluxo de inicialização da aplicação.

**Fluxo:**
1. JVM inicia Main
2. Main chama Menu.chooseConsoleMode()
3. Usuário escolhe entre GUI (opção 1) ou CLI (opção 2)
4. Se GUI → initializa GraphGUI
5. Se CLI → mostra menu console legado

**Duração:** Carga inicial até interface pronta

---

#### 4. **sequence-gui-generate-network.puml**
Geração de rede social completa (5.000 pessoas).

**Fluxo:**
1. Usuário clica "Gerar Rede Social"
2. Handler mostra diálogo de progresso
3. GraphGenerator.generateSocialNetwork() é chamado
4. NamesLoader carrega 5.000 nomes
5. Graph.addEdge() é chamado ~32.500 vezes
6. Progressão é atualizada
7. GraphUIState recebe o novo grafo
8. GraphPanel renderiza

**Duração:** ~5-10 segundos (mostra progresso)

---

#### 5. **sequence-gui-algorithm.puml**
Execução de algoritmo de centralidade (operação pesada).

**Fluxo:**
1. Usuário clica "Centralidade de Proximidade"
2. Handler checa se grafo está carregado
3. Diálogo de progresso é mostrado
4. GraphAlgorithms.closenessCentrality() é chamado
5. Dijkstra é executado para cada vértice (V × (V + E) log V)
6. Resultado é passado para GUI
7. Top 10 nós são exibidos

**Duração:** ~1-5 minutos para 5.000 nós (operação O(V³))

---

#### 6. **sequence-gui-recommendation.puml**
Sistema de recomendação de seguidores.

**Fluxo:**
1. Usuário insere nomes (source, target)
2. Clica "Recomendar"
3. SocialNetwork.recommendFollower() é chamado
4. Grafo é buscado pelo nome (ambos: source e target)
5. Verificação: já segue?
6. Se não: DFS busca caminho entre vértices
7. Resultado é exibido (recomendação + caminho)

**Duração:** < 1 segundo

---

### Diagramas de Arquitetura

#### 7. **component-diagram.puml**
Visão de componentes da aplicação inteira.

**Organização:**
- **Camada de Modelo:** Graph (sem dependências)
- **Camada de Algoritmos:** GraphAlgorithms, SocialNetwork, Generator, I/O
- **Camada de GUI:** Handlers, Componentes, Theme
- **Camada de Aplicação:** Main, Menu

**Relações:**
- Camada superior depende de camada inferior
- Sem dependências circulares

---

#### 8. **package-diagram.puml**
Diagrama de pacotes mostrando dependências entre módulos.

**Packages:**
- `java.util` — Collections (ArrayList, Queue, Stack, etc)
- `javax.swing` — GUI framework
- `graph.model` — Estrutura de dados
- `graph.algorithm` — Algoritmos
- `graph.domain` — Lógica de negócio
- `graph.io` — I/O de arquivos
- `graph.generator` — Construção de grafos
- `graph.gui` — Interface gráfica
- `graph` — Aplicação principal

**Dependências:**
Todas as dependências fluem de cima para baixo (nenhuma circulação)

---

### Diagramas de Estado

#### 9. **state-diagram.puml**
Máquina de estados da aplicação.

**Estados GUI:**
- **StartupMenu** → Seleção inicial
- **NoGraphLoaded** → Nenhum grafo carregado
- **LoadingNetwork/Random/Import** → Carregando
- **GraphLoaded** → Grafo disponível
- **Analyzing...** → Executando algoritmo
- **DisplayingResults** → Mostrando resultados

**Estados CLI:**
- Menu interativo legado
- Mesmo conjunto de operações, interface diferente

---

#### 10. **data-structures.puml**
Estrutura de dados interna do Graph.

**Representação:**
- `names[]` — Nomes dos vértices (indexados)
- `adj[][]` — Lista de adjacências
  - `adj[u]` = lista de `{target, weight}`
  - Sparse representation (O(V + E) espaço)

**Exemplo:**
```
names = ["Alice", "Bob", "Carol"]
adj[0] = [{1, 1}, {2, 1}]  // Alice → Bob, Carol
adj[1] = [{2, 1}]           // Bob → Carol
adj[2] = []                 // Carol → ninguém
```

**Renderização:**
- Vértices são círculos
- Arestas são setas direcionadas
- Cores refletem propriedades (grau, centralidade)

---

## Como Visualizar os Diagramas

### Opção 1: Online (PlantUML Editor)
1. Acesse https://www.plantuml.com/plantuml/uml/
2. Cole o conteúdo do arquivo .puml
3. Visualize em tempo real

### Opção 2: Editor Local
**VS Code:**
```bash
# Instale a extensão PlantUML
ext install jebbs.plantuml
```

**IntelliJ/PyCharm:**
```bash
# Plugin nativo disponível
Settings → Plugins → PlantUML
```

### Opção 3: Renderizar em PNG/SVG
```bash
# Requer Java + PlantUML JAR
java -jar plantuml.jar docs/diagrams/*.puml

# Ou com Docker
docker run -v $(pwd):/data plantuml/plantuml -v docs/diagrams/
```

---

## Referências Cruzadas

| Arquivo | Referenciado em |
|---------|---|
| class-diagram-core.puml | docs/design.md — "Class Design" |
| class-diagram-gui.puml | docs/design.md — "GUI Architecture" |
| sequence-startup.puml | docs/design.md — "Application Startup" |
| sequence-gui-generate-network.puml | docs/design.md — "Data Flow" |
| sequence-gui-algorithm.puml | docs/design.md — "Algorithm Execution" |
| sequence-gui-recommendation.puml | docs/design.md — "Social Network Recommendation" |
| component-diagram.puml | docs/design.md — "Component Architecture" |
| package-diagram.puml | docs/design.md — "Dependency Graph" |
| state-diagram.puml | docs/design.md — "Design Patterns" |
| data-structures.puml | docs/design.md — "Performance Considerations" |

---

## Legendas Comuns

### Cores em Diagramas de Classe
- **Branco** — Classe concreta
- **Azul** — Interface
- **Cinza** — Abstract class
- **Verde** — Pacote externo

### Relações
- **→** (seta sólida) — Associação / Dependência
- **⇒** (seta dupla) — Herança / Implementação
- **◆** (losango preenchido) — Composição
- **◇** (losango vazio) — Agregação

### Multiplicidade
- `1` — Um
- `*` — Zero ou mais
- `1..*` — Um ou mais
- `0..1` — Zero ou um

---

## Documentação Relacionada

- **README.md** — Overview do projeto
- **design.md** — Arquitetura e decisões de design
- **CHANGELOG.md** — Histórico de mudanças
- **Javadoc** — Documentação inline (100% cobertura em português)

---

## Autores

- Jafte Carneiro Fagundes da Silva (@cyberfika)
- Nicolas Hrescak (@NicolasHrescak)

Professor: Fabrício Enembreck — PUCPR
