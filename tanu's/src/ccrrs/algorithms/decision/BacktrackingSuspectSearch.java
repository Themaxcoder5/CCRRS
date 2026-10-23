package ccrrs.algorithms.decision;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 6: Backtracking - Suspect Shortlisting via Constrained Combination Search.
 * Problem:
 * Given multiple independent evidence constraints extracted from a crime scene
 * (IP subnet, device type, fraud pattern, geographic region, minimum risk score),
 * backtracking explores candidate combinations while pruning non-viable partial
 * assignments early, returning only suspects satisfying all criteria.
 */
public class BacktrackingSuspectSearch {

    public static class SuspectCandidate {
        public final String suspectId;
        public final String name;
        public final String ipSubnet;
        public final String deviceType;
        public final String fraudPattern;
        public final String city;
        public final int riskScore; // 1 to 100

        public SuspectCandidate(String suspectId, String name, String ipSubnet,
                                String deviceType, String fraudPattern, String city, int riskScore) {
            this.suspectId = suspectId;
            this.name = name;
            this.ipSubnet = ipSubnet;
            this.deviceType = deviceType;
            this.fraudPattern = fraudPattern;
            this.city = city;
            this.riskScore = riskScore;
        }

        @Override
        public String toString() {
            return String.format("[%s] %-16s | Subnet: %-10s | Device: %-12s | Pattern: %-14s | City: %-10s | Risk: %d",
                    suspectId, name, ipSubnet, deviceType, fraudPattern, city, riskScore);
        }
    }

    public static class EvidenceFilter {
        public String requiredSubnetPrefix; // e.g. "103.21"
        public String requiredDeviceType;   // e.g. "Android"
        public String requiredFraudPattern; // e.g. "UPI_SPOOF"
        public String requiredCity;         // e.g. "Delhi" or null
        public int minRiskScore;            // e.g. 60

        public EvidenceFilter(String requiredSubnetPrefix, String requiredDeviceType,
                              String requiredFraudPattern, String requiredCity, int minRiskScore) {
            this.requiredSubnetPrefix = requiredSubnetPrefix;
            this.requiredDeviceType = requiredDeviceType;
            this.requiredFraudPattern = requiredFraudPattern;
            this.requiredCity = requiredCity;
            this.minRiskScore = minRiskScore;
        }

        public boolean matches(SuspectCandidate s) {
            if (requiredSubnetPrefix != null && !s.ipSubnet.startsWith(requiredSubnetPrefix)) return false;
            if (requiredDeviceType != null && !s.deviceType.toLowerCase().contains(requiredDeviceType.toLowerCase())) return false;
            if (requiredFraudPattern != null && !s.fraudPattern.equalsIgnoreCase(requiredFraudPattern)) return false;
            if (requiredCity != null && !s.city.equalsIgnoreCase(requiredCity)) return false;
            if (s.riskScore < minRiskScore) return false;
            return true;
        }
    }

    public static class SearchMetrics {
        public int nodesExplored = 0;
        public int branchesPruned = 0;
    }

    /**
     * Finds all suspect combinations of size syndicateSize that satisfy the evidence constraints.
     * Uses backtracking with early pruning.
     */
    public static List<List<SuspectCandidate>> findSyndicateCombinations(
            List<SuspectCandidate> pool, EvidenceFilter filter, int syndicateSize, SearchMetrics metrics) {

        List<List<SuspectCandidate>> solutions = new ArrayList<>();
        List<SuspectCandidate> currentCombo = new ArrayList<>();

        backtrack(pool, filter, 0, syndicateSize, currentCombo, solutions, metrics);
        return solutions;
    }

    private static void backtrack(List<SuspectCandidate> pool, EvidenceFilter filter,
                                  int startIndex, int targetSize, List<SuspectCandidate> currentCombo,
                                  List<List<SuspectCandidate>> solutions, SearchMetrics metrics) {

        if (currentCombo.size() == targetSize) {
            solutions.add(new ArrayList<>(currentCombo));
            return;
        }

        for (int i = startIndex; i < pool.size(); i++) {
            SuspectCandidate candidate = pool.get(i);
            metrics.nodesExplored++;

            // Constraint Checking & Early Pruning
            if (!filter.matches(candidate)) {
                // Prune this candidate branch immediately
                metrics.branchesPruned++;
                continue;
            }

            // Choose candidate
            currentCombo.add(candidate);

            // Recurse to next depth
            backtrack(pool, filter, i + 1, targetSize, currentCombo, solutions, metrics);

            // Backtrack (un-choose)
            currentCombo.remove(currentCombo.size() - 1);
        }
    }

    /**
     * Single-suspect constraint filtering using backtracking exploration.
     */
    public static List<SuspectCandidate> filterSuspects(List<SuspectCandidate> pool, EvidenceFilter filter) {
        SearchMetrics metrics = new SearchMetrics();
        List<List<SuspectCandidate>> results = findSyndicateCombinations(pool, filter, 1, metrics);
        List<SuspectCandidate> matched = new ArrayList<>();
        for (List<SuspectCandidate> list : results) {
            matched.add(list.get(0));
        }
        return matched;
    }
}