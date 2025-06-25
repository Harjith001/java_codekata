package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTreeTest {

    private BinarySearchTree<Integer> bst;

    @BeforeEach
    void setup() {
        bst = new BinarySearchTree<>();
    }


    @Test
    void testInsertIntoEmptyTree() {
        bst.insert(10);
        assertEquals(List.of(10), bst.getInorderList());
    }

    @Test
    void testInsertOnlyOneElementMultipleTimes() {
        bst.insert(List.of(5, 5, 5, 5, 5, 5, 5));
        assertEquals(List.of(5), bst.getInorderList(), "Duplicate inserts should be ignored");
    }

    @Test
    void testFindInEmptyTree() {
        assertFalse(bst.exists(10), "Should return null for find on empty tree");
    }

    @Test
    void testDeleteNonExistingNode() {
        bst.insert(List.of(10, 20));
        bst.delete(30);
        assertEquals(List.of(10, 20), bst.getInorderList());
    }

    @Test
    void testMinMaxOnEmptyTree() {
        assertNull(bst.findMin());
        assertNull(bst.findMax());
    }

    @Test
    void inorderTest() {
        List<Integer> items = List.of(1, 3, 2, 5, 4);
        bst.insert(items);
        List<Integer> expected = List.of(1, 2, 3, 4, 5);

        assertEquals(expected, bst.getInorderList());
    }

    @Test
    void testHeightOfEmptyTree() {
        assertEquals(-1, bst.height(), "Height of an empty tree should be -1");
    }

    @Test
    void testDeleteRootNodeOnly() {
        bst.insert(10);
        bst.delete(10);
        assertEquals(List.of(), bst.getInorderList(), "Tree should be empty after deleting the root");
    }

    @Test
    void testDeleteRootWithOneChild() {
        bst.insert(10);
        bst.insert(5);
        bst.delete(10);
        assertEquals(List.of(5), bst.getInorderList());

        bst = new BinarySearchTree<>();
        bst.insert(10);
        bst.insert(15);
        bst.delete(10);
        assertEquals(List.of(15), bst.getInorderList());
    }

    @Test
    void testDeleteRootWithTwoChildren() {
        bst.insert(List.of(10, 5, 15, 10));
        assertEquals(3, bst.getInorderList().size());
        bst.delete(10);
        assertFalse(bst.getInorderList().contains(10));
    }

    @Test
    void testRebalanceEmptyTree() {
        assertDoesNotThrow(() -> bst.rebalance(), "Rebalancing empty tree shouldn't fail");
        assertEquals(List.of(), bst.getInorderList());
    }

    @Test
    void testRebalanceSingleNodeTree() {
        bst.insert(42);
        bst.rebalance();
        assertEquals(List.of(42), bst.getInorderList());
    }

    @Test
    void testFindMinMaxAfterDeletions() {
        for (int val : new int[]{20, 10, 30, 5, 15, 25, 35}) {
            bst.insert(val);
        }
        bst.delete(5);
        bst.delete(35);
        assertEquals(10, bst.findMin());
        assertEquals(30, bst.findMax());
    }

    @Test
    void testFindWithNegativeNumbers() {
        bst.insert(List.of(-10, -20, -5, 0, 5));
        assertTrue(bst.exists(-10));
        assertEquals(-20, bst.findMin());
        assertEquals(5, bst.findMax());
    }

    @Test
    void testDeleteAllNodes() {
        List<Integer> values = List.of(7, 3, 9, 1, 5, 8, 10);
        bst.insert(values);
        for (int val : values) {
            bst.delete(val);
        }
        assertEquals(List.of(), bst.getInorderList(), "All nodes should be deleted");
        assertNull(bst.findMin());
        assertNull(bst.findMax());
        assertEquals(-1, bst.height());
    }
}
