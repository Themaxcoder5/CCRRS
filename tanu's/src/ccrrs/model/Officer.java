package ccrrs.model;

/**
 * Represents an Investigating Officer in the cybercrime unit.
 * Used for Greedy job-sequencing workload allocation.
 */
public class Officer {
    private final String officerId;
    private final String name;
    private final String badgeNumber;
    private final String rank;
    private final String specialization;
    private int assignedCasesCount;

    public Officer(String officerId, String name, String badgeNumber, String rank, String specialization) {
        this.officerId = officerId;
        this.name = name;
        this.badgeNumber = badgeNumber;
        this.rank = rank;
        this.specialization = specialization;
        this.assignedCasesCount = 0;
    }

    public String getOfficerId() {
        return officerId;
    }

    public String getName() {
        return name;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public String getRank() {
        return rank;
    }

    public String getSpecialization() {
        return specialization;
    }

    public int getAssignedCasesCount() {
        return assignedCasesCount;
    }

    public void incrementAssignedCases() {
        this.assignedCasesCount++;
    }

    @Override
    public String toString() {
        return String.format("Officer[%s, %s, %s, Specialization: %s, Active: %d]",
                officerId, name, rank, specialization, assignedCasesCount);
    }
}
