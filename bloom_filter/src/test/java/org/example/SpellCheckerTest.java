package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.GraphLayout;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class SpellCheckerTest {
    private static BloomFilterSpellChecker checker;
    private static final int bitSetSize = 1_100_000;
    private static final int numHashFunctions = 6;
    private static final String DICTIONARY_PATH = "/usr/share/dict/words";

    @BeforeAll
    public static void setUp() throws Exception {
        checker = new BloomFilterSpellChecker(bitSetSize, numHashFunctions);
        checker.loadDictionary(DICTIONARY_PATH);
        System.out.println("Words loaded: " + checker.getWordCount());
    }

    @Test
    public void testKnownWords() {
        String[] testWords = {"apple", "banana", "cat", "quantum", "xylophone"};
        for (String word : testWords) {
            assertTrue(checker.isWordCorrect(word),
                    String.format("Expected '%s' to be probably correct", word));
        }
    }

    @Test
    public void testUnknownWordShouldBeIncorrect() {
        String nonsenseWord = "qzxyv";
        assertFalse(checker.isWordCorrect(nonsenseWord),
                "Expected unknown word to be misspelled");
    }

    @Test
    public void testFalsePositiveRate() {
        int trials = 10_000;
        int falsePositives = 0;

        for (int i = 0; i < trials; i++) {
            String randomWord = generateRandomWord(5);
            if (checker.isWordCorrect(randomWord)) {
                falsePositives++;
            }
        }

        double falsePositiveRate = (falsePositives * 100.0) / trials;
        System.out.printf("False positives: %d/%d (%.4f%%)\n", falsePositives, trials, falsePositiveRate);

        assertTrue(falsePositiveRate <= 1.0,
                "False positive rate exceeded acceptable threshold: " + falsePositiveRate + "%");

        System.out.println(GraphLayout.parseInstance(checker).toFootprint());
    }

    private String generateRandomWord(int length) {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + rand.nextInt(26)));
        }
        return sb.toString();
    }
}
