package ccrrs;
import ccrrs.algorithms.decision.BacktrackingSuspectSearch;
import ccrrs.algorithms.decision.BacktrackingSuspectSearch.*;
import ccrrs.algorithms.decision.GreedyOfficerAllocation;
import ccrrs.algorithms.decision.KnapsackBudgetOptimizer;
import ccrrs.algorithms.searchsort.CaseBinarySearch;
import ccrrs.algorithms.searchsort.CaseLinearSearch;
import ccrrs.algorithms.searchsort.CaseQuickSort;
import ccrrs.database.ComplaintDAO;
import ccrrs.database.DBConnection;
import ccrrs.demo.CCRRSVerifier;
import ccrrs.demo.SampleDataGenerator;
import ccrrs.ds.graph.SuspectNetworkGraph;
import ccrrs.ds.linear.LinkedQueue;
import ccrrs.ds.linear.PostfixPriorityEvaluator;
import ccrrs.ds.linear.SinglyLinkedList;
import ccrrs.ds.tree.CategoryBinaryTree;
import ccrrs.ds.tree.CategoryNode;
import ccrrs.ds.tree.ComplaintBST;
import ccrrs.model.Complaint;
import ccrrs.model.EvidenceEntity;
import ccrrs.model.Officer;
import ccrrs.model.StatusHistoryEntry;
import ccrrs.web.CCRRSHttpServer;
import java.awt.Desktop;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Cyber Crime Report and Resolution System (CCRRS)
 * Main Application Entry Point with Web Server & Console Interfaces.
 */
public class Main {

    public static final SinglyLinkedList complaintList = new SinglyLinkedList();
    public static final ComplaintBST complaintBST = new ComplaintBST();
    public static final LinkedQueue<Complaint> investigationQueue = new LinkedQueue<>();
    public static final CategoryBinaryTree categoryTree = new CategoryBinaryTree();
    public static SuspectNetworkGraph suspectGraph = new SuspectNetworkGraph();
    public static List<Officer> officerRoster = new ArrayList<>();
    public static List<SuspectCandidate> suspectPool = new ArrayList<>();

    private static CCRRSHttpServer httpServer;
    private static final Scanner scanner = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // Supabase (PostgreSQL via JDBC) connection
    // Configure via environment variables:
    //   SUPABASE_DB_URL   e.g. jdbc:postgresql://aws-0-xxx.pooler.supabase.com:6543/postgres?sslmode=require
    //   SUPABASE_DB_USER  e.g. postgres.xxx
    //   SUPABASE_DB_PASSWORD  your Supabase database password
    // -------------------------------------------------------------------------
    private static final String SUPABASE_DB_URL = System.getenv("SUPABASE_DB_URL");
    private static final String SUPABASE_DB_USER = System.getenv("SUPABASE_DB_USER");
    private static final String SUPABASE_DB_PASSWORD = System.getenv("SUPABASE_DB_PASSWORD");

    public static void main(String[] args) {
        initializeSystem();

        // If --web flag passed or by default on startup, launch the web server
        boolean startWeb = true;
        if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
            startWeb = false;
        }

        if (startWeb) {
            startWebServer(CCRRSHttpServer.DEFAULT_PORT, true);
        }

