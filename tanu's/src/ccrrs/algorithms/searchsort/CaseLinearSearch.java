package ccrrs.algorithms.searchsort;

import ccrrs.model.Complaint;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 5: Linear Search for Free-Text Keyword Retrieval.
 * Searches across non-indexed and free-text fields:
 * - Complainant Name
 * - City
 * - Phone Number
 * - Description Keywords
 *
 * Time Complexity: O(n)
 */
public class CaseLinearSearch {

    public static List<Complaint> search(List<Complaint> complaints, String keyword) {
        List<Complaint> matches = new ArrayList<>();
        if (complaints == null || keyword == null || keyword.trim().isEmpty()) {
            return matches;
        }

        String query = keyword.trim().toLowerCase();

        for (Complaint c : complaints) {
            if (c.getComplainantName().toLowerCase().contains(query) ||
                c.getCity().toLowerCase().contains(query) ||
                c.getPhone().contains(query) ||
                c.getCategory().toLowerCase().contains(query) ||
                c.getDescription().toLowerCase().contains(query)) {
                matches.add(c);
            }
        }

        return matches;
    }
}
