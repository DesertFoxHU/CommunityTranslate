package me.bence.ctranslate.lang.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class JsonLangFormat implements LangFormat {
    private final ObjectMapper mapper = new ObjectMapper();

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
        try {
            return mapper.readValue(file.toFile(), new TypeReference<LinkedHashMap<String, String>>() {});
        }
        catch(JsonProcessingException e){
            throw new LangFormatException("Corrupt json file: " + file, e);
        }
        catch(IOException e){
            throw new LangFormatException("Couldn't read file: " + file, e);
        }
    }

    @Override
    public void write(Path file, Map<String, String> entries) throws LangFormatException {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), entries);
        } catch (IOException e) {
            throw new LangFormatException("Couldn't write to file: " + file, e);
        }
    }
}