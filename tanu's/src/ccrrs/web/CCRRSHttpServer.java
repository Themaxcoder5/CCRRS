package ccrrs.web;

import ccrrs.algorithms.decision.BacktrackingSuspectSearch;
import ccrrs.algorithms.decision.BacktrackingSuspectSearch.*;
import ccrrs.algorithms.decision.GreedyOfficerAllocation;
import ccrrs.algorithms.decision.KnapsackBudgetOptimizer;
import ccrrs.algorithms.searchsort.CaseBinarySearch;
import ccrrs.algorithms.searchsort.CaseLinearSearch;
import ccrrs.algorithms.searchsort.CaseQuickSort;
import ccrrs.Main;
import ccrrs.database.ComplaintDAO;
import ccrrs.ds.graph.GraphEdge;
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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Embedded REST API and Web Server connecting the Java DSA Backend
 * to the Cybercrime Command Center Web Frontend.
 * Built using Java 17 com.sun.net.httpserver.HttpServer (Zero external dependencies).
 */
public class CCRRSHttpServer {

    public static final int DEFAULT_PORT = 8080;
    private static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "web";

    private final SinglyLinkedList complaintList;
    private final ComplaintBST complaintBST;
    private final LinkedQueue<Complaint> investigationQueue;
    private final CategoryBinaryTree categoryTree;
    private final SuspectNetworkGraph suspectGraph;
    private final List<Officer> officerRoster;
    private final List<SuspectCandidate> suspectPool;

    private HttpServer server;

