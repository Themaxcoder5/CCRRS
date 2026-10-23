package ccrrs.ds.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 3: Binary Tree for Crime Category Classification.
 * Organizes cybercrime categories and subcategories into a hierarchical binary tree.
 * Implements:
 * - Preorder Traversal: Top-down hierarchical export / breadcrumb structure.
 * - Inorder Traversal: Symmetric category listing.
 * - Postorder Traversal: Bottom-up aggregation of case counts and financial impact.
 */
public class CategoryBinaryTree {
    private CategoryNode root;

    public CategoryBinaryTree() {
        buildDefaultHierarchy();
    }

    /**
     * Initializes the cybercrime category binary tree hierarchy.
     */
    public void buildDefaultHierarchy() {
        // Root
        root = new CategoryNode("CAT-0", "Cybercrime Incidents", "Root classification node for all cyber offences");

        // Level 1
        root.left = new CategoryNode("CAT-100", "Financial Cybercrimes", "Crimes involving monetary theft, bank fraud, and crypto");
        root.right = new CategoryNode("CAT-200", "Cyber Intrusions & Harassment", "Crimes involving system breaches, privacy leaks, and stalking");

        // Level 2 - Left Subtree (Financial)
        root.left.left = new CategoryNode("CAT-110", "UPI & Banking Fraud", "Unauthorized UPI transfers, OTP scams, and ATM cloning");
        root.left.right = new CategoryNode("CAT-120", "Phishing & Identity Theft", "Credential harvesting, fake web portals, and impersonation");

        // Level 2 - Right Subtree (Intrusions/Harassment)
        root.right.left = new CategoryNode("CAT-210", "Hacking & Ransomware", "Server unauthorized access, malware injection, and ransomware");
        root.right.right = new CategoryNode("CAT-220", "Cyber Harassment & Defamation", "Online stalking, morphed media blackmail, and hate speech");
    }

    public CategoryNode getRoot() {
        return root;
    }

    /**
     * Records an incoming complaint into the matching category node.
     */
    public boolean recordComplaint(String categoryName, double loss) {
        CategoryNode node = search(root, categoryName);
        if (node != null) {
            node.complaintCount++;
            node.totalFinancialLoss += loss;
            return true;
        }
        return false;
    }

    public CategoryNode search(CategoryNode curr, String name) {
        if (curr == null) return null;
        if (curr.name.equalsIgnoreCase(name) || curr.code.equalsIgnoreCase(name) ||
                curr.name.toLowerCase().contains(name.toLowerCase())) {
            return curr;
        }
        CategoryNode found = search(curr.left, name);
        if (found != null) return found;
        return search(curr.right, name);
    }

    /**
     * Preorder Traversal: Root -> Left -> Right
     * Used for top-down export of category hierarchy.
     */
    public List<CategoryNode> preorderTraversal() {
        List<CategoryNode> list = new ArrayList<>();
        preorder(root, list);
        return list;
    }

    private void preorder(CategoryNode node, List<CategoryNode> list) {
        if (node == null) return;
        list.add(node);
        preorder(node.left, list);
        preorder(node.right, list);
    }

    /**
     * Inorder Traversal: Left -> Root -> Right
     * Used for symmetric category traversal.
     */
    public List<CategoryNode> inorderTraversal() {
        List<CategoryNode> list = new ArrayList<>();
        inorder(root, list);
        return list;
    }

    private void inorder(CategoryNode node, List<CategoryNode> list) {
        if (node == null) return;
        inorder(node.left, list);
        list.add(node);
        inorder(node.right, list);
    }

    /**
     * Postorder Traversal: Left -> Right -> Root
     * Used for bottom-up aggregation of case counts and financial loss rollups.
     */
    public List<CategoryNode> postorderTraversal() {
        List<CategoryNode> list = new ArrayList<>();
        postorder(root, list);
        return list;
    }

    private void postorder(CategoryNode node, List<CategoryNode> list) {
        if (node == null) return;
        postorder(node.left, list);
        postorder(node.right, list);
        list.add(node);
    }

    /**
     * Bottom-up aggregation: aggregates child counts and losses into parent nodes.
     */
    public void aggregateBottomUp(CategoryNode node) {
        if (node == null || node.isLeaf()) return;

        if (node.left != null) aggregateBottomUp(node.left);
        if (node.right != null) aggregateBottomUp(node.right);

        int childCount = (node.left != null ? node.left.complaintCount : 0) +
                         (node.right != null ? node.right.complaintCount : 0);
        double childLoss = (node.left != null ? node.left.totalFinancialLoss : 0.0) +
                          (node.right != null ? node.right.totalFinancialLoss : 0.0);

        node.complaintCount += childCount;
        node.totalFinancialLoss += childLoss;
    }

    /**
     * Pretty-prints the hierarchical tree structure with indentation.
     */
    public void printTreeStructure() {
        System.out.println("\n=== Cybercrime Classification Binary Tree ===");
        printTreeRecursive(root, "", true);
        System.out.println("================================================");
    }

    private void printTreeRecursive(CategoryNode node, String prefix, boolean isTail) {
        if (node != null) {
            System.out.println(prefix + (isTail ? "└── " : "├── ") + node.name + " (" + node.code + ")" +
                    " [Cases: " + node.complaintCount + ", Loss: Rs. " + String.format("%,.0f", node.totalFinancialLoss) + "]");
            List<CategoryNode> children = new ArrayList<>();
            if (node.left != null) children.add(node.left);
            if (node.right != null) children.add(node.right);

            for (int i = 0; i < children.size(); i++) {
                boolean lastChild = (i == children.size() - 1);
                printTreeRecursive(children.get(i), prefix + (isTail ? "    " : "│   "), lastChild);
            }
        }
    }
}
