package org.example;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BloomFilterSpellChecker {

    static class BloomFilter {
        private final BitSet bitset;
        private final int size;
        private final int hashCount;

        public BloomFilter(int size, int hashCount) {
            this.size = size;
            this.hashCount = hashCount;
            this.bitset = new BitSet(size);
        }

        public void add(String word) {
            byte[] digest = getMD5(word);
            for (int i = 0; i < hashCount; i++) {
                int hash = getHash(digest, i);
                bitset.set(Math.abs(hash % size));
            }
        }

        public boolean mightContain(String word) {
            byte[] digest = getMD5(word);
            for (int i = 0; i < hashCount; i++) {
                int hash = getHash(digest, i);
                if (!bitset.get(Math.abs(hash % size))) {
                    return false;
                }
            }
            return true;
        }

        private byte[] getMD5(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                return md.digest(input.getBytes());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        private int getHash(byte[] digest, int seed) {
            int hash = 0;
            for (int i = 0; i < 4; i++) {
                hash <<= 8;
                hash |= ((int) digest[(i + seed) % digest.length]) & 0xFF;
            }
            return hash;
        }
    }

    public static void main(String[] args) throws IOException {
        int bitArraySize = 1 << 20;
        int numHashFunctions = 7;

        BloomFilter bloomFilter = new BloomFilter(bitArraySize, numHashFunctions);
        Set<String> actualDictionary = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader("/usr/share/dict/words"))) {
            String word;
            while ((word = br.readLine()) != null) {
                word = word.trim().toLowerCase();
                if (!word.isEmpty()) {
                    bloomFilter.add(word);
                    actualDictionary.add(word);
                }
            }
        }

        System.out.println("Dictionary loaded with " + actualDictionary.size() + " words.");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter word to check (or type 'exit'): ");
            String word = scanner.nextLine().trim().toLowerCase();
            if (word.equals("exit")) break;

            if (bloomFilter.mightContain(word)) {
                System.out.println("Bloom Filter: '" + word + "' might be in the dictionary.");
                if (actualDictionary.contains(word)) {
                    System.out.println("Confirmed: Word is in dictionary.");
                } else {
                    System.out.println("False Positive: Word NOT in dictionary.");
                }
            } else {
                System.out.println("Definitely not in dictionary.");
            }
        }

        testFalsePositives(bloomFilter, actualDictionary, 10000);
    }

    private static void testFalsePositives(BloomFilter bloomFilter, Set<String> actualDictionary, int testCount) {
        int falsePositives = 0;
        Random random = new Random();
        for (int i = 0; i < testCount; i++) {
            String word = randomWord(5, random);
            if (bloomFilter.mightContain(word) && !actualDictionary.contains(word)) {
                falsePositives++;
            }
        }
        System.out.printf("False positives after %d random tests: %d (%.2f%%)\n", testCount, falsePositives, 100.0 * falsePositives / testCount);
    }

    private static String randomWord(int length, Random random) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char ch = (char) ('a' + random.nextInt(26));
            sb.append(ch);
        }
        return sb.toString();
    }
}