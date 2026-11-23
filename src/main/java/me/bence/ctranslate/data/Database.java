package me.bence.ctranslate.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private final String url;

    public Database(Path file) throws SQLException {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if(parent != null){
                Files.createDirectories(parent);
            }
        }
        catch(IOException e){
            throw new SQLException("Couldn't create database folder for " + file, e);
        }
        this.url = "jdbc:sqlite:" + file.toAbsolutePath();
        createSchema();
    }

    public static Path defaultLocation() {
        String appData = System.getenv("APPDATA");
        Path base = appData != null ? Path.of(appData) : Path.of(System.getProperty("user.home"));
        return base.resolve("CommunityTranslate").resolve("data.db");
    }

    public Connection connection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try(Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private void createSchema() throws SQLException {
        try(Connection connection = connection(); Statement statement = connection.createStatement()){
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS project (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, source_path TEXT NOT NULL UNIQUE, last_opened_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS project_file (id INTEGER PRIMARY KEY AUTOINCREMENT, project_id INTEGER NOT NULL REFERENCES project (id) ON DELETE CASCADE, target_path TEXT NOT NULL, UNIQUE (project_id, target_path))
                    """);
        }
    }

}