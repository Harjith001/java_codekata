package org.example;

public interface SpellChecker {
    void loadDictionary(String dictionaryFilePath) throws Exception;
    boolean isWordCorrect(String word);
}
