package me.bence.ctranslate.lang.format;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public interface LangFormat {
    String id();

    /**
     * Returns if this format supports this file
     * @param file checked file
     * @return true if it supports
     */
    boolean supports(Path file);

    LinkedHashMap<String, String> read(Path file) throws LangFormatException;

    void write(Path file, Map<String, String> entries) throws LangFormatException;
}