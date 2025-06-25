package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleLinkedListTest {

    private SingleLinkedList<String> list;

    @BeforeEach
    void setUp() {
        list = new SingleLinkedList<>();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");
    }


    @Test
    void testGetFirst() {
        assertEquals("one", list.getFirst());
    }

    @Test
    void testGetFirstEmptyList() {
        list.clear();
        assertNull(list.getFirst());
    }

    @Test
    void testGetLast() {
        assertEquals("five", list.getLast());
    }

    @Test
    void testGetLastEmptyList() {
        list.clear();
        assertNull(list.getLast());
    }

    @Test
    void testAddFirstOnEmptyList() {
        list.clear();
        list.addFirst("start");
        assertEquals(1, list.size());
        assertEquals("start", list.getFirst());
        assertEquals("start", list.getLast());
    }

    @Test
    void testAddFirstOnNonEmptyList() {
        list.addFirst("start");
        assertEquals(6, list.size());
        assertEquals("start", list.getFirst());
    }

    @Test
    void testAddLastOnEmptyList() {
        list.clear();
        list.addLast("end");
        assertEquals(1, list.size());
        assertEquals("end", list.getFirst());
        assertEquals("end", list.getLast());
    }

    @Test
    void testAddLastOnNonEmptyList() {
        list.addLast("end");
        assertEquals(6, list.size());
        assertEquals("end", list.getLast());
    }

    @Test
    void testAdd() {
        assertTrue(list.add("six"));
        assertEquals(6, list.size());
        assertEquals("six", list.getLast());
    }

    @Test
    void testRemoveFirst() {
        String removed = list.removeFirst();
        assertEquals("two", list.getFirst());
        assertEquals(4, list.size());
        assertEquals("one", removed);
    }

    @Test
    void testRemoveFirstUntilEmpty() {

        list.removeFirst();
        list.removeFirst();
        list.removeFirst();
        list.removeFirst();
        list.removeFirst();
        assertEquals(0, list.size());
        assertNull(list.getFirst());
        assertNull(list.getLast());
        assertNull(list.removeFirst());
    }

    @Test
    void testRemoveLast() {
        String removed = list.removeLast();
        assertEquals("five", removed);
        assertEquals("four", list.getLast());
        assertEquals(4, list.size());
    }

    @Test
    void testRemoveLastSingleElement() {
        list.clear();
        list.add("only");
        String removed = list.removeLast();
        assertEquals("only", removed);
        assertEquals(0, list.size());
        assertNull(list.getFirst());
        assertNull(list.getLast());
    }

    @Test
    void testRemoveLastOnEmptyList() {
        list.clear();
        assertNull(list.removeLast());
    }

    @Test
    void testReverse() {
        list.reverse();
        assertEquals("five", list.getFirst());
        assertEquals("one", list.getLast());
        assertEquals(5, list.size());
    }

    @Test
    void testReverseEmptyList() {
        list.clear();
        list.reverse();
        assertEquals(0, list.size());
        assertNull(list.getFirst());
    }

    @Test
    void testReverseSingleElementList() {
        list.clear();
        list.add("only");
        list.reverse();
        assertEquals("only", list.getFirst());
        assertEquals("only", list.getLast());
    }

    @Test
    void testContainsTrue() {
        assertTrue(list.contains("three"));
    }

    @Test
    void testContainsFalse() {
        assertFalse(list.contains("ten"));
    }

    @Test
    void testContainsNull() {
        list.add(null);
        assertTrue(list.contains(null));
    }

    @Test
    void testContainsOnEmptyList() {
        list.clear();
        assertFalse(list.contains("one"));
    }

    @Test
    void testClear() {
        list.clear();
        assertEquals(0, list.size());
        assertNull(list.getFirst());
        assertNull(list.getLast());
    }

    @Test
    void testClearTwice() {
        list.clear();
        list.clear();
        assertEquals(0, list.size());
    }

    @Test
    void testCycleDetectionFalse() {
        assertFalse(list.cycle());
    }

    @Test
    void testCycleDetectionTrue() throws Exception {
        list.makeCycle();
        assertTrue(list.cycle());
        list.breakCycle();
        assertFalse(list.cycle());
    }


    @Test
    void testMergeBothEmpty() {
        SingleLinkedList<String> a = new SingleLinkedList<>();
        SingleLinkedList<String> b = new SingleLinkedList<>();
        SingleLinkedList<String> merged = list.merge(a, b);
        assertEquals(0, merged.size());
    }

    @Test
    void testMergeFirstEmpty() {
        SingleLinkedList<String> a = new SingleLinkedList<>();
        SingleLinkedList<String> b = new SingleLinkedList<>();
        b.add("a");
        b.add("b");
        SingleLinkedList<String> merged = list.merge(a, b);
        assertEquals(2, merged.size());
        assertEquals("a", merged.getFirst());
        assertEquals("b", merged.getLast());
    }

    @Test
    void testMergeSecondEmpty() {
        SingleLinkedList<String> a = new SingleLinkedList<>();
        a.add("a");
        a.add("b");
        SingleLinkedList<String> b = new SingleLinkedList<>();
        SingleLinkedList<String> merged = list.merge(a, b);
        assertEquals(2, merged.size());
        assertEquals("a", merged.getFirst());
        assertEquals("b", merged.getLast());
    }
}
