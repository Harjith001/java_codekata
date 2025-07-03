package org.example;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.openjdk.jol.info.GraphLayout;

@SuppressWarnings("UnstableApiUsage")
public class GuavaBloomFilterSpellChecker implements SpellChecker{
    private static BloomFilter<CharSequence> bloomFilter = null;
    private int wordCount;

    public GuavaBloomFilterSpellChecker(int expectedInsertions, double fpp) {

        bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), expectedInsertions, fpp);
        wordCount = 0;
    }

    @Override
    public void loadDictionary(InputStream dictionaryFilePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dictionaryFilePath))) {
            String word;
            while ((word = reader.readLine()) != null) {
                word = word.trim().toLowerCase();
                bloomFilter.put(word);
                wordCount++;
            }
        }
    }

    @Override
    public boolean isWordCorrect(String word) {
        return bloomFilter.mightContain(word.toLowerCase());
    }

    public int getWordCount() {
        return wordCount;
    }


    public static void main(String[] args) throws Exception {
        int expectedWords = 110000;
        double falsePositiveProb = 0.01;

        GuavaBloomFilterSpellChecker checker = new GuavaBloomFilterSpellChecker(expectedWords, falsePositiveProb);
        checker.loadDictionary(new FileInputStream("/usr/share/dict/american-english"));

        System.out.println("Words loaded: " + checker.getWordCount());

        String[] testWords = {"apple", "banana", "cat", "quantum", "xylophone", "asdfgh"};
        for (String word : testWords) {
            System.out.printf("'%s' is %s\n", word, checker.isWordCorrect(word) ? "probably correct" : "misspelled");
        }

        System.out.println(GraphLayout.parseInstance(bloomFilter).toFootprint());

    }
}
