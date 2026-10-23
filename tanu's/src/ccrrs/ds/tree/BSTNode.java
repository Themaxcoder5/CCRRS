package ccrrs.ds.tree;

import ccrrs.model.Complaint;

/**
 * Node for the Complaint Binary Search Tree (BST).
 * Key: Case ID string (e.g., "CCRRS-1001")
 * Value: Complaint reference
 */
public class BSTNode {
    public String caseId;
    public Complaint complaint;
    public BSTNode left;
    public BSTNode right;

    public BSTNode(Complaint complaint) {
        this.caseId = complaint.getCaseId();
        this.complaint = complaint;
        this.left = null;
        this.right = null;
    }
}
