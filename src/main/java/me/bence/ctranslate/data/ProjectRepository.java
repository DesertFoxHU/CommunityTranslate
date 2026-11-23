package me.bence.ctranslate.data;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {
    private final Database database;

    public ProjectRepository(Database database) {
        this.database = database;
    }

    public List<Project> findRecentProjects() throws SQLException {
        List<Project> projects = new ArrayList<>();
        try(Connection connection = database.connection(); PreparedStatement statement = connection.prepareStatement("SELECT id, name, source_path FROM project ORDER BY last_opened_at DESC LIMIT 10");){
            ResultSet result = statement.executeQuery();
            while(result.next()){
                projects.add(new Project(
                        result.getLong("id"),
                        result.getString("name"),
                        Path.of(result.getString("source_path"))));
            }
        }
        return projects;
    }

    public Project saveProject(String name, Path sourcePath) throws SQLException {
        String path = sourcePath.toAbsolutePath().toString();
        try (Connection connection = database.connection()) {
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT OR IGNORE INTO project (name, source_path) VALUES (?, ?)")) {
                insert.setString(1, name);
                insert.setString(2, path);
                insert.executeUpdate();
            }
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE project SET last_opened_at = CURRENT_TIMESTAMP WHERE source_path = ?")) {
                update.setString(1, path);
                update.executeUpdate();
            }
            try (PreparedStatement select = connection.prepareStatement(
                    "SELECT id, name, source_path FROM project WHERE source_path = ?")) {
                select.setString(1, path);
                try (ResultSet result = select.executeQuery()) {
                    if (result.next()) {
                        return new Project(
                                result.getLong("id"),
                                result.getString("name"),
                                Path.of(result.getString("source_path")));
                    }
                }
            }
        }
        throw new SQLException("Project not found after insert: " + path);
    }

    public void addTranslationFile(long projectId, Path targetPath) throws SQLException {
        try (Connection connection = database.connection();
             PreparedStatement insert = connection.prepareStatement("INSERT OR IGNORE INTO project_file (project_id, target_path) VALUES (?, ?)")) {
            insert.setLong(1, projectId);
            insert.setString(2, targetPath.toAbsolutePath().toString());
            insert.executeUpdate();
        }
    }

    public List<Path> findTranslationFiles(long projectId) throws SQLException {
        List<Path> files = new ArrayList<>();
        try (Connection connection = database.connection();
             PreparedStatement select = connection.prepareStatement("SELECT target_path FROM project_file WHERE project_id = ? ORDER BY target_path")) {
            select.setLong(1, projectId);
            try (ResultSet result = select.executeQuery()) {
                while (result.next()) {
                    files.add(Path.of(result.getString("target_path")));
                }
            }
        }
        return files;
    }
}