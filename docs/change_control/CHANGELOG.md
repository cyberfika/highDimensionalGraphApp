# Changelog

All notable changes to this project are documented here.
Format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased] — 2026-06-16 (Session 5 — Defense Documentation & Source Code Export)

### Documentation

#### 📄 Comprehensive Technical Documentation (LaTeX)

Generated complete technical documentation for **academic defense of authorship** in rigorous LaTeX format.

**File:** `docs/documentacao.tex` (Overleaf-compatible)

**Contents:**
- **15 Sections** covering complete system architecture and implementation
  - Arquitecture Overview with module dependencies
  - Data Structure Layer (Graph.java) with 6 public methods analyzed
  - Algorithm Layer (GraphAlgorithms.java) with 7 algorithms + pseudocode:
    * Connectivity (BFS) — O(V+E)
    * Components (BFS) — O(V+E)
    * Eulerian (degree analysis) — O(V+E)
    * Cycle Detection (DFS coloration) — O(V+E)
    * Closeness Centrality (Dijkstra) — O(V(V+E)logV)
    * Betweenness Centrality (Brandes) — O(V(V+E)logV)
    * Dijkstra (shortest paths) — O((V+E)logV)
  - Domain Layer (SocialNetwork.java) with DFS path tracing algorithm
  - I/O & Utilities (GraphGenerator, NamesLoader, PajekIO)
  - GUI Presentation Layer with 11 classes analyzed
  - Main Data Flows (5 complete flows with diagrams)
  - Design Patterns (MVC, Facade, Factory, Strategy, Observer, Dependency Injection)
  - Dependency Analysis with 14-class matrix
  - Complexity Analysis for all algorithms with real-world estimates (5000 vertices)
  - SOLID Principles applied with code examples
  - Refactorizations documented
  - Appendices with code examples and compilation instructions

- **Format:** 12pt, A4, 2.5cm margins, 100% Overleaf-compatible
- **Language:** Portuguese (academic rigor)
- **Authors:** Jafte Carneiro Fagundes da Silva, Nicolas Hrescak
- **Scope:** 15 sections, ~50 pages when compiled
- **Purpose:** Complete authorship defense with "quem chama quem" (who calls whom) dependency analysis

#### 📑 Source Code PDF Export

Generated printable PDF containing all 20 Java source files in logical order.

**File:** `codigo-fonte.pdf` (ready for physical submission)

**Contents:**
- **Organization:** 8 logical sections mirroring project architecture
  1. Core Model (Graph.java)
  2. Algorithms & Generation (GraphAlgorithms.java, GraphGenerator.java)
  3. Input/Output (PajekIO.java, NamesLoader.java)
  4. GUI Utilities (VectorIcon.java, Theme.java, GraphUIState.java)
  5. GUI Components (PrimaryButton, MenuActionButton, CollapsiblePanel, ProgressDialog)
  6. GUI Handlers (GraphLoadingHandler, SocialNetworkHandler, AlgorithmHandler)
  7. GUI Main (GraphPanel.java, GraphGUI.java)
  8. Application (Menu.java, Main.java)

- **Format:** A4, 12pt monospace font with syntax highlighting
- **Total:** 20 files, 200+ pages, dependency-ordered sequence
- **Generation:** Automated Node.js script using pdfkit library
- **Use Case:** Physical submission for academic defense

#### 📊 Project Structure Diagram

Added `docs/diagrams/` directory for architectural diagrams.

### Tools & Build Scripts

#### PDF Generation Script

Created `generate-pdf.js` — Node.js script for automated PDF generation with pdfkit.

**Features:**
- Reads 20 Java files in logical dependency order
- Generates formatted PDF with:
  - Syntax highlighting
  - Line numbers
  - Section headers matching architecture
  - Automatic page breaks
  - Monospace font (Courier, 9pt)
- Output: `codigo-fonte.pdf` (196+ KB)

**Execution:**
```bash
npm install pdfkit
node generate-pdf.js
```

### Package Dependencies

Added `pdfkit` to `package.json` for PDF generation.

```json
{
  "dependencies": {
    "pdfkit": "^0.13.0"
  }
}
```

---

## [Unreleased] — 2026-06-16 (Session 4 — Complete Documentation & PT-BR Translation)

### Documentation

