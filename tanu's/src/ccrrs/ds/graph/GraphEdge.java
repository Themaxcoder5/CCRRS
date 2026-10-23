package ccrrs.ds.graph;

/**
 * Represents an edge connecting two evidence entities in the cybercrime network graph.
 */
public class GraphEdge {
    private final String targetId;
    private final String relationType;
    private final String complaintId;

    public GraphEdge(String targetId, String relationType, String complaintId) {
        this.targetId = targetId;
        this.relationType = relationType;
        this.complaintId = complaintId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getRelationType() {
        return relationType;
    }

    public String getComplaintId() {
        return complaintId;
    }

    @Override
    public String toString() {
        return String.format("--[%s (Case: %s)]--> %s", relationType, complaintId, targetId);
    }
}