    public CCRRSHttpServer(SinglyLinkedList list, ComplaintBST bst, LinkedQueue<Complaint> queue,
                           CategoryBinaryTree tree, SuspectNetworkGraph graph,
                           List<Officer> officers, List<SuspectCandidate> pool) {
        this.complaintList = list;
        this.complaintBST = bst;
        this.investigationQueue = queue;
        this.categoryTree = tree;
        this.suspectGraph = graph;
        this.officerRoster = officers;
        this.suspectPool = pool;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static Web Files Handler
        server.createContext("/", new StaticFileHandler());

        // API Endpoints
        server.createContext("/api/complaints", new ComplaintsHandler());
        server.createContext("/api/queue", new QueueHandler());
        server.createContext("/api/queue/dequeue", new DequeueHandler());
        server.createContext("/api/cases/status", new StatusUpdateHandler());
        server.createContext("/api/cases/undo", new UndoStatusHandler());
        server.createContext("/api/tree", new CategoryTreeHandler());
        server.createContext("/api/graph", new GraphDataHandler());
        server.createContext("/api/graph/bfs", new BfsHandler());
        server.createContext("/api/graph/dfs", new DfsHandler());
        server.createContext("/api/sort", new SortHandler());
        server.createContext("/api/search", new SearchHandler());
        server.createContext("/api/algorithms/greedy", new GreedyHandler());
        server.createContext("/api/algorithms/knapsack", new KnapsackHandler());
        server.createContext("/api/algorithms/backtracking", new BacktrackingHandler());

        server.setExecutor(null); // default multithreading
        server.start();

        System.out.println("==========================================================================================");
        System.out.printf("  [+] CCRRS Web Server successfully started on: http://localhost:%d/%n", port);
        System.out.println("  [+] Connected Frontend: HTML5 / CSS3 / JavaScript Cybercrime Command Center Dashboard");
        System.out.println("  [+] Connected Backend : Java 17 Custom Data Structures & Algorithms Core Engine");
        System.out.println("==========================================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // =========================================================================
    // Static Files Handler
    // =========================================================================
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            Path filePath = Paths.get(WEB_ROOT, path.replace('/', File.separatorChar));
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                sendResponse(exchange, 404, "text/plain", "404 Not Found");
                return;
            }

            String mimeType = getMimeType(filePath.toString());
            byte[] bytes = Files.readAllBytes(filePath);

            exchange.getResponseHeaders().set("Content-Type", mimeType + "; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String getMimeType(String filename) {
            if (filename.endsWith(".html")) return "text/html";
            if (filename.endsWith(".css")) return "text/css";
            if (filename.endsWith(".js")) return "application/javascript";
            if (filename.endsWith(".json")) return "application/json";
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".svg")) return "image/svg+xml";
            return "text/plain";
        }
    }

    // =========================================================================
    // API Handlers
    // =========================================================================

    // 1. /api/complaints (GET all, POST new)
    private class ComplaintsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Complaint> list = complaintList.toList();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    json.append(complaintToJson(list.get(i)));
                    if (i < list.size() - 1) json.append(",");
                }
                json.append("]");
                sendResponse(exchange, 200, "application/json", json.toString());

            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                Map<String, String> form = parseFormOrJson(body);

                String name = form.getOrDefault("complainantName", "Anonymous Citizen");
                String phone = form.getOrDefault("phone", "+910000000000");
                String email = form.getOrDefault("email", "citizen@example.com");
                String city = form.getOrDefault("city", "Delhi");
                String category = form.getOrDefault("category", "UPI & Banking Fraud");
                String desc = form.getOrDefault("description", "Cyber incident reported online");
                int severity = Integer.parseInt(form.getOrDefault("severity", "7"));
                double loss = Double.parseDouble(form.getOrDefault("financialLoss", "50000"));
                int victims = Integer.parseInt(form.getOrDefault("victimCount", "1"));

                String caseId = "CCRRS-" + (1000 + complaintList.size() + 1);
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                int deadline = Math.max(2, 8 - (severity / 2));
                int cost = Math.max(5, (int)(loss / 50000) * 5 + 10);
                int resolutionValue = Math.min(100, severity * 8 + victims * 3);

                Complaint c = new Complaint(caseId, name, phone, email, city, category, desc,
                        timestamp, severity, loss, victims, deadline, cost, resolutionValue);

                double priority = PostfixPriorityEvaluator.computeCasePriority(severity, loss, victims);
                c.setPriorityScore(priority);

                // Insert into core data structures
                complaintList.insertTail(c);
                complaintBST.insert(c);
                investigationQueue.enqueue(c);
                categoryTree.recordComplaint(category, loss);
                categoryTree.aggregateBottomUp(categoryTree.getRoot());

                ComplaintDAO.insertComplaint(c);
                // Persist complaint (with its crime classification category) to Supabase as well
                Main.syncComplaintToSupabase(c);

                String resJson = String.format("{\"success\":true,\"caseId\":\"%s\",\"priorityScore\":%.1f,\"message\":\"Complaint registered successfully\"}",
                        caseId, priority);
                sendResponse(exchange, 201, "application/json", resJson);
            } else {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
            }
        }
    }

    // 2. /api/queue (GET FIFO list)
    private class QueueHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            List<Complaint> qList = investigationQueue.toList();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < qList.size(); i++) {
                json.append(complaintToJson(qList.get(i)));
                if (i < qList.size() - 1) json.append(",");
            }
            json.append("]");
            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // 3. /api/queue/dequeue (POST dequeue next)
    private class DequeueHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (investigationQueue.isEmpty()) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"Queue is empty\"}");
                return;
            }
            Complaint c = investigationQueue.dequeue();
            c.updateStatus("UNDER_INVESTIGATION", "Dispatch Desk", "Assigned from FIFO queue");
            String json = String.format("{\"success\":true,\"message\":\"Dequeued case %s\",\"case\":%s}",
                    c.getCaseId(), complaintToJson(c));
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    // 4. /api/cases/status (POST update status)
    private class StatusUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            Map<String, String> params = parseFormOrJson(readRequestBody(exchange));
            String caseId = params.get("caseId");
            String newStatus = params.get("status");
            String officer = params.getOrDefault("officer", "Investigating Officer");
            String remarks = params.getOrDefault("remarks", "Status update");

            Complaint c = complaintBST.search(caseId);
            if (c == null) {
                sendResponse(exchange, 404, "application/json", "{\"error\":\"Case not found\"}");
                return;
            }

            c.updateStatus(newStatus, officer, remarks);
            String json = String.format("{\"success\":true,\"caseId\":\"%s\",\"newStatus\":\"%s\",\"stackDepth\":%d}",
                    c.getCaseId(), c.getStatus(), c.getStatusHistoryStack().size());
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    // 5. /api/cases/undo (POST undo last status change via LIFO stack)
    private class UndoStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            Map<String, String> params = parseFormOrJson(readRequestBody(exchange));
            String caseId = params.get("caseId");

            Complaint c = complaintBST.search(caseId);
            if (c == null) {
                sendResponse(exchange, 404, "application/json", "{\"error\":\"Case not found\"}");
                return;
            }

            StatusHistoryEntry undone = c.undoStatus();
            if (undone != null) {
                String json = String.format("{\"success\":true,\"revertedStatus\":\"%s\",\"previousStatus\":\"%s\",\"remarks\":\"%s\",\"stackDepth\":%d}",
                        c.getStatus(), undone.getNewStatus(), escape(undone.getRemarks()), c.getStatusHistoryStack().size());
                sendResponse(exchange, 200, "application/json", json);
            } else {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"Cannot undo further. Already at initial status.\"}");
            }
        }
    }

    // 6. /api/tree (GET category hierarchy & traversals)
    private class CategoryTreeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            CategoryNode root = categoryTree.getRoot();

            List<CategoryNode> pre = categoryTree.preorderTraversal();
            List<CategoryNode> in = categoryTree.inorderTraversal();
            List<CategoryNode> post = categoryTree.postorderTraversal();

            StringBuilder json = new StringBuilder("{");
            json.append("\"tree\":").append(categoryNodeToJson(root)).append(",");
            json.append("\"preorder\":[").append(nodesToJsonArray(pre)).append("],");
            json.append("\"inorder\":[").append(nodesToJsonArray(in)).append("],");
            json.append("\"postorder\":[").append(nodesToJsonArray(post)).append("]");
            json.append("}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }

        private String nodesToJsonArray(List<CategoryNode> nodes) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nodes.size(); i++) {
                CategoryNode n = nodes.get(i);
                sb.append(String.format("{\"code\":\"%s\",\"name\":\"%s\",\"cases\":%d,\"loss\":%.0f}",
                        n.code, escape(n.name), n.complaintCount, n.totalFinancialLoss));
                if (i < nodes.size() - 1) sb.append(",");
            }
            return sb.toString();
        }

        private String categoryNodeToJson(CategoryNode node) {
            if (node == null) return "null";
            return String.format("{\"code\":\"%s\",\"name\":\"%s\",\"desc\":\"%s\",\"cases\":%d,\"loss\":%.0f,\"left\":%s,\"right\":%s}",
                    node.code, escape(node.name), escape(node.description), node.complaintCount, node.totalFinancialLoss,
                    categoryNodeToJson(node.left), categoryNodeToJson(node.right));
        }
    }

    // 7. /api/graph (GET vertices & edges)
    private class GraphDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            StringBuilder json = new StringBuilder("{\"vertices\":[");
            int vi = 0;
            for (EvidenceEntity e : suspectGraph.getEntityRegistry().values()) {
                json.append(String.format("{\"id\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"metadata\":\"%s\"}",
                        e.getId(), escape(e.getName()), e.getType(), escape(e.getMetadata())));
                if (++vi < suspectGraph.getEntityRegistry().size()) json.append(",");
            }
            json.append("],\"edges\":[");

            Set<String> visitedEdges = new HashSet<>();
            List<String> edgeListJson = new ArrayList<>();

            for (EvidenceEntity e : suspectGraph.getEntityRegistry().values()) {
                for (GraphEdge edge : suspectGraph.getNeighbors(e.getId())) {
                    String u = e.getId();
                    String v = edge.getTargetId();
                    String edgeKey = u.compareTo(v) < 0 ? u + "--" + v : v + "--" + u;

                    if (!visitedEdges.contains(edgeKey)) {
                        visitedEdges.add(edgeKey);
                        edgeListJson.add(String.format("{\"source\":\"%s\",\"target\":\"%s\",\"relation\":\"%s\",\"caseId\":\"%s\"}",
                                u, v, edge.getRelationType(), edge.getComplaintId()));
                    }
                }
            }
            json.append(String.join(",", edgeListJson));
            json.append("]}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // 8. /api/graph/bfs (GET startId, hops)
    private class BfsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            String start = query.getOrDefault("start", "SUSPECT-01");
            int hops = Integer.parseInt(query.getOrDefault("hops", "2"));

            Map<String, Integer> results = suspectGraph.bfsDegreeOfConnection(start, hops);
            StringBuilder json = new StringBuilder("{\"start\":\"" + start + "\",\"maxHops\":" + hops + ",\"connected\":[");
            int i = 0;
            for (Map.Entry<String, Integer> entry : results.entrySet()) {
                EvidenceEntity ent = suspectGraph.getEntity(entry.getKey());
                json.append(String.format("{\"id\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"hop\":%d}",
                        entry.getKey(), ent != null ? escape(ent.getName()) : "", ent != null ? ent.getType().name() : "", entry.getValue()));
                if (++i < results.size()) json.append(",");
            }
            json.append("]}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // 9. /api/graph/dfs (GET from, to)
    private class DfsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            String from = query.getOrDefault("from", "SUSPECT-02");
            String to = query.getOrDefault("to", "DEVICE-SERVER-BGLR");

            List<String> path = suspectGraph.dfsFindPath(from, to);
            StringBuilder json = new StringBuilder("{\"from\":\"" + from + "\",\"to\":\"" + to + "\",\"found\":" + (!path.isEmpty()) + ",\"path\":[");
            for (int i = 0; i < path.size(); i++) {
                EvidenceEntity ent = suspectGraph.getEntity(path.get(i));
                json.append(String.format("{\"id\":\"%s\",\"name\":\"%s\",\"type\":\"%s\"}",
                        path.get(i), ent != null ? escape(ent.getName()) : "", ent != null ? ent.getType().name() : ""));
                if (i < path.size() - 1) json.append(",");
            }
            json.append("]}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // 10. /api/sort (QuickSort cases)
    private class SortHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            List<Complaint> cases = complaintList.toList();

            long start = System.nanoTime();
            CaseQuickSort.sort(cases);
            long elapsed = System.nanoTime() - start;

            StringBuilder json = new StringBuilder("{\"executionTimeNanos\":" + elapsed + ",\"sortedCases\":[");
            for (int i = 0; i < cases.size(); i++) {
                json.append(complaintToJson(cases.get(i)));
                if (i < cases.size() - 1) json.append(",");
            }
            json.append("]}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // 11. /api/search (BST / Binary Search / Linear Search)
    private class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            String q = query.getOrDefault("q", "");
            String type = query.getOrDefault("type", "bst").toLowerCase();

            long start = System.nanoTime();
            List<Complaint> matches = new ArrayList<>();

            if ("bst".equals(type)) {
                Complaint hit = complaintBST.search(q);
                if (hit != null) matches.add(hit);
            } else if ("binary".equals(type)) {
                Complaint[] arr = CaseBinarySearch.sortById(complaintList.toList().toArray(new Complaint[0]));
                Complaint hit = CaseBinarySearch.search(arr, q);
                if (hit != null) matches.add(hit);
            } else {
                matches = CaseLinearSearch.search(complaintList.toList(), q);
            }
            long elapsed = System.nanoTime() - start;

            StringBuilder json = new StringBuilder("{\"query\":\"" + escape(q) + "\",\"type\":\"" + type + "\",\"timeNanos\":" + elapsed + ",\"matches\":[");
            for (int i = 0; i < matches.size(); i++) {
                json.append(complaintToJson(matches.get(i)));
                if (i < matches.size() - 1) json.append(",");
            }
            json.append("]}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // 12. /api/algorithms/greedy (Job Sequencing)
    private class GreedyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            int days = Integer.parseInt(query.getOrDefault("days", "7"));

            GreedyOfficerAllocation.ScheduleResult result =
                    GreedyOfficerAllocation.scheduleCases(complaintList.toList(), officerRoster, days);

            StringBuilder json = new StringBuilder("{");
            json.append("\"totalScheduled\":").append(result.totalCasesScheduled).append(",");
            json.append("\"totalPriority\":").append(result.totalPriorityValue).append(",");
            json.append("\"slots\":[");
            for (int t = 1; t < result.scheduledSlots.length; t++) {
                Complaint c = result.scheduledSlots[t];
                json.append(String.format("{\"day\":%d,\"case\":%s}", t, c != null ? complaintToJson(c) : "null"));
                if (t < result.scheduledSlots.length - 1) json.append(",");
            }
            json.append("],\"unassigned\":[");
            for (int i = 0; i < result.unassignedCases.size(); i++) {
                json.append(complaintToJson(result.unassignedCases.get(i)));
                if (i < result.unassignedCases.size() - 1) json.append(",");
            }
            json.append("]}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // 13. /api/algorithms/knapsack (0/1 Knapsack DP)
    private class KnapsackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            int budget = Integer.parseInt(query.getOrDefault("budget", "60"));

            KnapsackBudgetOptimizer.BudgetPlanResult plan =
                    KnapsackBudgetOptimizer.optimizeBudget(complaintList.toList(), budget);

            StringBuilder json = new StringBuilder("{");
            json.append("\"budgetLimit\":").append(plan.budgetLimit).append(",");
            json.append("\"totalCost\":").append(plan.totalCost).append(",");
            json.append("\"remainingBudget\":").append(plan.remainingBudget).append(",");
            json.append("\"totalResolutionValue\":").append(plan.totalResolutionValue).append(",");
            json.append("\"fundedCases\":[");
            for (int i = 0; i < plan.fundedCases.size(); i++) {
                json.append(complaintToJson(plan.fundedCases.get(i)));
                if (i < plan.fundedCases.size() - 1) json.append(",");
            }
            json.append("]}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    // 14. /api/algorithms/backtracking (Constrained Combination Search)
    private class BacktrackingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                query.putAll(parseFormOrJson(readRequestBody(exchange)));
            }

            String subnet = query.getOrDefault("subnet", "103.21");
            String device = query.getOrDefault("device", "Android");
            String pattern = query.getOrDefault("pattern", "UPI_SPOOFING");
            int minRisk = Integer.parseInt(query.getOrDefault("minRisk", "75"));
            int groupSize = Integer.parseInt(query.getOrDefault("groupSize", "2"));

            EvidenceFilter filter = new EvidenceFilter(subnet, device, pattern, null, minRisk);
            SearchMetrics metrics = new SearchMetrics();

            List<List<SuspectCandidate>> combos =
                    BacktrackingSuspectSearch.findSyndicateCombinations(suspectPool, filter, groupSize, metrics);

            List<SuspectCandidate> singles =
                    BacktrackingSuspectSearch.filterSuspects(suspectPool, filter);

            StringBuilder json = new StringBuilder("{");
            json.append("\"metrics\":{\"explored\":").append(metrics.nodesExplored)
                .append(",\"pruned\":").append(metrics.branchesPruned).append("},");
            json.append("\"shortlisted\":[");
            for (int i = 0; i < singles.size(); i++) {
                json.append(suspectToJson(singles.get(i)));
                if (i < singles.size() - 1) json.append(",");
            }
            json.append("],\"syndicates\":[");
            for (int i = 0; i < combos.size(); i++) {
                json.append("[");
                List<SuspectCandidate> group = combos.get(i);
                for (int j = 0; j < group.size(); j++) {
                    json.append(suspectToJson(group.get(j)));
                    if (j < group.size() - 1) json.append(",");
                }
                json.append("]");
                if (i < combos.size() - 1) json.append(",");
            }
            json.append("]}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }

        private String suspectToJson(SuspectCandidate s) {
            return String.format("{\"id\":\"%s\",\"name\":\"%s\",\"ip\":\"%s\",\"device\":\"%s\",\"pattern\":\"%s\",\"city\":\"%s\",\"risk\":%d}",
                    s.suspectId, escape(s.name), s.ipSubnet, escape(s.deviceType), s.fraudPattern, escape(s.city), s.riskScore);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private String complaintToJson(Complaint c) {
        if (c == null) return "null";
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"caseId\":\"").append(c.getCaseId()).append("\",");
        sb.append("\"complainantName\":\"").append(escape(c.getComplainantName())).append("\",");
        sb.append("\"phone\":\"").append(escape(c.getPhone())).append("\",");
        sb.append("\"email\":\"").append(escape(c.getEmail())).append("\",");
        sb.append("\"city\":\"").append(escape(c.getCity())).append("\",");
        sb.append("\"category\":\"").append(escape(c.getCategory())).append("\",");
        sb.append("\"description\":\"").append(escape(c.getDescription())).append("\",");
        sb.append("\"timestamp\":\"").append(c.getTimestamp()).append("\",");
        sb.append("\"severity\":").append(c.getSeverity()).append(",");
        sb.append("\"financialLoss\":").append(c.getFinancialLoss()).append(",");
        sb.append("\"victimCount\":").append(c.getVictimCount()).append(",");
        sb.append("\"priorityScore\":").append(c.getPriorityScore()).append(",");
        sb.append("\"status\":\"").append(c.getStatus()).append("\",");
        sb.append("\"assignedOfficer\":\"").append(escape(c.getAssignedOfficerId())).append("\",");
        sb.append("\"deadlineDays\":").append(c.getDeadlineDays()).append(",");
        sb.append("\"investigationCost\":").append(c.getInvestigationCost()).append(",");
        sb.append("\"expectedResolutionValue\":").append(c.getExpectedResolutionValue()).append(",");
        sb.append("\"stackDepth\":").append(c.getStatusHistoryStack().size()).append(",");
        sb.append("\"evidenceEntityIds\":[");
        for (int i = 0; i < c.getEvidenceEntityIds().size(); i++) {
            sb.append("\"").append(c.getEvidenceEntityIds().get(i)).append("\"");
            if (i < c.getEvidenceEntityIds().size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseFormOrJson(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;

        body = body.trim();
        if (body.startsWith("{") && body.endsWith("}")) {
            // Simple JSON key-value extraction
            body = body.substring(1, body.length() - 1);
            String[] pairs = body.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String pair : pairs) {
                String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (kv.length == 2) {
                    String k = kv[0].trim().replaceAll("^\"|\"$", "");
                    String v = kv[1].trim().replaceAll("^\"|\"$", "");
                    map.put(k, v);
                }
            }
        } else {
            // URL encoded form
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            }
        }
        return map;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
