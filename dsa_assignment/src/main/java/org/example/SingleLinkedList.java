package org.example;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SingleLinkedList<T extends Comparable<T>> {

    private static final Logger LOG = LogManager.getLogger(SingleLinkedList.class);

    private class Node {
        private final T item;
        private Node next;

        Node(T item, Node next) {
            this.item = item;
            this.next = next;
        }
    }


    int size;
    Node first;
    Node last;

    public SingleLinkedList() {
        this.size = 0;
        this.first = null;
        this.last = null;
    }

    public T getFirst() {
        if(first == null) return null;
        return first.item;
    }

    public T getLast() {
        if(last == null) return null;
        return last.item;
    }

    public void addFirst(T e) {
        Node temp = new Node(e, first);
        if(first == null) {
            first = temp;
            last = temp;
        }
        first = temp;
        size++;
    }

    public void addLast(T e) {
        if (first == null) {
            Node temp = new Node(e, null);
            first = temp;
            last = temp;
        } else {
            last.next = new Node(e, null);
            last = last.next;
        }
        size++;
    }

    public boolean add(T e) {
        addLast(e);
        return true;
    }

    public T removeFirst() {
        if (first == null) return null;
        T item = first.item;
        first = first.next;
        if (first == null) {
            last = null;
        }
        size--;
        return item;
    }

    public T removeLast() {
        if (first == null) return null;

        if (first.next == null) {
            T item = first.item;
            first = null;
            last = null;
            size--;
            return item;
        }

        Node current = first;

        while (current.next.next != null) {
            current = current.next;
        }

        T item = current.next.item;
        current.next = null;
        last = current;
        size--;
        return item;
    }

    public void printList() {
        if (first == null) LOG.info("List is empty!!!");
        Node head = first;
        LOG.info("Linked list - ");
        while (head != null) {
            LOG.info(head.item);
            head = head.next;
        }
    }

    public int size() {
        return size;
    }

    public void reverse() {
        if (first == null || first.next == null) return;

        Node prev = null;
        Node current = first;
        Node oldFirst = first;

        while (current != null) {
            Node temp = current.next;
            current.next = prev;
            prev = current;
            current = temp;
        }
        first = prev;
        last = oldFirst;
    }

    public boolean contains(T e) {
        Node head = first;

        while (head != null) {
            if (head.item == e) {
                return true;
            }
            head = head.next;
        }

        return false;
    }

    public void clear() {
        first = null;
        last = null;
        size = 0;
    }

    public boolean cycle() {
        if (first == null) return false;

        Node slow = first;
        Node fast = first;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;

            if (slow == fast) return true;
        }

        return false;
    }

    void makeCycle() {
        if (first != null && last != null) {
            last.next = first;
        }
    }

    void breakCycle() {
        if (last != null) {
            last.next = null;
        }
    }

    public SingleLinkedList<T> merge(SingleLinkedList<T> a, SingleLinkedList<T> b) {
        if (a.getFirst() == null && b.getFirst() == null) return new SingleLinkedList<>();
        if (a.getFirst() == null) return b;
        if (b.getFirst() == null) return a;

        Node x = a.first;
        Node y = b.first;
        SingleLinkedList<T> result = new SingleLinkedList<>();

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

    public static void main(String[] args) {

        SingleLinkedList<String> list = new SingleLinkedList<>();

        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");
        list.removeFirst();
        LOG.info(list.size());
        list.printList();
        list.reverse();
        list.printList();
        LOG.info(list.contains("two"));
        LOG.info(list.cycle());

        SingleLinkedList<Integer> first = new SingleLinkedList<>();
        first.add(1);
        first.add(3);
        first.add(5);

        SingleLinkedList<Integer> second = new SingleLinkedList<>();
        second.add(2);
        second.add(4);
        second.add(6);

        SingleLinkedList<Integer> merged = first.merge(first, second);
        LOG.info("Merged list:");
        merged.printList();

        List<Integer> l = new LinkedList<>();
        l.add(1);
        l.add(2);
        l.add(3);
        for(Integer i: l){
            LOG.info(i);
        }
    }
}