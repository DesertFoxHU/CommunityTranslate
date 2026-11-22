package me.bence.ctranslate.lang;

import me.bence.ctranslate.lang.format.LangFormat;
import me.bence.ctranslate.lang.format.LangFormatException;
import me.bence.ctranslate.lang.format.LangFormatRegistry;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A loaded file
 */
public class LangFile {
    private final Path path;
    private final LangFormat format;
    private final LinkedHashMap<String, String> entries;

    private LangFile(Path path, LangFormat format, LinkedHashMap<String, String> entries){
        this.path = path;
        this.format = format;
        this.entries = entries;
    }

    public static LangFile load(Path path, LangFormatRegistry registry) throws LangFormatException {
        LangFormat format = registry.findFor(path).orElseThrow(() -> new LangFormatException("Unsupported file format: " + path));
        return new LangFile(path, format, format.read(path));
    }

    public static LangFile createEmpty(Path path, LangFormat format){
        return new LangFile(path, format, new LinkedHashMap<>());
    }

    public void save() throws LangFormatException {
        format.write(path, entries);
    }

    public String get(String key){
        return entries.get(key);
    }

    public void put(String key, String value){
        entries.put(key, value);
    }

    public void remove(String key){
        entries.remove(key);
    }

    public Set<String> keys(){
        return Collections.unmodifiableSet(entries.keySet());
    }

    public Map<String, String> entries(){
        return Collections.unmodifiableMap(entries);
    }

    public Path path(){
        return path;
    }

    public LangFormat format(){
        return format;
    }

}