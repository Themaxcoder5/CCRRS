package ccrrs.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single status change event in a complaint's lifecycle.
 * Pushed onto the complaint's LIFO undo stack.
 */
public class StatusHistoryEntry {
    private final String caseId;
    private final String previousStatus;
    private final String newStatus;
    private final String timestamp;
    private final String updatedBy;
    private final String remarks;

    public StatusHistoryEntry(String caseId, String previousStatus, String newStatus, String updatedBy, String remarks) {
        this.caseId = caseId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.updatedBy = updatedBy;
        this.remarks = remarks;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getCaseId() {
        return caseId;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public String toString() {
        return String.format("[%s] Status changed: %s -> %s by %s (%s)",
                timestamp, previousStatus, newStatus, updatedBy, remarks);
    }
}
