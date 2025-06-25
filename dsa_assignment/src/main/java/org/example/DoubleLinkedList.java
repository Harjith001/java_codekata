package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class DoubleLinkedList<T extends Comparable<T>> {

    private static final Logger LOG = LogManager.getLogger(DoubleLinkedList.class);

    private class Node {
        private final T item;
        private Node next;
        private Node prev;

        Node(T item, Node next, Node prev) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }
    }

    int size;
    Node first;
    Node last;

    public DoubleLinkedList() {
        this.size = 0;
        this.first = null;

        this.last = null;
    }
    public T getFirst() {
        return (first == null) ? null : first.item;
    }

    public T getLast() {
        return (last == null) ? null : last.item;
    }

    public void addFirst(T e) {
        Node newNode = new Node(e, first, null);
        if (first != null) {
            first.prev = newNode;
        } else {
            last = newNode;
        }
        first = newNode;
        size++;
    }

    public void addLast(T e) {
        Node newNode = new Node(e, null, last);
        if (last != null) {
            last.next = newNode;
        } else {
            first = newNode;
        }
        last = newNode;
        size++;
    }

    public void add(List<T> items) {
        for(T item: items) addLast(item);
    }

    public boolean add(T e, int pos) {
        if (pos < 0 || pos > size) {
            return false;
        }
        if (pos == 0) {
            addFirst(e);
        } else if (pos == size) {
            addLast(e);
        } else {
            Node curr = first;
            for (int i = 0; i < pos; i++) {
                curr = curr.next;
            }
            Node newNode = new Node(e, curr, curr.prev);
            if (curr.prev != null) {
                curr.prev.next = newNode;
            }
            curr.prev = newNode;
            size++;
        }
        return true;
    }


    public T removeFirst() {
        if (first == null) return null;
        T item = first.item;
        first = first.next;
        if (first != null) {
            first.prev = null;
        } else {
            last = null;
        }
        size--;
        return item;
    }

    public T removeLast() {
        if (last == null) return null;
        T item = last.item;
        last = last.prev;
        if (last != null) {
            last.next = null;
        } else {
            first = null;
        }
        size--;
        return item;
    }

    public void printList() {
        if (first == null) {
            LOG.info("List is empty!!!");
            return;
        }
        Node current = first;
        LOG.info("Doubly Linked list:");
        while (current != null) {
            LOG.info(current.item);
            current = current.next;
        }
    }

    public int size() {
        return size;
    }

    public void reverse() {
        Node current = first;
        Node temp = null;
        while (current != null) {
            temp = current.prev;
            current.prev = current.next;
            current.next = temp;
            current = current.prev;
        }
        if (temp != null) {
            first = temp.prev;
        }
        Node swap = first;
        first = last;
        last = swap;
    }

    public boolean contains(T e) {
        Node current = first;
        while (current != null) {
            if (current.item.equals(e)) {
                return true;
            }
            current = current.next;
        }
        throw new NoSuchElementException("No such element as " + e);
    }

    public void clear() {
        first = null;
        last = null;
        size = 0;
    }

    public boolean cycle() {
        Node slow = first;
        Node fast = first;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;

            if (slow == fast) return true;
        }
        return false;
    }

    public DoubleLinkedList<T> merge(DoubleLinkedList<T> a, DoubleLinkedList<T> b) {
        if (a.getFirst() == null && b.getFirst() == null) return new DoubleLinkedList<>();
        if (a.getFirst() == null) return b;
        if (b.getFirst() == null) return a;

        Node x = a.first;
        Node y = b.first;
        DoubleLinkedList<T> result = new DoubleLinkedList<>();

        while (x != null && y != null) {
            if (x.item.compareTo(y.item) <= 0) {
                result.addLast(x.item);
                x = x.next;
            } else {
                result.addLast(y.item);
                y = y.next;
            }
        }

        while (x != null) {
            result.addLast(x.item);
            x = x.next;
        }

        while (y != null) {
            result.addLast(y.item);
            y = y.next;
        }

        return result;
    }

    public static class NoSuchElementException extends RuntimeException {
        public NoSuchElementException() {
            super();
        }

        public NoSuchElementException(String s) {
            super(s);
        }
    }

    public static void main(String[] args) {
        DoubleLinkedList<String> list = new DoubleLinkedList<>();

        list.add(List.of("one","two","three","four","five"));
        list.removeFirst();
        LOG.info("Size: " + list.size());
        list.printList();

        list.reverse();
        LOG.info("After reverse:");
        list.printList();

        LOG.info("Contains 'two': " + list.contains("two"));
        LOG.info("Cycle: " + list.cycle());

        DoubleLinkedList<Integer> first = new DoubleLinkedList<>();
        first.add(List.of(1,3,5));

        DoubleLinkedList<Integer> second = new DoubleLinkedList<>();

        second.add(List.of(2,4,6));
        DoubleLinkedList<Integer> merged = first.merge(first, second);
        LOG.info("Merged list:");
        merged.printList();
    }
}
