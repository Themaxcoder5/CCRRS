package ccrrs.ds.linear;

/**
 * Custom singly linked list implementation of a LIFO Stack.
 * Used for:
 * 1. Tracking case-status history and undo operations (pop).
 * 2. Evaluating postfix priority score formulas.
 *
 * @param <T> The element type
 */
public class LinkedStack<T> {

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data, Node<T> next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> top;
    private int size;

    public LinkedStack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes an item onto the top of the stack.
     * Time Complexity: O(1)
     */
    public void push(T item) {
        top = new Node<>(item, top);
        size++;
    }

    /**
     * Removes and returns the top item.
     * Time Complexity: O(1)
     * @throws IllegalStateException if stack is empty
     */
    public T pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack Underflow: Cannot pop from an empty stack.");
        }
        T item = top.data;
        top = top.next;
        size--;
        return item;
    }

    /**
     * Returns the top item without removing it.
     * Time Complexity: O(1)
     */
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty.");
        }
        return top.data;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return size;
    }

    public void clear() {
        top = null;
        size = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[Top -> ");
        Node<T> curr = top;
        while (curr != null) {
            sb.append(curr.data);
            if (curr.next != null) sb.append(", ");
            curr = curr.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
