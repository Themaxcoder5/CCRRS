package ccrrs.algorithms.searchsort;

import ccrrs.model.Complaint;
import java.util.List;

public class CaseQuickSort {

    public static void sort(Complaint[] complaints) {
        if (complaints == null || complaints.length <= 1) return;
        quickSort(complaints, 0, complaints.length - 1);
    }

    public static void sort(List<Complaint> complaintList) {
        if (complaintList == null || complaintList.size() <= 1) return;
        Complaint[] array = complaintList.toArray(new Complaint[0]);
        quickSort(array, 0, array.length - 1);
        complaintList.clear();
        for (Complaint c : array) {
            complaintList.add(c);
        }
    }

    private static void quickSort(Complaint[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private static int partition(Complaint[] arr, int low, int high) {
        Complaint pivot = arr[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (compare(arr[j], pivot) < 0) { // Higher priority comes first
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    /**
     * Comparison function:
     * Returns negative if c1 has higher priority than c2.
     */
    private static int compare(Complaint c1, Complaint c2) {
        if (c1.getPriorityScore() != c2.getPriorityScore()) {
            // Descending order of priority score
            return Double.compare(c2.getPriorityScore(), c1.getPriorityScore());
        }
        // Secondary: Ascending order of Case ID
        return c1.getCaseId().compareToIgnoreCase(c2.getCaseId());
    }

    private static void swap(Complaint[] arr, int i, int j) {
        Complaint temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
