package org.example;

import java.io.InputStream;

public interface SpellChecker {
    void loadDictionary(InputStream dictionaryFilePath) throws Exception;
    boolean isWordCorrect(String word);
}
