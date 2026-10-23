package ccrrs.algorithms.decision;
import ccrrs.model.Complaint;
import ccrrs.model.Officer;
import java.util.*;

public class GreedyOfficerAllocation {

    public static class ScheduleResult {
        public final Complaint[] scheduledSlots; // index 1..maxDeadline
        public final int totalCasesScheduled;
        public final double totalPriorityValue;
        public final List<Complaint> unassignedCases;
        public final Map<String, List<Complaint>> officerAssignments;

        public ScheduleResult(Complaint[] scheduledSlots, int totalCasesScheduled,
                              double totalPriorityValue, List<Complaint> unassignedCases,
                              Map<String, List<Complaint>> officerAssignments) {
            this.scheduledSlots = scheduledSlots;
            this.totalCasesScheduled = totalCasesScheduled;
            this.totalPriorityValue = totalPriorityValue;
            this.unassignedCases = unassignedCases;
            this.officerAssignments = officerAssignments;
        }

        public void printSchedule() {
            System.out.println("==========================================================================================");
            System.out.println("               OPTIMAL GREEDY OFFICER ALLOCATION SCHEDULE (Job Sequencing)                ");
            System.out.println("==========================================================================================");
            System.out.printf("%-6s | %-12s | %-20s | %-10s | %-8s | %s%n",
                    "Slot", "Case ID", "Complainant", "Deadline", "Priority", "Assigned Officer");
            System.out.println("------------------------------------------------------------------------------------------");
            for (int t = 1; t < scheduledSlots.length; t++) {
                Complaint c = scheduledSlots[t];
                if (c != null) {
                    System.out.printf("Day %-2d | %-12s | %-20s | Day %-6d | %8.1f | %s%n",
                            t, c.getCaseId(), c.getComplainantName(), c.getDeadlineDays(),
                            c.getPriorityScore(), c.getAssignedOfficerId());
                } else {
                    System.out.printf("Day %-2d | %-12s | %-20s | %-10s | %8s | %s%n",
                            t, "FREE_SLOT", "-", "-", "-", "-");
                }
            }
            System.out.println("------------------------------------------------------------------------------------------");
            System.out.printf("Total Cases Resolved On-Time: %d%n", totalCasesScheduled);
            System.out.printf("Total Priority Value Achieved: %.1f%n", totalPriorityValue);
            System.out.printf("Deferred / Unassigned Cases  : %d%n", unassignedCases.size());
            System.out.println("==========================================================================================");
        }
    }

    /**
     * Executes the Greedy Job Sequencing algorithm.
     *
     * @param complaints List of open complaints.
     * @param officers List of available investigating officers.
     * @param maxTimeSlots Total available time window in days.
     */
    public static ScheduleResult scheduleCases(List<Complaint> complaints, List<Officer> officers, int maxTimeSlots) {
        if (complaints == null || complaints.isEmpty()) {
            return new ScheduleResult(new Complaint[1], 0, 0, Collections.emptyList(), Collections.emptyMap());
        }

        // Step 1: Copy and sort cases by priority score in descending order
        List<Complaint> sortedCases = new ArrayList<>(complaints);
        sortedCases.sort((a, b) -> Double.compare(b.getPriorityScore(), a.getPriorityScore()));

        // Step 2: Determine maximum deadline capped by maxTimeSlots
        int maxDeadline = 0;
        for (Complaint c : sortedCases) {
            if (c.getDeadlineDays() > maxDeadline) {
                maxDeadline = c.getDeadlineDays();
            }
        }
        maxDeadline = Math.min(maxDeadline, maxTimeSlots);
        if (maxDeadline == 0) maxDeadline = maxTimeSlots;

        Complaint[] slots = new Complaint[maxDeadline + 1]; // 1-based indexing for slots
        boolean[] slotOccupied = new boolean[maxDeadline + 1];
        List<Complaint> unassigned = new ArrayList<>();
        Map<String, List<Complaint>> officerMap = new LinkedHashMap<>();

        if (officers != null) {
            for (Officer o : officers) {
                officerMap.put(o.getOfficerId(), new ArrayList<>());
            }
        }

        int scheduledCount = 0;
        double totalPriority = 0.0;
        int officerIndex = 0;

        // Step 3: For each case, greedily find the latest available slot <= deadline
        for (Complaint c : sortedCases) {
            int targetDeadline = Math.min(c.getDeadlineDays(), maxDeadline);
            boolean assigned = false;

            for (int t = targetDeadline; t >= 1; t--) {
                if (!slotOccupied[t]) {
                    slotOccupied[t] = true;
                    slots[t] = c;
                    scheduledCount++;
                    totalPriority += c.getPriorityScore();
                    assigned = true;

                    // Assign an officer in round-robin fashion
                    if (officers != null && !officers.isEmpty()) {
                        Officer officer = officers.get(officerIndex % officers.size());
                        c.setAssignedOfficerId(officer.getOfficerId() + " (" + officer.getName() + ")");
                        officer.incrementAssignedCases();
                        officerMap.get(officer.getOfficerId()).add(c);
                        officerIndex++;
                    }
                    break;
                }
            }

            if (!assigned) {
                unassigned.add(c);
            }
        }

        return new ScheduleResult(slots, scheduledCount, totalPriority, unassigned, officerMap);
    }
}