        boolean exit = false;
        while (!exit) {
            printMainMenu();
            System.out.print("  Select an option [1-11]: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    openBrowser("http://localhost:" + CCRRSHttpServer.DEFAULT_PORT);
                    break;
                case "2":
                    handleRegisterComplaint();
                    break;
                case "3":
                    handleViewAllComplaints();
                    break;
                case "4":
                    handleQueueAndUndo();
                    break;
                case "5":
                    handleCategoryBinaryTree();
                    break;
                case "6":
                    handleCaseSearch();
                    break;
                case "7":
                    handleNetworkGraph();
                    break;
                case "8":
                    handleQuickSortTriage();
                    break;
                case "9":
                    handleAlgorithmicDecisions();
                    break;
                case "10":
                    CCRRSVerifier.main(new String[0]);
                    break;
                case "11":
                    exit = true;
                    if (httpServer != null) httpServer.stop();
                    System.out.println("\n  [+] Thank you for using Cyber Crime Report and Resolution System. Stay Safe Online!");
                    break;
                default:
                    System.out.println("  [!] Invalid selection. Please enter a number from 1 to 11.");
            }
        }
    }

    private static void initializeSystem() {
        System.out.println("==========================================================================================");
        System.out.println("                 CYBER CRIME REPORT AND RESOLUTION SYSTEM (CCRRS)                         ");
        System.out.println("                     Core Data Structures & Algorithms Project                            ");
        System.out.println("==========================================================================================");
        System.out.println("  Initializing custom data structures and loading mock cybercrime records...");

        // Load sample data into data structures
        SampleDataGenerator.populateSystem(complaintList, complaintBST, categoryTree);
        officerRoster = SampleDataGenerator.createSampleOfficers();
        suspectGraph = SampleDataGenerator.createSampleNetworkGraph();
        suspectPool = SampleDataGenerator.createSuspectPoolForBacktracking();

        // Populate initial investigation queue
        for (Complaint c : complaintList.toList()) {
            investigationQueue.enqueue(c);
        }

        // Check database connections (MySQL + Supabase)
        boolean mysqlStatus = DBConnection.isDatabaseAvailable();
        boolean supabaseStatus = isSupabaseAvailable();
        String dbLabel = (mysqlStatus && supabaseStatus) ? "YES (MySQL + Supabase)"
                : mysqlStatus ? "YES (MySQL)"
                : supabaseStatus ? "YES (Supabase)"
                : "NO (In-Memory Fallback)";
        System.out.printf("  Status: Ready. (Records Loaded: %d, DB Connected: %s)%n",
                complaintList.size(), dbLabel);
    }

    // -------------------------------------------------------------------------
    // Supabase (PostgreSQL) JDBC Connection Helpers
    // -------------------------------------------------------------------------

    /**
     * Opens a JDBC connection to Supabase (PostgreSQL).
     * Requires the PostgreSQL JDBC driver on the classpath (postgresql-*.jar).
     * Falls back gracefully (returns null) if env vars are missing or the DB is unreachable,
     * so CCRRS keeps running in in-memory data structure mode.
     */
    public static Connection getSupabaseConnection() {
        if (SUPABASE_DB_URL == null || SUPABASE_DB_USER == null || SUPABASE_DB_PASSWORD == null) {
            return null;
        }
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(SUPABASE_DB_URL, SUPABASE_DB_USER, SUPABASE_DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("  [Supabase Note] PostgreSQL JDBC driver not found in classpath. "
                    + "Add postgresql-*.jar to lib/ to enable Supabase persistence.");
            return null;
        } catch (SQLException e) {
            System.out.println("  [Supabase Note] Supabase not reachable (" + e.getMessage()
                    + "). Running in in-memory mode.");
            return null;
        }
    }

    public static boolean isSupabaseAvailable() {
        try (Connection c = getSupabaseConnection()) {
            return c != null && !c.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Persists a registered complaint to the Supabase complaints table.
     * Safe to call even when Supabase is offline (silently no-ops).
     */
    public static void syncComplaintToSupabase(Complaint c) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSupabaseConnection();
            if (conn == null) {
                return;
            }
            String sql = "INSERT INTO complaints "
                    + "(case_id, complainant_name, phone, email, city, category, description, "
                    + "severity, financial_loss, victim_count, priority_score, status, "
                    + "assigned_officer_id, deadline_days, investigation_cost, expected_resolution_value) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, c.getCaseId());
            ps.setString(2, c.getComplainantName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getCity());
            ps.setString(6, c.getCategory());
            ps.setString(7, c.getDescription());
            ps.setInt(8, c.getSeverity());
            ps.setDouble(9, c.getFinancialLoss());
            ps.setInt(10, c.getVictimCount());
            ps.setDouble(11, c.getPriorityScore());
            ps.setString(12, c.getStatus());
            ps.setString(13, c.getAssignedOfficerId());
            ps.setInt(14, c.getDeadlineDays());
            ps.setInt(15, c.getInvestigationCost());
            ps.setInt(16, c.getExpectedResolutionValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("  [!] Supabase sync failed: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ignored) {
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public static void startWebServer(int port, boolean autoOpen) {
        try {
            httpServer = new CCRRSHttpServer(complaintList, complaintBST, investigationQueue,
                    categoryTree, suspectGraph, officerRoster, suspectPool);
            httpServer.start(port);

            if (autoOpen) {
                openBrowser("http://localhost:" + port);
            }
        } catch (Exception e) {
            System.err.println("  [!] Could not start Web Server on port " + port + ": " + e.getMessage());
        }
    }

    public static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("  [+] Opened browser to: " + url);
            } else {
                System.out.println("  [+] Web UI available at: " + url);
            }
        } catch (Exception e) {
            System.out.println("  [+] Open in your browser: " + url);
        }
    }

    private static void printMainMenu() {
        System.out.println("\n------------------------------------------------------------------------------------------");
        System.out.println("                                  CCRRS DASHBOARD MENU                                    ");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("  [1] Open Web Command Center in Browser (http://localhost:8080)");
        System.out.println("  [2] Register New Cybercrime Complaint (Linked List + Stack Postfix Evaluator)");
        System.out.println("  [3] View All Complaint Records (Singly Linked List Traversal)");
        System.out.println("  [4] Manage Investigation Queue & Undo Status Change (Queue FIFO & Stack LIFO)");
        System.out.println("  [5] Crime Category Classification Tree (Binary Tree: Pre, In, Post Traversals)");
        System.out.println("  [6] Fast Case Lookup (Binary Search Tree O(log n) vs Binary Search vs Linear)");
        System.out.println("  [7] Suspect-Device-IP Network Tracing (Adjacency Graph: BFS & DFS)");
        System.out.println("  [8] Daily Case Priority Triage (Quick Sort by Priority Score)");
        System.out.println("  [9] Algorithmic Decision Support (Greedy Job Sequencing, DP Knapsack, Backtracking)");
        System.out.println("  [10] Run Automated Verification Test Suite (100% Code Coverage Test Runner)");
        System.out.println("  [11] Exit Application");
        System.out.println("------------------------------------------------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // 2. Complaint Registration
    // -------------------------------------------------------------------------
    private static void handleRegisterComplaint() {
        System.out.println("\n=== [Module 1 & 2] Citizen Complaint Intake ===");
        try {
            String caseId = "CCRRS-" + (1000 + complaintList.size() + 1);
            System.out.println("  Generated Case ID: " + caseId);

            System.out.print("  Enter Complainant Name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = "Anonymous Citizen";

            System.out.print("  Enter Contact Phone: ");
            String phone = scanner.nextLine().trim();

            System.out.print("  Enter Email Address: ");
            String email = scanner.nextLine().trim();

            System.out.print("  Enter Incident City: ");
            String city = scanner.nextLine().trim();

            System.out.println("  Select Category: ");
            System.out.println("    1. UPI & Banking Fraud");
            System.out.println("    2. Phishing & Identity Theft");
            System.out.println("    3. Hacking & Ransomware");
            System.out.println("    4. Cyber Harassment & Defamation");
            System.out.print("  Choice [1-4]: ");
            String catChoice = scanner.nextLine().trim();
            String category = switch (catChoice) {
                case "2" -> "Phishing & Identity Theft";
                case "3" -> "Hacking & Ransomware";
                case "4" -> "Cyber Harassment & Defamation";
                default -> "UPI & Banking Fraud";
            };

            System.out.print("  Brief Incident Description: ");
            String desc = scanner.nextLine().trim();

            System.out.print("  Severity Level (1 to 10): ");
            int severity = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("  Estimated Financial Loss (in INR): ");
            double loss = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("  Number of Affected Victims: ");
            int victims = Integer.parseInt(scanner.nextLine().trim());

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Estimate decision parameters
            int deadline = Math.max(2, 8 - (severity / 2));
            int cost = Math.max(5, (int)(loss / 50000) * 5 + 10);
            int resolutionValue = Math.min(100, severity * 8 + victims * 3);

            Complaint c = new Complaint(caseId, name, phone, email, city, category, desc,
                    timestamp, severity, loss, victims, deadline, cost, resolutionValue);

            // Compute priority via Postfix stack evaluator
            double priority = PostfixPriorityEvaluator.computeCasePriority(severity, loss, victims);
            c.setPriorityScore(priority);

            // 1. Insert into Singly Linked List
            complaintList.insertTail(c);

            // 2. Insert into BST
            complaintBST.insert(c);

            // 3. Push into FIFO Investigation Queue
            investigationQueue.enqueue(c);

            // 4. Update Category Binary Tree
            categoryTree.recordComplaint(category, loss);
            categoryTree.aggregateBottomUp(categoryTree.getRoot());

            // 5. Persist to DB if connected (MySQL + Supabase)
            ComplaintDAO.insertComplaint(c);
            syncComplaintToSupabase(c);

            System.out.println("\n  [+] Complaint Registered Successfully!");
            System.out.printf("      Case ID        : %s%n", caseId);
            System.out.printf("      Priority Score : %.1f (Computed via Postfix Stack Evaluator)%n", priority);
            System.out.printf("      Triage Action  : Placed at rear of FIFO Investigation Queue (Position %d)%n", investigationQueue.size());

        } catch (Exception e) {
            System.out.println("  [!] Error processing registration: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 3. View All Complaints (Linked List Traversal)
    // -------------------------------------------------------------------------
    private static void handleViewAllComplaints() {
        System.out.println("\n=== [Module 1] Complaint Ledger (Singly Linked List Traversal) ===");
        complaintList.traverse();
    }

    // -------------------------------------------------------------------------
    // 4. Queue & Undo Stack Management
    // -------------------------------------------------------------------------
    private static void handleQueueAndUndo() {
        System.out.println("\n=== [Module 2] Investigation Queue & Undo Stack Management ===");
        System.out.println("  [1] View Pending Investigation Queue (FIFO)");
        System.out.println("  [2] Dequeue and Assign Next Complaint to Officer");
        System.out.println("  [3] Update Case Status (Pushes to LIFO Undo Stack)");
        System.out.println("  [4] Undo Last Status Change (Pops from LIFO Undo Stack)");
        System.out.print("  Select action [1-4]: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                System.out.println("\n--- Current FIFO Investigation Queue ---");
                List<Complaint> qList = investigationQueue.toList();
                if (qList.isEmpty()) {
                    System.out.println("  Queue is empty. No pending complaints awaiting assignment.");
                } else {
                    for (int i = 0; i < qList.size(); i++) {
                        Complaint c = qList.get(i);
                        System.out.printf("  Position #%d: %s | %s | Pri: %.1f | Status: %s%n",
                                (i + 1), c.getCaseId(), c.getComplainantName(), c.getPriorityScore(), c.getStatus());
                    }
                }
            }
            case "2" -> {
                if (investigationQueue.isEmpty()) {
                    System.out.println("  [!] Queue is empty.");
                    return;
                }
                Complaint assigned = investigationQueue.dequeue();
                assigned.updateStatus("UNDER_INVESTIGATION", "Dispatch Desk", "Assigned from FIFO queue");
                System.out.printf("  [+] Dequeued Case %s (%s). Updated status to UNDER_INVESTIGATION.%n",
                        assigned.getCaseId(), assigned.getComplainantName());
            }
            case "3" -> {
                System.out.print("  Enter Case ID: ");
                String caseId = scanner.nextLine().trim();
                Complaint c = complaintBST.search(caseId);
                if (c == null) {
                    System.out.println("  [!] Case ID not found.");
                    return;
                }
                System.out.println("  Current Status: " + c.getStatus());
                System.out.println("  Select New Status: ");
                System.out.println("    1. UNDER_INVESTIGATION");
                System.out.println("    2. EVIDENCE_COLLECTED");
                System.out.println("    3. RESOLVED");
                System.out.println("    4. CLOSED");
                System.out.print("  Choice [1-4]: ");
                String stChoice = scanner.nextLine().trim();
                String newStatus = switch (stChoice) {
                    case "1" -> "UNDER_INVESTIGATION";
                    case "2" -> "EVIDENCE_COLLECTED";
                    case "3" -> "RESOLVED";
                    case "4" -> "CLOSED";
                    default -> c.getStatus();
                };

                System.out.print("  Enter Officer / Investigator Name: ");
                String officer = scanner.nextLine().trim();
                System.out.print("  Enter Remarks: ");
                String remarks = scanner.nextLine().trim();

                c.updateStatus(newStatus, officer, remarks);
                System.out.printf("  [+] Status updated. Transition recorded on LIFO Stack (Stack Depth: %d).%n",
                        c.getStatusHistoryStack().size());
            }
            case "4" -> {
                System.out.print("  Enter Case ID to undo last status change: ");
                String caseId = scanner.nextLine().trim();
                Complaint c = complaintBST.search(caseId);
                if (c == null) {
                    System.out.println("  [!] Case ID not found.");
                    return;
                }
                StatusHistoryEntry undone = c.undoStatus();
                if (undone != null) {
                    System.out.printf("  [+] UNDO SUCCESSFUL! Popped action from Stack.%n");
                    System.out.printf("      Reverted Status: %s -> %s%n", undone.getNewStatus(), c.getStatus());
                    System.out.printf("      Original Remark: %s (by %s)%n", undone.getRemarks(), undone.getUpdatedBy());
                } else {
                    System.out.println("  [!] Cannot undo further. Case is already at initial REGISTERED state.");
                }
            }
            default -> System.out.println("  [!] Invalid choice.");
        }
    }

    // -------------------------------------------------------------------------
    // 5. Category Binary Tree
    // -------------------------------------------------------------------------
    private static void handleCategoryBinaryTree() {
        System.out.println("\n=== [Module 3] Crime Category Classification Binary Tree ===");
        categoryTree.printTreeStructure();

        System.out.println("\nTraversals:");
        System.out.println("  [1] Preorder Traversal (Root -> Left -> Right) [Top-down Hierarchy]");
        for (CategoryNode node : categoryTree.preorderTraversal()) {
            System.out.printf("      -> [%s] %s%n", node.code, node.name);
        }

        System.out.println("\n  [2] Inorder Traversal (Left -> Root -> Right) [Symmetric Ordering]");
        for (CategoryNode node : categoryTree.inorderTraversal()) {
            System.out.printf("      -> [%s] %s%n", node.code, node.name);
        }

        System.out.println("\n  [3] Postorder Traversal (Left -> Right -> Root) [Bottom-Up Aggregation]");
        for (CategoryNode node : categoryTree.postorderTraversal()) {
            System.out.printf("      -> [%s] %s (Cases: %d, Loss: Rs. %,.0f)%n",
                    node.code, node.name, node.complaintCount, node.totalFinancialLoss);
        }
    }

    // -------------------------------------------------------------------------
    // 6. Fast Case Lookup (BST vs Linear Search vs Binary Search)
    // -------------------------------------------------------------------------
    private static void handleCaseSearch() {
        System.out.println("\n=== [Module 3 & 5] Case Search and Retrieval ===");
        System.out.println("  [1] Lookup by Case ID using Binary Search Tree (BST - O(log n))");
        System.out.println("  [2] Lookup by Case ID using Binary Search on Sorted Array (O(log n))");
        System.out.println("  [3] Free-Text Keyword Search using Linear Search (O(n))");
        System.out.print("  Select option [1-3]: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                System.out.print("  Enter Case ID (e.g., CCRRS-1002): ");
                String id = scanner.nextLine().trim();
                long start = System.nanoTime();
                Complaint c = complaintBST.search(id);
                long elapsed = System.nanoTime() - start;

                if (c != null) {
                    System.out.printf("  [+] Found in BST (%d ns):%n", elapsed);
                    printComplaintDetails(c);
                } else {
                    System.out.println("  [!] Case ID not found in BST.");
                }
            }
            case "2" -> {
                System.out.print("  Enter Case ID (e.g., CCRRS-1004): ");
                String id = scanner.nextLine().trim();
                Complaint[] array = complaintList.toList().toArray(new Complaint[0]);
                Complaint[] sorted = CaseBinarySearch.sortById(array);

                long start = System.nanoTime();
                Complaint c = CaseBinarySearch.search(sorted, id);
                long elapsed = System.nanoTime() - start;

                if (c != null) {
                    System.out.printf("  [+] Found via Binary Search (%d ns):%n", elapsed);
                    printComplaintDetails(c);
                } else {
                    System.out.println("  [!] Case ID not found.");
                }
            }
            case "3" -> {
                System.out.print("  Enter search keyword (complainant name, city, keyword): ");
                String kw = scanner.nextLine().trim();
                List<Complaint> results = CaseLinearSearch.search(complaintList.toList(), kw);
                System.out.printf("  [+] Linear Search matched %d case(s):%n", results.size());
                for (Complaint c : results) {
                    System.out.println("      " + c);
                }
            }
            default -> System.out.println("  [!] Invalid choice.");
        }
    }

    private static void printComplaintDetails(Complaint c) {
        System.out.println("  --------------------------------------------------");
        System.out.println("  Case ID          : " + c.getCaseId());
        System.out.println("  Complainant      : " + c.getComplainantName() + " (" + c.getCity() + ")");
        System.out.println("  Contact          : " + c.getPhone() + " | " + c.getEmail());
        System.out.println("  Category         : " + c.getCategory());
        System.out.println("  Description      : " + c.getDescription());
        System.out.printf("  Financial Loss   : Rs. %,.2f%n", c.getFinancialLoss());
        System.out.printf("  Priority Score   : %.1f (Severity: %d, Victims: %d)%n",
                c.getPriorityScore(), c.getSeverity(), c.getVictimCount());
        System.out.println("  Current Status   : " + c.getStatus());
        System.out.println("  Assigned Officer : " + c.getAssignedOfficerId());
        System.out.println("  Evidence Entities: " + c.getEvidenceEntityIds());
        System.out.println("  --------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // 7. Suspect Network Graph (BFS & DFS)
    // -------------------------------------------------------------------------
    private static void handleNetworkGraph() {
        System.out.println("\n=== [Module 4] Suspect-Device-IP Relationship Graph ===");
        System.out.println("  Entities registered: " + suspectGraph.getVertexCount());
        System.out.println("  Co-occurrence links: " + suspectGraph.getEdgeCount());
        System.out.println("\n  [1] BFS: Trace Entity Network within 'k' Degrees of Connection (Fraud Ring)");
        System.out.println("  [2] DFS: Check Chain of Evidence between Two Entities / Suspects");
        System.out.println("  [3] View All Graph Entities");
        System.out.print("  Select action [1-3]: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                System.out.print("  Enter Start Entity ID (e.g., SUSPECT-01, IP-103.21.244.12): ");
                String startId = scanner.nextLine().trim();
                System.out.print("  Enter Max Degree of Connection (e.g., 2): ");
                int hops = Integer.parseInt(scanner.nextLine().trim());

                Map<String, Integer> results = suspectGraph.bfsDegreeOfConnection(startId, hops);
                if (results.isEmpty()) {
                    System.out.println("  [!] Entity not found in graph.");
                    return;
                }

                System.out.printf("\n  [+] BFS Search from '%s' within %d hops found %d connected nodes:%n",
                        startId, hops, results.size());
                System.out.println("  ----------------------------------------------------------------------------------");
                for (Map.Entry<String, Integer> entry : results.entrySet()) {
                    EvidenceEntity entity = suspectGraph.getEntity(entry.getKey());
                    System.out.printf("  [Hop %d] %-20s | Type: %-14s | %s%n",
                            entry.getValue(), entry.getKey(), entity.getType(), entity.getName());
                }
                System.out.println("  ----------------------------------------------------------------------------------");
            }
            case "2" -> {
                System.out.print("  Enter Origin Entity ID (e.g., SUSPECT-02): ");
                String from = scanner.nextLine().trim();
                System.out.print("  Enter Destination Entity ID (e.g., DEVICE-SERVER-BGLR or PHONE-+919876543210): ");
                String to = scanner.nextLine().trim();

                List<String> path = suspectGraph.dfsFindPath(from, to);
                if (!path.isEmpty()) {
                    System.out.println("\n  [+] DFS Chain of Evidence Path Found (" + (path.size() - 1) + " hops):");
                    for (int i = 0; i < path.size(); i++) {
                        String id = path.get(i);
                        EvidenceEntity e = suspectGraph.getEntity(id);
                        System.out.printf("      Step %d: %s (%s, %s)%n", (i + 1), id, e.getType(), e.getName());
                        if (i < path.size() - 1) {
                            System.out.println("         │");
                            System.out.println("         ▼");
                        }
                    }
                } else {
                    System.out.println("  [-] No connecting evidence path found between these entities.");
                }
            }
            case "3" -> {
                System.out.println("\n  Registered Entities in Cybercrime Network:");
                for (EvidenceEntity e : suspectGraph.getEntityRegistry().values()) {
                    System.out.printf("  %-20s | %-14s | %-32s | %s%n",
                            e.getId(), e.getType(), e.getName(), e.getMetadata());
                }
            }
            default -> System.out.println("  [!] Invalid selection.");
        }
    }

    // -------------------------------------------------------------------------
    // 8. Case Prioritization (Quick Sort)
    // -------------------------------------------------------------------------
    private static void handleQuickSortTriage() {
        System.out.println("\n=== [Module 5] Quick Sort Complaint Prioritization ===");
        List<Complaint> cases = complaintList.toList();
        System.out.println("  Sorting " + cases.size() + " active complaints using Quick Sort by Priority Score descending...");

        long start = System.nanoTime();
        CaseQuickSort.sort(cases);
        long elapsed = System.nanoTime() - start;

        System.out.printf("  [+] Sorted in %d ns. Displaying triage priority list:%n%n", elapsed);
        System.out.println("==========================================================================================");
        System.out.printf("%-6s | %-12s | %-18s | %-20s | %-8s | %s%n",
                "Rank", "Case ID", "Complainant", "Category", "Priority", "Financial Loss");
        System.out.println("------------------------------------------------------------------------------------------");
        for (int i = 0; i < cases.size(); i++) {
            Complaint c = cases.get(i);
            System.out.printf("#%-5d | %-12s | %-18s | %-20s | %8.1f | Rs. %,.0f%n",
                    (i + 1), c.getCaseId(), c.getComplainantName(), c.getCategory(),
                    c.getPriorityScore(), c.getFinancialLoss());
        }
        System.out.println("==========================================================================================");
    }

    // -------------------------------------------------------------------------
    // 9. Algorithmic Decision Support
    // -------------------------------------------------------------------------
    private static void handleAlgorithmicDecisions() {
        System.out.println("\n=== [Module 6] Advanced Algorithmic Decision Support ===");
        System.out.println("  [1] Greedy Officer Allocation (Job Sequencing with Deadlines)");
        System.out.println("  [2] Dynamic Programming Budget Optimization (0/1 Knapsack)");
        System.out.println("  [3] Backtracking Suspect Shortlisting (Constrained Pruning Search)");
        System.out.print("  Select decision tool [1-3]: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                System.out.print("  Enter Total Investigation Days Window (e.g. 7): ");
                int days = Integer.parseInt(scanner.nextLine().trim());
                GreedyOfficerAllocation.ScheduleResult res =
                        GreedyOfficerAllocation.scheduleCases(complaintList.toList(), officerRoster, days);
                res.printSchedule();
            }
            case "2" -> {
                System.out.print("  Enter Total Monthly Forensic Budget in Rs. Thousands (e.g., 60 for Rs. 60,000): ");
                int budget = Integer.parseInt(scanner.nextLine().trim());
                KnapsackBudgetOptimizer.BudgetPlanResult plan =
                        KnapsackBudgetOptimizer.optimizeBudget(complaintList.toList(), budget);
                plan.printPlan();
            }
            case "3" -> {
                System.out.println("\n  Current Evidence Filter Criteria:");
                System.out.println("    - IP Subnet Prefix : 103.21");
                System.out.println("    - Device Brand     : Android");
                System.out.println("    - Fraud Pattern    : UPI_SPOOFING");
                System.out.println("    - Minimum Risk     : 75");

                EvidenceFilter filter = new EvidenceFilter("103.21", "Android", "UPI_SPOOFING", null, 75);
                SearchMetrics metrics = new SearchMetrics();

                List<SuspectCandidate> matched = BacktrackingSuspectSearch.filterSuspects(suspectPool, filter);
                System.out.println("\n  [+] Backtracking Search Result (Shortlisted Suspects):");
                for (SuspectCandidate s : matched) {
                    System.out.println("      " + s);
                }

                System.out.println("\n  Syndicate Pair Combination Search (Syndicate Size = 2):");
                List<List<SuspectCandidate>> combos =
                        BacktrackingSuspectSearch.findSyndicateCombinations(suspectPool, filter, 2, metrics);

                for (int i = 0; i < combos.size(); i++) {
                    System.out.printf("  Syndicate Group #%d:%n", (i + 1));
                    for (SuspectCandidate s : combos.get(i)) {
                        System.out.println("      * " + s.suspectId + " (" + s.name + ", " + s.city + ")");
                    }
                }
                System.out.printf("\n  [Metrics] Nodes Explored: %d | Branches Pruned Early: %d%n",
                        metrics.nodesExplored, metrics.branchesPruned);
            }
            default -> System.out.println("  [!] Invalid choice.");
        }
    }
}
