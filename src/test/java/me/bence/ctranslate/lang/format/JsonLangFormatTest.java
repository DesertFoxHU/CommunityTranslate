package me.bence.ctranslate.lang.format;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonLangFormatTest {
    private final JsonLangFormat format = new JsonLangFormat();

    @TempDir
    Path tempDir;

    @Test
    void supportJsonFiles(){
        assertTrue(format.supports(Path.of("samples/en_us.json")));
        assertTrue(format.supports(Path.of("a.JSON")));
        assertFalse(format.supports(Path.of("en_us.text")));
    }

    @Test
    void writeAndRead() throws Exception {
        var original = new LinkedHashMap<String, String>();
        original.put("menu.start", "Indítás");
        original.put("menu.quit", "Kilépés");
        original.put("item.sword", "Általános Kard");

        Path file = tempDir.resolve("hu_hu.json");
        format.write(file, original);
        var loaded = format.read(file);

        assertEquals(original, loaded);
        //We check if the content holds the order of the keys correctly
        assertEquals(List.copyOf(original.keySet()), List.copyOf(loaded.keySet()));
    }

    @Test
    void invalidJsonThrows() throws Exception {
        Path file = tempDir.resolve("broken.json");
        Files.writeString(file, "{ not correct json");
        assertThrows(LangFormatException.class, () -> format.read(file));
    }
}