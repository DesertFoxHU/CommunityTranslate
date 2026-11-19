package me.bence.ctranslate.lang.format;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class JsonLangFormat implements LangFormat {
    @Override
    public String id() {
        return "json";
    }

    @Override
    public boolean supports(Path file) {
        Path name = file.getFileName();
        if(name == null){
            return false;
        }
        return name.toString().toLowerCase(Locale.ROOT).endsWith(".json");
    }

    @Override
    public LinkedHashMap<String, String> read(Path file) throws LangFormatException {
        return null;
    }

    @Override
    public void write(Path file, Map<String, String> entries) throws LangFormatException {

    }
}