package ccrrs.ds.linear;

import ccrrs.model.Complaint;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 1: Custom Singly Linked List for storing and managing complaint records.
 * Supports O(1) tail insertion, O(n) deletion, and traversal for reporting.
 */
public class SinglyLinkedList {

    public static class Node {
        public Complaint data;
        public Node next;

        public Node(Complaint data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    public SinglyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Inserts a new complaint at the tail of the linked list.
     * Time Complexity: O(1)
     */
    public void insertTail(Complaint complaint) {
        Node newNode = new Node(complaint);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Inserts a new complaint at the head of the linked list.
     * Time Complexity: O(1)
     */
    public void insertHead(Complaint complaint) {
        Node newNode = new Node(complaint);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head = newNode;
        }
        size++;
    }

    /**
     * Deletes a complaint matching the given case ID.
     * Time Complexity: O(n)
     * @return The deleted Complaint, or null if not found.
     */
    public Complaint deleteByCaseId(String caseId) {
        if (head == null) return null;

        if (head.data.getCaseId().equalsIgnoreCase(caseId)) {
            Complaint deleted = head.data;
            head = head.next;
            if (head == null) {
                tail = null;
            }
            size--;
            return deleted;
        }

        Node curr = head;
        while (curr.next != null && !curr.next.data.getCaseId().equalsIgnoreCase(caseId)) {
            curr = curr.next;
        }

        if (curr.next != null) {
            Complaint deleted = curr.next.data;
            if (curr.next == tail) {
                tail = curr;
            }
            curr.next = curr.next.next;
            size--;
            return deleted;
        }

        return null;
    }

    /**
     * Searches for a complaint by Case ID linearly in the linked list.
     * Time Complexity: O(n)
     */
    public Complaint findByCaseId(String caseId) {
        Node curr = head;
        while (curr != null) {
            if (curr.data.getCaseId().equalsIgnoreCase(caseId)) {
                return curr.data;
            }
            curr = curr.next;
        }
        return null;
    }

    /**
     * Traverses and returns all complaints as a List.
     */
    public List<Complaint> toList() {
        List<Complaint> list = new ArrayList<>(size);
        Node curr = head;
        while (curr != null) {
            list.add(curr.data);
            curr = curr.next;
        }
        return list;
    }

    /**
     * Traverses and displays all complaints in the register.
     */
    public void traverse() {
        if (head == null) {
            System.out.println("  [!] The complaint register is empty.");
            return;
        }
        System.out.println("==========================================================================================");
        System.out.printf("%-12s | %-16s | %-20s | %-6s | %-18s | %s%n",
                "Case ID", "Complainant", "Category", "Pri", "Status", "Loss (INR)");
        System.out.println("------------------------------------------------------------------------------------------");
        Node curr = head;
        while (curr != null) {
            Complaint c = curr.data;
            System.out.printf("%-12s | %-16s | %-20s | %6.1f | %-18s | Rs. %,.0f%n",
                    c.getCaseId(),
                    c.getComplainantName(),
                    c.getCategory(),
                    c.getPriorityScore(),
                    c.getStatus(),
                    c.getFinancialLoss());
            curr = curr.next;
        }
        System.out.println("==========================================================================================");
        System.out.println("  Total Active Records: " + size);
    }

    public Node getHead() {
        return head;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
