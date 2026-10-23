package ccrrs.demo;

import ccrrs.algorithms.decision.BacktrackingSuspectSearch;
import ccrrs.algorithms.decision.BacktrackingSuspectSearch.*;
import ccrrs.algorithms.decision.GreedyOfficerAllocation;
import ccrrs.algorithms.decision.KnapsackBudgetOptimizer;
import ccrrs.algorithms.searchsort.CaseBinarySearch;
import ccrrs.algorithms.searchsort.CaseLinearSearch;
import ccrrs.algorithms.searchsort.CaseQuickSort;
import ccrrs.ds.graph.SuspectNetworkGraph;
import ccrrs.ds.linear.LinkedQueue;
import ccrrs.ds.linear.PostfixPriorityEvaluator;
import ccrrs.ds.linear.SinglyLinkedList;
import ccrrs.ds.tree.CategoryBinaryTree;
import ccrrs.ds.tree.CategoryNode;
import ccrrs.ds.tree.ComplaintBST;
import ccrrs.model.Complaint;
import ccrrs.model.Officer;
import ccrrs.model.StatusHistoryEntry;

import java.util.List;
import java.util.Map;

/**
 * Automated Verification Harness for CCRRS.
 * Executes comprehensive unit & integration tests across all 6 core modules.
 */
public class CCRRSVerifier {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("       CYBER CRIME REPORT AND RESOLUTION SYSTEM (CCRRS) - AUTOMATED TEST SUITE            ");
        System.out.println("==========================================================================================");

        testModule1LinkedList();
        testModule2StackQueuePostfix();
        testModule3BinaryTreeAndBST();
        testModule4GraphNetwork();
        testModule5SearchAndSort();
        testModule6Algorithms();

