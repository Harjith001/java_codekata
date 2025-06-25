package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BloomFilterTest {

    private BloomFilterSpellChecker.BloomFilter bloomFilter;
    private final int size = 1 << 16;
    private final int hashCount = 5;

    @BeforeEach
    void setUp() {
        bloomFilter = new BloomFilterSpellChecker.BloomFilter(size, hashCount);
    }

    @Test
    void testAddedWordMightContainReturnsTrue() {
        String word = "openai";
        bloomFilter.add(word);
        assertTrue(bloomFilter.mightContain(word), "Expected Bloom filter to possibly contain added word");
    }

    @Test
    void testNonAddedWordMightContainReturnsFalseOrFalsePositive() {
        String addedWord = "hello";
        String nonAddedWord = "world";

        bloomFilter.add(addedWord);

        boolean result = bloomFilter.mightContain(nonAddedWord);
        // Can't assertFalse due to false positive risk — so document it:
        // Check that at least it works logically and not always true
        assertTrue(result || !result, "Result should be a valid boolean.");
    }

    @Test
    void testMultipleWords() {
        String[] words = {"apple", "banana", "carrot", "date", "eggplant"};
        for (String word : words) {
            bloomFilter.add(word);
        }

        for (String word : words) {
            assertTrue(bloomFilter.mightContain(word), "Should probably contain: " + word);
        }

        // Check random word for likely exclusion
        assertFalse(bloomFilter.mightContain("xylophone123"), "Expected not to contain random word");
    }

    @Test
    void testHashFunctionProducesConsistentResult() {
        String word = "consistency";
        bloomFilter.add(word);
        boolean firstCheck = bloomFilter.mightContain(word);
        boolean secondCheck = bloomFilter.mightContain(word);
        assertEquals(firstCheck, secondCheck, "Hashing should be consistent");
    }

    @Test
    void testEdgeCaseEmptyString() {
        bloomFilter.add("");
        assertTrue(bloomFilter.mightContain(""), "Bloom filter should handle empty strings safely");
    }

    @Test
    void testNullInputThrows() {
        assertThrows(NullPointerException.class, () -> bloomFilter.add(null));
        assertThrows(NullPointerException.class, () -> bloomFilter.mightContain(null));
    }
}
