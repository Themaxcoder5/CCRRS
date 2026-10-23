# Cyber Crime Report and Resolution System (CCRRS)

A comprehensive, production-quality Data Structures and Algorithms (DSA) capstone project implemented in Core Java with MySQL database support.

---

## Project Highlights

- **Pure Custom Data Structures**: All core operations use handwritten data structures (Linked List, Stack, Queue, Binary Tree, Binary Search Tree, and Graph Adjacency List) rather than library wrappers.
- **Real-World Investigative Domain**: Solves complaint registration, prioritization, hierarchical categorization, suspect syndicate tracking, and resource allocation under constraints.
- **Multi-Paradigm Algorithmic Decision Support**:
  - **Greedy**: Job Sequencing with Deadlines for officer assignment.
  - **Dynamic Programming**: 0/1 Knapsack for forensic budget planning.
  - **Backtracking**: Constrained combination search with early pruning for suspect shortlisting.
- **Preloaded Realistic Scenarios**: UPI/QR code phishing, hospital ransomware, SIM swap netbanking drain, deepfake extortion, and crypto syndicate networks.
- **Dual Persistence Mode**: Connects to MySQL 8.0 via JDBC, with auto-fallback to high-speed in-memory mode when running offline.

---

## Data Structures & Algorithms Mapping

| Module | Purpose | Data Structure / Algorithm | Time Complexity | Space Complexity |
|---|---|---|---|---|
| **Module 1** | Complaint Ledger & Registration | **Singly Linked List** (tail pointer) | Insert: $O(1)$<br>Delete: $O(n)$<br>Traverse: $O(n)$ | $O(n)$ |
| **Module 2** | Investigation Triage Queue | **Linked Queue (FIFO)** | Enqueue: $O(1)$<br>Dequeue: $O(1)$ | $O(n)$ |
| **Module 2** | Status Change Undo History | **Linked Stack (LIFO)** | Push: $O(1)$<br>Pop (Undo): $O(1)$ | $O(k)$ per case |
| **Module 2** | Case Priority Calculation | **Stack Postfix Evaluator** | Evaluation: $O(n)$<br>Infix-to-Postfix: $O(n)$ | $O(n)$ |
| **Module 3** | Crime Category Hierarchy | **Binary Tree** (Pre/In/Postorder) | Traversal: $O(n)$<br>Search: $O(n)$ | $O(h)$ |
| **Module 3** | Fast Case ID Retrieval | **Binary Search Tree (BST)** | Search: $O(\log n)$ avg<br>Insert: $O(\log n)$ avg | $O(n)$ |
| **Module 4** | Suspect Syndicate Detection | **Graph Adjacency List + BFS** | $O(V + E)$ | $O(V)$ |
| **Module 4** | Chain-of-Evidence Verification | **Graph Adjacency List + DFS** | $O(V + E)$ | $O(V)$ |
| **Module 5** | Case Prioritization | **Quick Sort** (in-place) | Avg: $O(n \log n)$<br>Worst: $O(n^2)$ | $O(\log n)$ |
| **Module 5** | Free-Text Keyword Search | **Linear Search** | $O(n)$ | $O(1)$ |
| **Module 5** | Ordered Case Retrieval | **Binary Search** | $O(\log n)$ | $O(1)$ |
| **Module 6** | Officer Schedule Optimization | **Greedy Job Sequencing** | $O(n \cdot \min(d, n))$ | $O(d)$ |
| **Module 6** | Forensic Budget Optimization | **Dynamic Programming (0/1 Knapsack)** | $O(n \cdot W)$ | $O(n \cdot W)$ |
| **Module 6** | Multi-Constraint Suspect Matching | **Backtracking with Early Pruning** | $O(b^d)$ pruned | $O(d)$ stack |

---

## Directory Structure

