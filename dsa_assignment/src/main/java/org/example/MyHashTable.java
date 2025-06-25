package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyHashTable<K, V> {
    private static final Logger LOG = LogManager.getLogger(MyHashTable.class);
    private class Entry{
        private final int hash;
        private final K key;
        private V value;
        private Entry next;

        Entry(int hash, K key, V value, Entry next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Entry[] table;
    private int size;
    private int capacity;
    private static final float LOAD_FACTOR = 0.75f;
    private static final float SHRINK_FACTOR = 0.3f;

    public MyHashTable(int capacity) {
        this.capacity = capacity;
        this.table = new MyHashTable.Entry[capacity];
    }

    public MyHashTable() {
        this(10);
    }

    private int hash(K key) {
        return (key == null) ? 0 : Math.abs(key.hashCode() % capacity);
    }

    public V put(K key, V value) {
        resizeIfNeeded();
        int hash = hash(key);
        Entry current = table[hash];

        for (Entry e = current; e != null; e = e.next) {
            if ((key == null && e.key == null) || (key != null && key.equals(e.key))) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }

        table[hash] = new Entry(hash, key, value, current);
        size++;
        return null;
    }

    public V get(K key) {
        int hash = hash(key);
        for (Entry e = table[hash]; e != null; e = e.next) {
            if ((key == null && e.key == null) || (key != null && key.equals(e.key))) {
                return e.value;
            }
        }
        return null;
    }

    public V remove(K key) {
        int hash = hash(key);
        Entry current = table[hash];
        Entry prev = null;

        while (current != null) {
            if ((key == null && current.key == null) || (key != null && key.equals(current.key))) {
                if (prev == null) {
                    table[hash] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                resizeIfNeeded();
                return current.value;
            }
            prev = current;
            current = current.next;
        }
        return null;
    }

    public boolean containsKey(K key) {
        int hash = hash(key);
        for (Entry e = table[hash]; e != null; e = e.next) {
            if ((key == null && e.key == null) || (key != null && key.equals(e.key))) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        table = (Entry[]) new MyHashTable<?, ?>.Entry[capacity];
        size = 0;
    }

    private void resizeIfNeeded() {
        float load = (float) size / capacity;

        if (load > LOAD_FACTOR) {
            resize(capacity * 2);
        } else if (load < SHRINK_FACTOR && capacity > 10) {
            resize(Math.max(10, capacity / 2));
        }
    }

    private void resize(int newCapacity) {
        Entry[] oldTable = table;
        table = (Entry[]) new MyHashTable<?, ?>.Entry[newCapacity];
        int oldCapacity = capacity;
        capacity = newCapacity;
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            for (Entry e = oldTable[i]; e != null; e = e.next) {
                put(e.key, e.value);
            }
        }
    }

    public void printTable() {
        LOG.info("---- Hash Table Visualization ----");
        for (int i = 0; i < capacity; i++) {
            Entry entry = table[i];
            if (entry == null) {
                LOG.info("Bucket {}: empty", i);
            } else {
                StringBuilder bucketContents = new StringBuilder();
                while (entry != null) {
                    bucketContents.append("[Key=").append(entry.key)
                            .append(", Value=").append(entry.value)
                            .append("] -> ");
                    entry = entry.next;
                }
                bucketContents.append("null");
                LOG.info("Bucket {}: {}", i, bucketContents);
            }
        }
        LOG.info("---- End of Table ----");
    }


    public static void main(String[] args) {
        MyHashTable<String, String> htable = new MyHashTable<>();
        htable.put("1", "ONE");
        htable.put("2", "TWO");
        htable.put("3", "THREE");

        LOG.info("Get '1': {}", htable.get("1"));
        LOG.info("Contains '2': {}", htable.containsKey("2"));
        LOG.info("Size: {}", htable.size());

        htable.printTable();

        htable.remove("2");
        LOG.info("After removing '2', contains? {}", htable.containsKey("2"));
        LOG.info("Size after removal: {}", htable.size());

        htable.clear();
        LOG.info("Is empty after clear? {}", htable.isEmpty());
    }
}
