package ccrrs.ds.graph;

import ccrrs.ds.linear.LinkedQueue;
import ccrrs.model.EvidenceEntity;
import java.util.*;

/**
 * Module 4: Suspect-Device-IP Relationship Graph.
 * Implemented using an Adjacency List.
 * Vertices: Suspects, Device IMEIs, IP Addresses, Bank Accounts, Phone Numbers.
 * Edges: Co-occurrence in cybercrime complaints and observed interactions.
 *
 * Algorithms:
 * - BFS: Explores entities within 'k' degrees of connection (fraud ring detection).
 * - DFS: Verifies connectivity and reconstructs chain of evidence between two entities.
 */
public class SuspectNetworkGraph {

    private final Map<String, EvidenceEntity> entityRegistry;
    private final Map<String, List<GraphEdge>> adjacencyList;

    public SuspectNetworkGraph() {
        this.entityRegistry = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    /**
     * Adds an evidence entity vertex to the graph.
     */
    public void addVertex(EvidenceEntity entity) {
        if (!entityRegistry.containsKey(entity.getId())) {
            entityRegistry.put(entity.getId(), entity);
            adjacencyList.put(entity.getId(), new ArrayList<>());
        }
    }

    /**
     * Adds an undirected edge between two entities (co-occurring in a complaint).
     */
    public void addEdge(String fromId, String toId, String relationType, String caseId) {
        if (!adjacencyList.containsKey(fromId) || !adjacencyList.containsKey(toId)) {
            return;
        }
        adjacencyList.get(fromId).add(new GraphEdge(toId, relationType, caseId));
        adjacencyList.get(toId).add(new GraphEdge(fromId, relationType, caseId));
    }

    public EvidenceEntity getEntity(String entityId) {
        return entityRegistry.get(entityId);
    }

    public List<GraphEdge> getNeighbors(String entityId) {
        return adjacencyList.getOrDefault(entityId, Collections.emptyList());
    }

    /**
     * Breadth-First Search (BFS):
     * Finds all evidence entities within 'maxDegree' hops of a start entity.
     * Uses our custom LinkedQueue for FIFO frontier exploration.
     *
     * @param startId The target suspect or device ID.
     * @param maxDegree Maximum distance in hops (e.g., 2-hop radius for syndicate detection).
     * @return Map of entity ID to hop distance.
     */
    public Map<String, Integer> bfsDegreeOfConnection(String startId, int maxDegree) {
        Map<String, Integer> distances = new LinkedHashMap<>();
        if (!entityRegistry.containsKey(startId)) {
            return distances;
        }

        LinkedQueue<String> queue = new LinkedQueue<>();
        Set<String> visited = new HashSet<>();

        queue.enqueue(startId);
        visited.add(startId);
        distances.put(startId, 0);

        while (!queue.isEmpty()) {
            String currId = queue.dequeue();
            int currentDist = distances.get(currId);

            if (currentDist >= maxDegree) {
                continue;
            }

            for (GraphEdge edge : adjacencyList.getOrDefault(currId, Collections.emptyList())) {
                String neighborId = edge.getTargetId();
                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    distances.put(neighborId, currentDist + 1);
                    queue.enqueue(neighborId);
                }
            }
        }

        return distances;
    }

    /**
     * Depth-First Search (DFS):
     * Verifies if two suspects/devices are connected through a chain of shared evidence
     * and returns the exact path trace.
     *
     * @param sourceId Origin entity ID
     * @param destinationId Target entity ID
     * @return List of entity IDs representing the path from source to destination, or empty list if no path.
     */
    public List<String> dfsFindPath(String sourceId, String destinationId) {
        List<String> path = new ArrayList<>();
        if (!entityRegistry.containsKey(sourceId) || !entityRegistry.containsKey(destinationId)) {
            return path;
        }

        Set<String> visited = new HashSet<>();
        boolean found = dfsRecursive(sourceId, destinationId, visited, path);
        if (!found) {
            path.clear();
        }
        return path;
    }

    private boolean dfsRecursive(String currentId, String targetId, Set<String> visited, List<String> path) {
        visited.add(currentId);
        path.add(currentId);

        if (currentId.equalsIgnoreCase(targetId)) {
            return true;
        }

        for (GraphEdge edge : adjacencyList.getOrDefault(currentId, Collections.emptyList())) {
            String neighborId = edge.getTargetId();
            if (!visited.contains(neighborId)) {
                if (dfsRecursive(neighborId, targetId, visited, path)) {
                    return true;
                }
            }
        }

        // Backtrack
        path.remove(path.size() - 1);
        return false;
    }

    public int getVertexCount() {
        return entityRegistry.size();
    }

    public int getEdgeCount() {
        int count = 0;
        for (List<GraphEdge> edges : adjacencyList.values()) {
            count += edges.size();
        }
        return count / 2; // Undirected
    }

    public Map<String, EvidenceEntity> getEntityRegistry() {
        return entityRegistry;
    }
}
