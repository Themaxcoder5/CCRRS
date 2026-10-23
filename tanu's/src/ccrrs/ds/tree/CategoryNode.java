package ccrrs.ds.tree;

/**
 * Node structure for the Crime Category Classification Binary Tree.
 */
public class CategoryNode {
    public String name;
    public String code;
    public String description;
    public int complaintCount;
    public double totalFinancialLoss;
    public CategoryNode left;
    public CategoryNode right;

    public CategoryNode(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.complaintCount = 0;
        this.totalFinancialLoss = 0.0;
        this.left = null;
        this.right = null;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Cases: %d, Loss: Rs. %,.0f)", code, name, complaintCount, totalFinancialLoss);
    }
}
