package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoubleLinkedListTest {


    DoubleLinkedList<Integer> list;

    @BeforeEach
    void setUp() {
        list = new DoubleLinkedList<>();
    }

    @Test
    void testAddFirstAndGetFirst() {
        assertNull(list.getFirst());
        list.addFirst(10);
        assertEquals(10, list.getFirst());
        list.addFirst(20);
        assertEquals(20, list.getFirst());
    }

    @Test
    void testAddLastAndGetLast() {
        assertNull(list.getLast());
        list.addLast(10);
        assertEquals(10, list.getLast());
        list.addLast(20);
        assertEquals(20, list.getLast());
    }

    @Test
    void testAddAtPosition() {
        list.clear();
        assertTrue(list.add(10, 0));
        assertEquals(1, list.size());
        assertTrue(list.add(5, 0)); // addLast at start
        assertTrue(list.add(15, 2)); // addLast at end
        assertTrue(list.add(12, 2)); // addLast in middle

        assertEquals(4, list.size());
    }

    @Test
    void testAddAtInvalidPosition() {
        assertFalse(list.add(1, -1));
        assertFalse(list.add(1, 1)); // list is empty, valid index is 0
    }

    @Test
    void testAdd() {
        assertEquals(1, list.size());
        assertEquals(1, list.getFirst());
        assertEquals(1, list.getLast());
    }

    @Test
    void testRemoveFirst() {
        assertNull(list.removeFirst());

        list.addLast(1);
        list.addLast(2);
        assertEquals(1, list.removeFirst());
        assertEquals(1, list.size());
    }

    @Test
    void testRemoveLast() {
        assertNull(list.removeLast());

        list.addLast(1);
        list.addLast(2);
        assertEquals(2, list.removeLast());
        assertEquals(1, list.size());
    }

    @Test
    void testReverseEmptyList() {
        list.reverse(); // should not throw
        assertEquals(0, list.size());
    }

    @Test
    void testReverseSingleElement() {
        list.addLast(1);
        list.reverse();
        assertEquals(1, list.getFirst());
    }

    @Test
    void testReverseMultipleElements() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        list.reverse();
        assertEquals(3, list.getFirst());
    }

    @Test
    void testContainsElementExists() {
        list.addLast(1);
        list.addLast(2);
        assertTrue(list.contains(2));
    }

    @Test
    void testContainsElementDoesNotExist() {
        list.addLast(1);
        list.addLast(2);
        Exception exception = assertThrows(DoubleLinkedList.NoSuchElementException.class, () -> list.contains(99));
        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    void testClear() {
        list.addLast(1);
        list.addLast(2);
        list.clear();
        assertEquals(0, list.size());
        assertNull(list.getFirst());
        assertNull(list.getLast());
    }

    @Test
    void testCycleDetectionInAcyclicList() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        assertFalse(list.cycle());
    }

    @Test
    void testMergeTwoEmptyLists() {
        DoubleLinkedList<Integer> a = new DoubleLinkedList<>();
        DoubleLinkedList<Integer> b = new DoubleLinkedList<>();

        DoubleLinkedList<Integer> merged = a.merge(a, b);
        assertEquals(0, merged.size());
    }

    @Test
    void testMergeOneEmptyList() {
        DoubleLinkedList<Integer> a = new DoubleLinkedList<>();
        a.addLast(1);
        a.addLast(3);

        DoubleLinkedList<Integer> b = new DoubleLinkedList<>();

        DoubleLinkedList<Integer> merged = a.merge(a, b);
        assertEquals(2, merged.size());
        assertEquals(1, merged.getFirst());
        assertEquals(3, merged.getLast());
    }
}
