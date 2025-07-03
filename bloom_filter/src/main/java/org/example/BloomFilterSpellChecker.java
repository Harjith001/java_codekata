package org.example;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.BitSet;

public class BloomFilterSpellChecker implements SpellChecker {
    private final BitSet bitSet;
    private final int bitSetSize;
    private final int numHashFunctions;
    private final MessageDigest md5;
    private int wordCount;

    public BloomFilterSpellChecker(int bitSetSize, int numHashFunctions) throws Exception {
        this.bitSetSize = bitSetSize;
        this.numHashFunctions = numHashFunctions;
        this.bitSet = new BitSet(bitSetSize);
        this.md5 = MessageDigest.getInstance("MD5");
        this.wordCount = 0;
    }

    @Override
    public void loadDictionary(InputStream dictionaryFilePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dictionaryFilePath))) {
            String word;
            while ((word = reader.readLine()) != null) {
                addWord(word.trim().toLowerCase());
                wordCount++;
            }
        }
    }

    private void addWord(String word) {
        int[] hashes = getHashes(word);
        for (int hash : hashes) {
            bitSet.set(Math.abs(hash % bitSetSize));
        }
    }

    @Override
    public boolean isWordCorrect(String word) {
        int[] hashes = getHashes(word.toLowerCase());
        for (int hash : hashes) {
            if (!bitSet.get(Math.abs(hash % bitSetSize))) {
                return false;
            }
        }
        return true;
    }

    private int[] getHashes(String word) {
        byte[] digest = md5.digest(word.getBytes());
        int[] hashes = new int[numHashFunctions];
        for (int i = 0; i < numHashFunctions; i++) {
            int hash = 0;
            for (int j = 0; j < 4; j++) {
                hash <<= 8;
                //System.out.println(digest[(i * 4 + j) % digest.length]);
                hash |= (digest[(i * 4 + j) % digest.length] & 0xFF);
            }
            hashes[i] = hash;
        }
        return hashes;
    }

    public int getWordCount() {
        return wordCount;
    }
}

