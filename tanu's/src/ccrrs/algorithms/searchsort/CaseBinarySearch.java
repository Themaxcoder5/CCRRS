package ccrrs.algorithms.searchsort;
import ccrrs.model.Complaint;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Module 5: Binary Search for Case Lookup.
 * Quickly locates a complaint by Case ID on a sorted array of complaints.
 *
 * Time Complexity: O(log n)
 * Space Complexity: O(1)
 */
public class CaseBinarySearch {

    /**
     * Performs binary search on an array of complaints sorted by Case ID.
     * @param sortedCases Array of complaints sorted by Case ID ascending.
     * @param targetCaseId The case ID to look up (e.g. "CCRRS-1003").
     * @return The matching Complaint, or null if not found.
     */
    public static Complaint search(Complaint[] sortedCases, String targetCaseId) {
        if (sortedCases == null || targetCaseId == null || sortedCases.length == 0) {
            return null;
        }

        int low = 0;
        int high = sortedCases.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = sortedCases[mid].getCaseId().compareToIgnoreCase(targetCaseId);

            if (cmp == 0) {
                return sortedCases[mid];
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return null;
    }

    /**
     * Utility to sort complaints by Case ID ascending prior to Binary Search.
     */
    public static Complaint[] sortById(Complaint[] cases) {
        Complaint[] copy = Arrays.copyOf(cases, cases.length);
        Arrays.sort(copy, Comparator.comparing(Complaint::getCaseId, String.CASE_INSENSITIVE_ORDER));
        return copy;
    }
}
