package ccrrs.model;

import ccrrs.ds.linear.LinkedStack;
import java.util.ArrayList;
import java.util.List;

/**
 * Core Domain Model representing a Cyber Crime Complaint in CCRRS.
 * Contains all complaint metadata, investigative parameters, evidence connections,
 * and an attached LIFO Stack for undoing status changes.
 */
public class Complaint {
    private final String caseId;
    private final String complainantName;
    private final String phone;
    private final String email;
    private final String city;
    private final String category;
    private final String description;
    private final String timestamp;

    // Quantitative metrics
    private final int severity;            // 1 to 10
    private final double financialLoss;     // Amount in INR
    private final int victimCount;         // Number of affected persons
    private double priorityScore;          // Computed via postfix stack evaluator

    // Lifecycle status
    private String status;                 // REGISTERED, UNDER_INVESTIGATION, EVIDENCE_COLLECTED, RESOLVED, CLOSED
    private String assignedOfficerId;

    // Decision-support attributes
    private int deadlineDays;              // Deadline in days for Greedy Job Sequencing
    private int investigationCost;         // Cost (in ₹ thousands) for DP 0/1 Knapsack
    private int expectedResolutionValue;   // Strategic payoff value (1-100) for DP Knapsack

    // Connected evidence references
    private final List<String> evidenceEntityIds;

    // LIFO Stack for status change history and undo operations
    private final LinkedStack<StatusHistoryEntry> statusHistoryStack;

    public Complaint(String caseId, String complainantName, String phone, String email, String city,
                     String category, String description, String timestamp, int severity,
                     double financialLoss, int victimCount, int deadlineDays, int investigationCost,
                     int expectedResolutionValue) {
        this.caseId = caseId;
        this.complainantName = complainantName;
        this.phone = phone;
        this.email = email;
        this.city = city;
        this.category = category;
        this.description = description;
        this.timestamp = timestamp;
        this.severity = severity;
        this.financialLoss = financialLoss;
        this.victimCount = victimCount;
        this.status = "REGISTERED";
        this.assignedOfficerId = "UNASSIGNED";
        this.deadlineDays = deadlineDays;
        this.investigationCost = investigationCost;
        this.expectedResolutionValue = expectedResolutionValue;
        this.priorityScore = 0.0;
        this.evidenceEntityIds = new ArrayList<>();
        this.statusHistoryStack = new LinkedStack<>();

        // Push initial status to history
        this.statusHistoryStack.push(new StatusHistoryEntry(caseId, "NONE", "REGISTERED", "SYSTEM", "Initial Registration"));
    }

    // Getters and Setters
    public String getCaseId() {
        return caseId;
    }

    public String getComplainantName() {
        return complainantName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getCity() {
        return city;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getSeverity() {
        return severity;
    }

    public double getFinancialLoss() {
        return financialLoss;
    }

    public int getVictimCount() {
        return victimCount;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public String getStatus() {
        return status;
    }

    public String getAssignedOfficerId() {
        return assignedOfficerId;
    }

    public void setAssignedOfficerId(String assignedOfficerId) {
        this.assignedOfficerId = assignedOfficerId;
    }

    public int getDeadlineDays() {
        return deadlineDays;
    }

    public void setDeadlineDays(int deadlineDays) {
        this.deadlineDays = deadlineDays;
    }

    public int getInvestigationCost() {
        return investigationCost;
    }

    public int getExpectedResolutionValue() {
        return expectedResolutionValue;
    }

    public List<String> getEvidenceEntityIds() {
        return evidenceEntityIds;
    }

    public void addEvidenceEntityId(String entityId) {
        if (!evidenceEntityIds.contains(entityId)) {
            evidenceEntityIds.add(entityId);
        }
    }

    public LinkedStack<StatusHistoryEntry> getStatusHistoryStack() {
        return statusHistoryStack;
    }

    /**
     * Updates the status of the complaint and records the transition on the LIFO undo stack.
     */
    public void updateStatus(String newStatus, String officerName, String remarks) {
        String oldStatus = this.status;
        this.status = newStatus;
        StatusHistoryEntry entry = new StatusHistoryEntry(caseId, oldStatus, newStatus, officerName, remarks);
        statusHistoryStack.push(entry);
    }

    /**
     * Reverts the most recent status change using the LIFO undo stack.
     * @return The reverted StatusHistoryEntry, or null if cannot undo further.
     */
    public StatusHistoryEntry undoStatus() {
        if (statusHistoryStack.size() <= 1) {
            // Cannot undo past the initial REGISTERED state
            return null;
        }
        StatusHistoryEntry lastEntry = statusHistoryStack.pop();
        this.status = lastEntry.getPreviousStatus();
        return lastEntry;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Cat: %-18s | Pri: %6.1f | Status: %-18s | Officer: %-10s | Loss: ₹%,.0f",
                caseId, complainantName, category, priorityScore, status, assignedOfficerId, financialLoss);
    }
}
