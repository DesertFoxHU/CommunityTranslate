package me.bence.ctranslate.lang.format;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class LangFormatRegistry {

    private final List<LangFormat> formats = List.of(new JsonLangFormat());

    public LangFormatRegistry() {
    }

    public Optional<LangFormat> findFor(Path file){
        return formats.stream().filter(x -> x.supports(file)).findFirst();
    }

    public Optional<LangFormat> byId(String id){
        return formats.stream().filter(x -> x.id().equals(id)).findFirst();
    }
}