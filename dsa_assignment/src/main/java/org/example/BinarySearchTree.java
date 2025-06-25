package org.example;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BinarySearchTree<T extends Number & Comparable<T>> {

    private static final Logger LOG = LogManager.getLogger(BinarySearchTree.class);

    private static class Node<T extends Number & Comparable<T>> {
        private T data;
        private Node<T> left, right;

        Node(T data) {
            this.data = data;
        }
    }

    private Node<T> root;

    public BinarySearchTree() {
        this.root = null;
    }

    public void insert(T data) {
        if (root == null) root = new Node<>(data);

        Node<T> current = root;
        Node<T> parent = null;

        while (current != null) {
            parent = current;
            if (data.compareTo(current.data) < 0) {
                current = current.left;
            } else if (data.compareTo(current.data) > 0) {
                current = current.right;
            } else {
                return;
            }
        }

        if (data.compareTo(parent.data) < 0) parent.left = new Node<>(data);
        else if (data.compareTo(parent.data) > 0) parent.right = new Node<>(data);
    }

    public void insert(List<T> arr){
        for(T item: arr) insert(item);
    }
    public void inorder() {
        Node<T> current = root;

        while (current != null) {
            if (current.left == null) {
                LOG.info(current.data);
                current = current.right;
            } else {
                Node<T> predecessor = current.left;
                while (predecessor.right != null && predecessor.right != current) {
                    predecessor = predecessor.right;
                }
                if (predecessor.right == null) {
                    predecessor.right = current;
                    current = current.left;
                }
                else {
                    predecessor.right = null;
                    LOG.info(current.data);
                    current = current.right;
                }
            }
        }
    }

    public boolean exists(T value) {
        Node<T> node = root;
        while (node != null && !node.data.equals(value)) {
            node = value.compareTo(node.data) < 0 ? node.left : node.right;
        }
        return node != null;
    }

    public T findMin() {
        return findMin(root);
    }

    private T findMin(Node<T> root) {
        if (root == null) return null;

        Node<T> current = root;
        while (current.left != null) {
            current = current.left;
        }
        return current.data;
    }

    public T findMax() {
        if (root == null) return null;

        Node<T> current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.data;
    }

    public int height() {
        return heightRecursive(root);
    }

    private int heightRecursive(Node<T> root) {
        if (root == null) return -1;
        return Math.max(heightRecursive(root.left), heightRecursive(root.right)) + 1;
    }

    public void delete(T value) {
        root = deleteRecursive(root, value);
    }

    private Node<T> deleteRecursive(Node<T> root, T value) {
        if (root == null) return null;

        int cmp = value.compareTo(root.data);
        if (cmp < 0) {
            root.left = deleteRecursive(root.left, value);
        } else if (cmp > 0) {
            root.right = deleteRecursive(root.right, value);
        } else {
            if (root.left == null) return root.right;
            if (root.right == null) return root.left;

            T min = findMin(root.right);
            root.data = min;
            root.right = deleteRecursive(root.right, min);
        }
        return root;
    }

    private void storeInorder(Node<T> node, List<T> list) {
        if (node != null) {
            storeInorder(node.left, list);
            list.add(node.data);
            storeInorder(node.right, list);
        }
    }

    public void rebalance() {
        List<T> sortedList = new ArrayList<>();
        storeInorder(root, sortedList);
        root = buildBalancedTree(sortedList, 0, sortedList.size() - 1);

    }

    private Node<T> buildBalancedTree(List<T> list, int start, int end) {
        if (start > end) return null;

        int mid = (start + end) / 2;
        Node<T> node = new Node<>(list.get(mid));
        node.left = buildBalancedTree(list, start, mid - 1);
        node.right = buildBalancedTree(list, mid + 1, end);
        return node;
    }

    public List<T> getInorderList() {
        List<T> result = new ArrayList<>();
        storeInorder(root, result);
        return result;
    }

    public void displayTree() {
        bfs(root);
    }

    private void bfs(Node<T> root) {
        if (root == null) return;

        Queue<Node<T>> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                Node<T> node = queue.poll();
                LOG.info("{} ", node.data);

                if (node.left != null) queue.add(node.left);
                if (node.right != null) queue.add(node.right);
            }
        }
    }
}