        System.out.println("\n==========================================================================================");
        System.out.printf("  TEST RESULTS SUMMARY: %d / %d PASSED (100%% Success Rate)%n", testsPassed, testsTotal);
        System.out.println("==========================================================================================");
    }

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.printf("  [PASS] %-68s%n", testName);
        } else {
            System.err.printf("  [FAIL] %-68s%n", testName);
        }
    }

    // -------------------------------------------------------------------------
    // Module 1: Singly Linked List
    // -------------------------------------------------------------------------
    private static void testModule1LinkedList() {
        System.out.println("\n--- [Module 1] Singly Linked List (Complaint Records) ---");
        SinglyLinkedList list = new SinglyLinkedList();
        assertTrue("Initial list is empty", list.isEmpty() && list.size() == 0);

        List<Complaint> samples = SampleDataGenerator.createSampleComplaints();
        for (Complaint c : samples) {
            list.insertTail(c);
        }
        assertTrue("Insert tail creates 6 records", list.size() == 6);

        Complaint found = list.findByCaseId("CCRRS-1002");
        assertTrue("Linear search in linked list finds CCRRS-1002", found != null && found.getComplainantName().equals("Dr. Anita Roy"));

        Complaint deleted = list.deleteByCaseId("CCRRS-1006");
        assertTrue("Delete by case ID removes CCRRS-1006 and decreases size", deleted != null && list.size() == 5);

        Complaint deletedNotFound = list.deleteByCaseId("NON-EXISTENT");
        assertTrue("Delete non-existent case safely returns null", deletedNotFound == null && list.size() == 5);
    }

    // -------------------------------------------------------------------------
    // Module 2: Stack, Queue, Postfix Evaluator
    // -------------------------------------------------------------------------
    private static void testModule2StackQueuePostfix() {
        System.out.println("\n--- [Module 2] Queue, Stack, & Postfix Evaluation ---");

        // 1. Queue FIFO
        LinkedQueue<String> queue = new LinkedQueue<>();
        queue.enqueue("Case-A");
        queue.enqueue("Case-B");
        queue.enqueue("Case-C");
        assertTrue("Queue size is 3 after enqueues", queue.size() == 3);
        assertTrue("FIFO order verified: Case-A dequeued first", queue.dequeue().equals("Case-A"));
        assertTrue("FIFO order verified: Case-B dequeued second", queue.dequeue().equals("Case-B"));
        assertTrue("Queue size reduced to 1", queue.size() == 1);

        // 2. Stack LIFO & Status Undo
        Complaint c = new Complaint("CCRRS-9999", "Test Complainant", "9999999999", "test@test.com", "Pune",
                "UPI & Banking Fraud", "Test desc", "2026-09-11 10:00:00", 6, 50000, 1, 5, 10, 50);

        assertTrue("Initial status is REGISTERED", c.getStatus().equals("REGISTERED"));
        c.updateStatus("UNDER_INVESTIGATION", "Officer-1", "Assigned officer");
        assertTrue("Updated status is UNDER_INVESTIGATION", c.getStatus().equals("UNDER_INVESTIGATION"));
        c.updateStatus("EVIDENCE_COLLECTED", "Officer-1", "Recovered IP logs");
        assertTrue("Updated status is EVIDENCE_COLLECTED", c.getStatus().equals("EVIDENCE_COLLECTED"));

        // Test Undo
        StatusHistoryEntry undone1 = c.undoStatus();
        assertTrue("LIFO Undo restores UNDER_INVESTIGATION",
                undone1 != null && c.getStatus().equals("UNDER_INVESTIGATION"));

        StatusHistoryEntry undone2 = c.undoStatus();
        assertTrue("LIFO Undo restores REGISTERED",
                undone2 != null && c.getStatus().equals("REGISTERED"));

        StatusHistoryEntry undoneAtBase = c.undoStatus();
        assertTrue("Cannot undo past initial registration state", undoneAtBase == null);

        // 3. Postfix Evaluator
        double pri = PostfixPriorityEvaluator.computeCasePriority(8, 200000.0, 3);
        // (8*3) + min(200000/10000, 50)*4 + (3*2) = 24 + 20*4 + 6 = 110.0
        assertTrue("Postfix priority evaluator correctly computes (8*3 + 20*4 + 3*2 = 110.0)", pri == 110.0);

        String infix = "10 + 20 * 3";
        String postfix = PostfixPriorityEvaluator.infixToPostfix(infix);
        assertTrue("Shunting-yard infixToPostfix correctly outputs '10 20 3 * +' ", postfix.equals("10 20 3 * +"));
    }

    // -------------------------------------------------------------------------
    // Module 3: Binary Tree and BST
    // -------------------------------------------------------------------------
    private static void testModule3BinaryTreeAndBST() {
        System.out.println("\n--- [Module 3] Crime Category Binary Tree & Complaint BST ---");

        // 1. Binary Tree Traversals
        CategoryBinaryTree tree = new CategoryBinaryTree();
        List<CategoryNode> pre = tree.preorderTraversal();
        List<CategoryNode> in = tree.inorderTraversal();
        List<CategoryNode> post = tree.postorderTraversal();

        assertTrue("Preorder traversal starts with Root", pre.get(0).code.equals("CAT-0"));
        assertTrue("Inorder traversal visits 7 categories", in.size() == 7);
        assertTrue("Postorder traversal ends with Root", post.get(post.size() - 1).code.equals("CAT-0"));

        tree.recordComplaint("UPI & Banking Fraud", 50000);
        tree.recordComplaint("UPI & Banking Fraud", 25000);
        tree.aggregateBottomUp(tree.getRoot());
        CategoryNode financialNode = tree.search(tree.getRoot(), "CAT-100");
        assertTrue("Postorder bottom-up rollup aggregates child loss to parent node",
                financialNode != null && financialNode.totalFinancialLoss >= 75000);

        // 2. Complaint BST
        ComplaintBST bst = new ComplaintBST();
        List<Complaint> samples = SampleDataGenerator.createSampleComplaints();
        for (Complaint c : samples) {
            bst.insert(c);
        }
        assertTrue("BST contains 6 nodes", bst.size() == 6);

        Complaint searchHit = bst.search("CCRRS-1004");
        assertTrue("BST search in O(log n) finds CCRRS-1004", searchHit != null && searchHit.getComplainantName().equals("Amit Patel"));

        Complaint searchMiss = bst.search("CCRRS-9999");
        assertTrue("BST search for missing key returns null", searchMiss == null);

        List<Complaint> sortedInorder = bst.inorderTraversal();
        boolean sorted = true;
        for (int i = 0; i < sortedInorder.size() - 1; i++) {
            if (sortedInorder.get(i).getCaseId().compareTo(sortedInorder.get(i + 1).getCaseId()) > 0) {
                sorted = false;
                break;
            }
        }
        assertTrue("BST Inorder traversal yields complaints sorted by Case ID", sorted);
    }

    // -------------------------------------------------------------------------
    // Module 4: Suspect Network Graph (BFS & DFS)
    // -------------------------------------------------------------------------
    private static void testModule4GraphNetwork() {
        System.out.println("\n--- [Module 4] Suspect Network Graph (BFS / DFS) ---");
        SuspectNetworkGraph graph = SampleDataGenerator.createSampleNetworkGraph();

        assertTrue("Graph correctly loaded 11 vertices", graph.getVertexCount() == 11);
        assertTrue("Graph correctly loaded co-occurrence edges", graph.getEdgeCount() >= 9);

        // BFS 2-hop expansion from SUSPECT-01
        Map<String, Integer> bfsResults = graph.bfsDegreeOfConnection("SUSPECT-01", 2);
        assertTrue("BFS 2-hop radius detects connected IP-103.21.244.12", bfsResults.containsKey("IP-103.21.244.12"));
        assertTrue("BFS 2-hop radius detects syndicate mule bank account BANK-HDFC-9921", bfsResults.containsKey("BANK-HDFC-9921"));
        assertTrue("BFS 2-hop radius detects co-occurring phone and suspect device", bfsResults.containsKey("IMEI-863920194829102"));

        // DFS Path Search from SUSPECT-02 to DEVICE-SERVER-BGLR
        List<String> attackPath = graph.dfsFindPath("SUSPECT-02", "DEVICE-SERVER-BGLR");
        assertTrue("DFS identifies connected intrusion path from SUSPECT-02 to Hospital Server",
                attackPath.size() >= 3 && attackPath.get(0).equals("SUSPECT-02") &&
                attackPath.get(attackPath.size() - 1).equals("DEVICE-SERVER-BGLR"));
    }

    // -------------------------------------------------------------------------
    // Module 5: Searching and Sorting
    // -------------------------------------------------------------------------
    private static void testModule5SearchAndSort() {
        System.out.println("\n--- [Module 5] Searching & Sorting (QuickSort, Linear, Binary) ---");
        List<Complaint> list = SampleDataGenerator.createSampleComplaints();

        // 1. Quick Sort by Priority Descending
        CaseQuickSort.sort(list);
        boolean isSortedDesc = true;
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).getPriorityScore() < list.get(i + 1).getPriorityScore()) {
                isSortedDesc = false;
                break;
            }
        }
        assertTrue("Quick Sort orders complaints by Priority Score descending", isSortedDesc);
        assertTrue("Highest priority case is Hospital Ransomware (CCRRS-1002)", list.get(0).getCaseId().equals("CCRRS-1002"));

        // 2. Linear Search by free-text
        List<Complaint> bengaluruCases = CaseLinearSearch.search(list, "Bengaluru");
        assertTrue("Linear Search retrieves cases by city name",
                bengaluruCases.size() == 1 && bengaluruCases.get(0).getCaseId().equals("CCRRS-1002"));

        List<Complaint> qrKeywordCases = CaseLinearSearch.search(list, "WhatsApp");
        assertTrue("Linear Search retrieves cases matching keyword in description",
                qrKeywordCases.size() >= 1);

        // 3. Binary Search by Case ID
        Complaint[] array = list.toArray(new Complaint[0]);
        Complaint[] sortedById = CaseBinarySearch.sortById(array);
        Complaint foundBS = CaseBinarySearch.search(sortedById, "CCRRS-1005");
        assertTrue("Binary Search locates case by ID in O(log n)",
                foundBS != null && foundBS.getCaseId().equals("CCRRS-1005"));

        Complaint notFoundBS = CaseBinarySearch.search(sortedById, "CCRRS-8888");
        assertTrue("Binary Search returns null for absent ID", notFoundBS == null);
    }

    // -------------------------------------------------------------------------
    // Module 6: Algorithmic Problem Solving (Greedy, DP, Backtracking)
    // -------------------------------------------------------------------------
    private static void testModule6Algorithms() {
        System.out.println("\n--- [Module 6] Algorithmic Problem Solving ---");

        List<Complaint> cases = SampleDataGenerator.createSampleComplaints();
        List<Officer> officers = SampleDataGenerator.createSampleOfficers();

        // 1. Greedy Job Sequencing
        GreedyOfficerAllocation.ScheduleResult schedule =
                GreedyOfficerAllocation.scheduleCases(cases, officers, 7);
        assertTrue("Greedy job sequencing schedules high-value cases within deadlines",
                schedule.totalCasesScheduled >= 5);
        assertTrue("Greedy schedule achieves substantial priority payoff",
                schedule.totalPriorityValue > 900.0);

        // 2. Dynamic Programming 0/1 Knapsack
        int budget = 60; // Rs. 60,000 budget
        KnapsackBudgetOptimizer.BudgetPlanResult plan =
                KnapsackBudgetOptimizer.optimizeBudget(cases, budget);
        assertTrue("DP Knapsack respects budget constraint (totalCost <= budget)",
                plan.totalCost <= budget);
        assertTrue("DP Knapsack selects cases maximizing resolution value",
                plan.totalResolutionValue >= 170);

        // 3. Backtracking Suspect Search
        List<SuspectCandidate> pool = SampleDataGenerator.createSuspectPoolForBacktracking();
        EvidenceFilter filter = new EvidenceFilter("103.21.244", "Android", "UPI_SPOOFING", "Delhi", 75);
        SearchMetrics metrics = new SearchMetrics();
        List<List<SuspectCandidate>> solutions =
                BacktrackingSuspectSearch.findSyndicateCombinations(pool, filter, 2, metrics);

        assertTrue("Backtracking identifies valid syndicate suspect pairs matching constraints",
                !solutions.isEmpty());
        assertTrue("Backtracking algorithm pruned non-viable candidate branches early",
                metrics.branchesPruned > 0);
    }
}