```
CCRRS_Project/
├── bin/                                       // Compiled bytecode (.class)
├── sql/
│   └── schema.sql                             // MySQL 8.0 DDL & seed data script
├── src/
│   └── ccrrs/
│       ├── Main.java                          // Interactive CLI application menu
│       ├── model/
│       │   ├── Complaint.java                 // Core complaint domain model
│       │   ├── Officer.java                   // Police officer roster model
│       │   ├── EvidenceEntity.java           // Graph vertex (Suspect, IP, IMEI, Account)
│       │   └── StatusHistoryEntry.java       // Undo stack state snapshot
│       ├── ds/linear/
│       │   ├── SinglyLinkedList.java          // Custom linked list
│       │   ├── LinkedStack.java               // Custom LIFO stack
│       │   ├── LinkedQueue.java               // Custom FIFO queue
│       │   └── PostfixPriorityEvaluator.java  // Postfix formula evaluator
│       ├── ds/tree/
│       │   ├── CategoryNode.java              // Category tree node
│       │   ├── CategoryBinaryTree.java        // Preorder, Inorder, Postorder traversals
│       │   ├── BSTNode.java                   // Binary Search Tree node
│       │   └── ComplaintBST.java              // O(log n) BST search
│       ├── ds/graph/
│       │   ├── GraphEdge.java                 // Graph edge with complaint metadata
│       │   └── SuspectNetworkGraph.java       // Adjacency list with BFS and DFS
│       ├── algorithms/searchsort/
│       │   ├── CaseQuickSort.java             // QuickSort by priority score
│       │   ├── CaseLinearSearch.java          // Substring & keyword search
│       │   └── CaseBinarySearch.java          // O(log n) array binary search
│       ├── algorithms/decision/
│       │   ├── GreedyOfficerAllocation.java   // Job sequencing with deadlines
│       │   ├── KnapsackBudgetOptimizer.java   // 0/1 Knapsack DP
│       │   └── BacktrackingSuspectSearch.java // Constrained combination search
│       ├── database/
│       │   ├── DBConnection.java              // JDBC manager with in-memory fallback
│       │   └── ComplaintDAO.java              // MySQL queries
│       └── demo/
│           ├── SampleDataGenerator.java       // Realistic test datasets
│           └── CCRRSVerifier.java             // Automated test suite (43 unit tests)
├── run.bat                                    // Windows batch launcher script
└── README.md                                  // Project documentation
```

---

## How to Run

### Method 1: Using `run.bat` (Recommended on Windows)
Simply double-click `run.bat` in the project root, or execute from PowerShell/CMD:
```bat
cd C:\Users\tanua\.gemini\antigravity\scratch\CCRRS_Project
run.bat
```
The script will automatically detect Adoptium JDK 17, recompile if needed, and prompt you to run the **Interactive Dashboard** or the **Automated Test Suite**.

### Method 2: Manual Terminal Commands
```powershell
cd C:\Users\tanua\.gemini\antigravity\scratch\CCRRS_Project

# 1. Compile
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin\javac.exe" -d bin (Get-ChildItem -Recurse src\*.java | % { $_.FullName })

# 2. Run Main Interactive Application
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin\java.exe" -cp bin ccrrs.Main

# 3. Run Automated Verification Test Suite (43 Tests)
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin\java.exe" -cp bin ccrrs.demo.CCRRSVerifier
```

---

## Setting Up MySQL (Optional)

If you wish to use the MySQL backend:
1. Open MySQL Command Line Client or MySQL Workbench:
   ```sql
   source C:/Users/tanua/.gemini/antigravity/scratch/CCRRS_Project/sql/schema.sql;
   ```
2. Update the credentials in `src/ccrrs/database/DBConnection.java` (`DEFAULT_USER` and `DEFAULT_PASSWORD`).
3. If MySQL is not configured, the application **automatically runs in pure in-memory mode** without errors.

---

## Viva Voce & Interview Guide

### Q1: Why use a Singly Linked List for the complaint ledger instead of an Array?
**Answer**: Complaint intake is continuous and unpredictable. A Singly Linked List provides $O(1)$ dynamic insertion at the tail without requiring expensive array resizing or memory pre-allocation.

### Q2: How does the Stack handle the "Undo" functionality?
**Answer**: Every time an officer alters a complaint's status (e.g. from `UNDER_INVESTIGATION` to `EVIDENCE_COLLECTED`), the previous state is pushed onto a `LinkedStack<StatusHistoryEntry>`. When the officer presses "Undo", the stack `pop()`s the topmost record and reverts the complaint's status to the previous state in $O(1)$ time.

### Q3: Why is Postfix notation evaluated using a Stack?
**Answer**: In postfix notation (Reverse Polish Notation), operators appear after their operands, eliminating the need for parentheses and operator precedence rules during evaluation. Operands are pushed onto the stack, and when an operator is encountered, the top two operands are popped, evaluated, and the result is pushed back.

### Q4: What is the difference between BFS and DFS in the Suspect Graph?
- **BFS (Breadth-First Search)**: Explores concentric hops around a suspect (1-hop, 2-hop radius) using a FIFO queue. It is ideal for identifying fraud rings, co-conspirators, and shared mule accounts across complaints.
- **DFS (Depth-First Search)**: Follows an evidence trail as deeply as possible before backtracking. It is used to verify if a continuous path exists connecting two separate complaints or suspects through shared devices/IPs.

### Q5: How do the Greedy, DP, and Backtracking algorithms differ in Module 6?
- **Greedy (Job Sequencing)**: Makes a locally optimal choice at each step (highest priority first) and assigns it to the latest valid time slot $\le \text{deadline}$.
- **Dynamic Programming (0/1 Knapsack)**: Solves overlapping subproblems to find the exact globally optimal subset of cases that maximizes resolution payoff within a fixed budget.
- **Backtracking**: Explores combinatorial suspect configurations while testing constraints at each decision node. If a suspect violates an evidence constraint (e.g. mismatched IP subnet), the branch is **pruned early**, avoiding exponential brute-force search.
