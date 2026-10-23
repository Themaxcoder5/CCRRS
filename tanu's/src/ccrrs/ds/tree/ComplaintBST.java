package ccrrs.ds.tree;

import ccrrs.model.Complaint;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 3: Binary Search Tree (BST) for Fast Case Lookup.
 * All complaints are indexed by Case ID (e.g. "CCRRS-1001").
 * Average Time Complexities:
 * - Insert: O(log n)
 * - Search: O(log n)
 * Inorder traversal yields complaints sorted lexicographically by Case ID.
 */
public class ComplaintBST {
    private BSTNode root;
    private int size;

    public ComplaintBST() {
        this.root = null;
        this.size = 0;
    }

    /**
     * Inserts a complaint into the BST keyed on its caseId.
     * Time Complexity: O(log n) average, O(n) worst case.
     */
    public void insert(Complaint complaint) {
        if (complaint == null) return;
        root = insertRecursive(root, complaint);
    }

    private BSTNode insertRecursive(BSTNode curr, Complaint complaint) {
        if (curr == null) {
            size++;
            return new BSTNode(complaint);
        }

        int cmp = complaint.getCaseId().compareToIgnoreCase(curr.caseId);
        if (cmp < 0) {
            curr.left = insertRecursive(curr.left, complaint);
        } else if (cmp > 0) {
            curr.right = insertRecursive(curr.right, complaint);
        } else {
            // Duplicate caseId -> overwrite value
            curr.complaint = complaint;
        }
        return curr;
    }

    /**
     * Searches for a complaint by Case ID in O(log n) average time.
     * @return Complaint object if found, null otherwise.
     */
    public Complaint search(String caseId) {
        if (caseId == null || root == null) return null;
        return searchRecursive(root, caseId.trim());
    }

    private Complaint searchRecursive(BSTNode curr, String caseId) {
        if (curr == null) return null;

        int cmp = caseId.compareToIgnoreCase(curr.caseId);
        if (cmp == 0) {
            return curr.complaint;
        } else if (cmp < 0) {
            return searchRecursive(curr.left, caseId);
        } else {
            return searchRecursive(curr.right, caseId);
        }
    }

    /**
     * Inorder Traversal: Left -> Root -> Right
     * Yields all complaints sorted by Case ID.
     */
    public List<Complaint> inorderTraversal() {
        List<Complaint> result = new ArrayList<>();
        inorder(root, result);
        return result;
    }

    private void inorder(BSTNode curr, List<Complaint> result) {
        if (curr == null) return;
        inorder(curr.left, result);
        result.add(curr.complaint);
        inorder(curr.right, result);
    }

    public Complaint findMin() {
        if (root == null) return null;
        BSTNode curr = root;
        while (curr.left != null) {
            curr = curr.left;
        }
        return curr.complaint;
    }

    public Complaint findMax() {
        if (root == null) return null;
        BSTNode curr = root;
        while (curr.right != null) {
            curr = curr.right;
        }
        return curr.complaint;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