#### Comprehensive Javadoc in Portuguese — All Java files fully documented

Every public and private method now has complete Javadoc in Portuguese with:
- Purpose and responsibility
- Parameters documented with @param
- Return values documented with @return
- Algorithm complexity notes (for O(V×(V+E) log V) operations)
- Implementation details and edge cases

| File | Methods | Status |
|---|---|---|
| `Graph.java` | 9 | ✅ All documented (constructor, setters, getters, addEdge, findByName, neighbors) |
| `GraphAlgorithms.java` | 14 | ✅ All documented (7 public + 7 private helpers with algorithm explanations) |
| `SocialNetwork.java` | 2 | ✅ All documented (recommendFollower + dfsPath) |
| `NamesLoader.java` | 1 | ✅ Documented (load with file path attempts) |
| `PajekIO.java` | 2 | ✅ Both documented (export + importFrom with format details) |
| `GraphGenerator.java` | 3 | ✅ All documented (generateRandomGraph, generateSocialNetwork, edgeKey) |
| `Main.java` | 15 | ✅ All documented (main + 14 action/menu methods) |

#### README.md translated to Portuguese-Brazil

- Complete translation maintaining structure and technical accuracy
- Updated requirements table with refactored method names
- Project structure reflects new sub-package organization (model/, algorithm/, domain/, io/, generator/)
- Added "Arquitetura e Design" section explaining SRP and low coupling principles
- Compilation/execution instructions in Portuguese
- All user-facing text translated while preserving code blocks and badges

---

## [Unreleased] — 2026-06-16 (Session 3 — SRP & Coupling Refactor)

### Violations Fixed

#### Violation 1 — `Graph` accumulated four unrelated responsibilities (SRP)

**Principle:** Single Responsibility Principle
**Severity:** Major
**File:** `src/graph/Graph.java` (now `src/graph/model/Graph.java`)

`Graph` was a God Class combining data structure, graph algorithms, domain logic, and
presentation in a single 340-line file. Any change to one concern forced modification
of the whole class, making testing and evolution unnecessarily risky.

| Responsibility | Extracted to |
|---|---|
| Graph algorithms (isConnected, isEulerian, hasCycle, etc.) | `graph.algorithm.GraphAlgorithms` |
| Social network domain logic (recommendFollower) | `graph.domain.SocialNetwork` |
| Console presentation (print method) | `graph.Main.printGraph()` |
| Data structure (addEdge, neighbors, names) | `graph.model.Graph` (retained) |

---

#### Violation 2 — `GraphGenerator` performed file I/O (SRP)

**Principle:** Single Responsibility Principle
**Severity:** Major
**File:** `src/graph/GraphGenerator.java` (now `src/graph/generator/GraphGenerator.java`)

`GraphGenerator` mixed graph construction logic with file reading (`loadNames`).
A generator should not know how to read files.

**Fix:** Extracted `loadNames` into a dedicated `graph.io.NamesLoader` class.
`GraphGenerator` now receives a list of names already loaded and focuses exclusively
on building `Graph` instances.

---

#### Violation 3 — `GraphGenerator` directly mutated internal state of `Graph` (Coupling)

**Principle:** Low Coupling / Encapsulation
**Severity:** Major
**File:** `src/graph/GraphGenerator.java`, line 118

```java
// Before — direct access to package-private field:
g.adj[i].add(new int[]{target, 1});

// After — through the public API:
g.addEdge(i, target, 1);
```

`adj` was package-private, allowing `GraphGenerator` to bypass `addEdge` and write
directly to the adjacency list. This created tight coupling between unrelated classes
and made `Graph`'s internal representation impossible to change without breaking
the generator.

**Fix:** Made `adj` private. All mutations now go through `addEdge`. Added a
read-only accessor `neighbors(int u)` for classes that need to iterate the list.

---

#### Violation 4 — `PajekIO` directly mutated internal state of `Graph` (Coupling)

**Principle:** Low Coupling / Encapsulation
**Severity:** Major
**File:** `src/graph/PajekIO.java`

```java
// Before:
g.adj[u].add(new int[]{v, weight});
if (!directed) g.adj[v].add(new int[]{u, weight});

// After:
g.addEdge(u, v, weight);
```

Same violation as GraphGenerator. `PajekIO` reached into `Graph`'s internals
instead of using the public API.

