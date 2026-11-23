package me.bence.ctranslate.data;

import java.nio.file.Path;

public record Project(long id, String name, Path sourcePath) {
}