package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyHashTableTest {

    private MyHashTable<String, String> htable;

    @BeforeEach
    void setup() {
        htable = new MyHashTable<>();
    }
    @Test
    void testPutAndGet() {
        assertNull(htable.put("1", "ONE"));
        assertEquals("ONE", htable.get("1"));

        assertEquals("ONE", htable.put("1", "NEW"));
        assertEquals("NEW", htable.get("1"));
    }

    @Test
    void testRemove() {
        htable.put("A", "Apple");
        htable.put("B", "Banana");

        assertEquals("Banana", htable.remove("B"));
        assertNull(htable.get("B"));
        assertEquals(1, htable.size());

        assertNull(htable.remove("C"));
        assertEquals(1, htable.size());

    }

    @Test
    void testContainsKey() {
        htable.put("A", "Apple");
        assertTrue(htable.containsKey("A"));
        assertFalse(htable.containsKey("Y"));

        htable.put(null, "NullKey");
        assertTrue(htable.containsKey(null));
    }

    @Test
    void testSizeAndIsEmpty() {
        assertTrue(htable.isEmpty());
        assertEquals(0, htable.size());

        htable.put("1", "ONE");
        assertFalse(htable.isEmpty());
        assertEquals(1, htable.size());

        htable.remove("1");
        assertTrue(htable.isEmpty());
    }

    @Test
    void testClear() {
        htable.put("A", "Apple");
        htable.put("B", "Banana");

        htable.clear();
        assertTrue(htable.isEmpty());
        assertNull(htable.get("A"));
        assertNull(htable.get("B"));
    }

    @Test
    void testCollisionHandling() {

        String key1 = "FB";
        String key2 = "Ea";

        htable.put(key1, "First");
        htable.put(key2, "Second");

        assertEquals("First", htable.get(key1));
        assertEquals("Second", htable.get(key2));

        htable.remove(key1);
        assertNull(htable.get(key1));
        assertEquals("Second", htable.get(key2));
    }

    @Test
    void testResizeUp() {
        for (int i = 0; i < 20; i++) {
            htable.put("key" + i, "val" + i);
        }

        assertEquals(20, htable.size());

        for (int i = 0; i < 20; i++) {
            assertEquals("val" + i, htable.get("key" + i));
        }
    }

    @Test
    void testResizeDown() {
        for (int i = 0; i < 20; i++) {
            htable.put("key" + i, "val" + i);
        }

        for (int i = 0; i < 17; i++) {
            htable.remove("key" + i);
        }

        assertEquals(3, htable.size());

        for (int i = 17; i < 20; i++) {
            assertEquals("val" + i, htable.get("key" + i));
        }
    }

    @Test
    void testPutDuplicateValue() {
        htable.put("1", "Value");
        htable.put("1", "Value");

        assertEquals(1, htable.size());
        assertEquals("Value", htable.get("1"));
    }

    @Test
    void testNullValues() {
        htable.put("key", null);
        assertTrue(htable.containsKey("key"));
        assertNull(htable.get("key"));
    }

}