**Fix:** Replaced direct field access with `g.addEdge(u, v, weight)`.

---

#### Violation 5 — `Main` accessed public fields instead of methods (Coupling)

**Principle:** Low Coupling / Encapsulation
**Severity:** Minor
**File:** `src/graph/Main.java`

`Main` accessed `graph.numVertices` as a public field in many places. While acceptable
for a simple academic project, it exposed `Graph`'s internal structure to the
presentation layer.

**Partial fix:** `numVertices` remains public (final) for simplicity given the academic
scope, but `adj` and `names` are now fully private and accessed only through
`neighbors(u)` and `getName(i)`.

---

#### Violation 6 — `Graph.print()` coupled data structure to console presentation (SRP)

**Principle:** Single Responsibility Principle
**Severity:** Minor
**File:** `src/graph/Graph.java`

A data structure class should not contain console output logic. `print()` belonged
to the presentation layer.

**Fix:** Removed `print()` from `Graph`. Logic moved to `Main.printGraph(int limit)`.

---

#### Violation 7 — Unused variable `sorted` (Dead Code)

**Principle:** Code Clarity
**Severity:** Minor
**File:** `src/graph/Main.java`, `calcCloseness()`

An array was created, filled, and sorted but its result was never used. Detected by
IntelliJ inspection "Contents of array are written to, but never read".

**Fix:** Removed the dead variable.

---

#### Violation 8 — String literals used for charset encoding (Code Quality)

**Principle:** Type Safety
**Severity:** NIT
**Files:** `Main.java`, `PajekIO.java`, `GraphGenerator.java`

```java
// Before:
new Scanner(System.in, "UTF-8")
new OutputStreamWriter(stream, "UTF-8")

// After:
new Scanner(System.in, StandardCharsets.UTF_8)
new OutputStreamWriter(stream, StandardCharsets.UTF_8)
```

String literals for charset names bypass compile-time type checking.
`StandardCharsets.UTF_8` is type-safe and cannot be misspelled at runtime.

---

### Structural Changes

- Reorganised package structure into sub-packages by responsibility:

```
src/graph/
├── model/       Graph.java
├── algorithm/   GraphAlgorithms.java
├── domain/      SocialNetwork.java
├── io/          PajekIO.java, NamesLoader.java
├── generator/   GraphGenerator.java
└──              Main.java
```

- `Main` now holds `GraphAlgorithms` and `SocialNetwork` as dependent services,
  instantiated via `setGraph()` whenever a new graph is loaded.
- Added `docs/design.md` with UML class diagram (per AGENTS.md §23).

### IntelliJ Inspection Fixes

| File | Warning | Fix |
|---|---|---|
| `PajekIO.java` | Local `directed` variable assigned but never read after encapsulation | Removed variable; `g.directed` used directly |
| `PajekIO.java` | `File.mkdirs()` result ignored | Wrapped in `if (!dir.mkdirs() && !dir.exists())` guard |
| `PajekIO.java` | `g != null` condition always true in edges block | Removed redundant null check; added explanatory comment |
| `GraphAlgorithms.java` | `start` parameter always `0` in `bfsUndirected` | Removed parameter; hardcoded vertex `0` as start |
| `GraphAlgorithms.java` | Blank line in Javadoc ignored | Removed blank line from class-level Javadoc |

---

## [Unreleased] — 2026-06-16 (Session 2 — English Refactor)

### Changed
- Refactored all source code from Portuguese to English (identifiers, UI strings, constants)
- Renamed package `grafo` → `graph`
- Renamed `Grafo.java` → `Graph.java`
- Renamed `GeradorGrafo.java` → `GraphGenerator.java`
- Renamed `data/nomes_selecionados.txt` → `data/names.txt`
- Renamed Pajek directories: `pajekLeitura/` → `pajek/input/`, `pajekGravacao/` → `pajek/output/`
- Updated `PajekIO` constants: `INPUT_DIR`, `OUTPUT_DIR`
- Updated `Main` to call `names.txt` and use English prompts
- Updated `.gitignore` to reflect new `pajek/output/` path
- Updated `docs/AGENT_HANDOFF.md` to reflect current structure

### Preserved
- All Java comments and Javadocs remain in Portuguese (per project convention in `AGENTS.md`)

---
