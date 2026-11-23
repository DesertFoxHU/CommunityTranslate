package me.bence.ctranslate;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import me.bence.ctranslate.lang.LangFile;
import me.bence.ctranslate.lang.format.LangFormatException;
import me.bence.ctranslate.lang.format.LangFormatRegistry;
import me.bence.ctranslate.model.TranslationEntry;
import me.bence.ctranslate.model.TranslationStatus;
import me.bence.ctranslate.model.TranslationTable;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

public class EditorController {
    private static final String FILTER_ALL = "All";
    private static final String FILTER_MISSING = "Missing";
    private static final String FILTER_TRANSLATED = "Translated";
    private static final String FILTER_INVALID = "Invalid";

    @FXML private TableView<TranslationEntry> table;
    @FXML private TableColumn<TranslationEntry, String> keyColumn;
    @FXML private TableColumn<TranslationEntry, String> sourceColumn;
    @FXML private TableColumn<TranslationEntry, String> translationColumn;
    @FXML private TableColumn<TranslationEntry, TranslationStatus> statusColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label fileLabel;
    @FXML private Label progressLabel;

    private final LangFormatRegistry registry = new LangFormatRegistry();

    private final ObservableList<TranslationEntry> rows = FXCollections.observableArrayList(entry -> new Observable[] { entry.statusProperty() });
    private final FilteredList<TranslationEntry> filteredRows = new FilteredList<>(rows);

    private LangFile source;
    private LangFile target;

    @FXML
    private void initialize() {
        keyColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getKey()));
        sourceColumn.setCellValueFactory(cell -> cell.getValue().sourceTextProperty());
        translationColumn.setCellValueFactory(cell -> cell.getValue().translationProperty());
        translationColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(TranslationStatus status, boolean empty) {
                super.updateItem(status, empty);
                setTooltip(null);
                setStyle("");
                if (empty || status == null) {
                    setText(null);
                    return;
                }
                setText(statusLabel(status));
                if (status == TranslationStatus.INVALID) {
                    setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                    TranslationEntry entry = getTableRow() == null ? null : getTableRow().getItem();
                    if (entry != null) {
                        setTooltip(new Tooltip(entry.getProblem()));
                    }
                }
            }
        });

        table.setItems(filteredRows);

        statusFilter.getItems().setAll(FILTER_ALL, FILTER_MISSING, FILTER_TRANSLATED, FILTER_INVALID);
        statusFilter.setValue(FILTER_ALL);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        rows.addListener((ListChangeListener<? super TranslationEntry>) change -> updateProgress());
        updateProgress();
    }

    @FXML
    private void onOpenSource(){
        Path file = chooseFile("Open source file", false);
        if(file == null){
            return;
        }

        try{
            source = LangFile.load(file, registry);
            target = null;
            refreshRows();
        }
        catch(LangFormatException e) {
            showError("Couldn't open source file", e);
        }
    }

    @FXML
    private void onOpenTarget(){
        if(source == null){
            showInfo("First open a source file");
            return;
        }
        Path file = chooseFile("Open Translation file", false);
        if(file == null){
            return;
        }

        try {
            target = LangFile.load(file, registry);
            refreshRows();
        }
        catch(LangFormatException e){
            showError("Couldn't open the translation file", e);
        }
    }

    @FXML
    private void onSave(){
        if(source == null){
            showInfo("There is nothing to be saved");
            return;
        }
        long invalid = rows.stream().filter(e -> e.getStatus() == TranslationStatus.INVALID).count();
        if (invalid > 0 && !confirm(invalid + " translation(s) have placeholder problems. Save anyway?")) {
            return;
        }
        if(target == null){
            Path file = chooseFile("Save translation", true);
            if(file == null){
                return;
            }
            target = LangFile.createEmpty(file, source.format());
        }
        TranslationTable.applyTo(rows, target);
        try {
            target.save();
            updateFileLabel();
        }
        catch(LangFormatException e){
            showError("Couldn't be saved", e);
        }
    }

    private void refreshRows(){
        rows.setAll(TranslationTable.build(source, target));
        updateFileLabel();
    }

    private void updateFilter(){
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String filter = statusFilter.getValue();
        filteredRows.setPredicate(entry -> matchesStatus(entry, filter) && matchesSearch(entry, search));
    }

    private static String statusLabel(TranslationStatus status) {
        return switch (status) {
            case MISSING -> FILTER_MISSING;
            case TRANSLATED -> FILTER_TRANSLATED;
            case INVALID -> FILTER_INVALID;
        };
    }

    private boolean matchesStatus(TranslationEntry entry, String filter) {
        return FILTER_ALL.equals(filter) || statusLabel(entry.getStatus()).equals(filter);
    }

    private boolean matchesSearch(TranslationEntry entry, String search) {
        if (search.isEmpty()) {
            return true;
        }
        return contains(entry.getKey(), search) || contains(entry.getSourceText(), search) || contains(entry.getTranslation(), search);
    }

    private static boolean contains(String text, String search) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(search);
    }

    private void updateProgress() {
        long translated = rows.stream().filter(e -> e.getStatus() == TranslationStatus.TRANSLATED).count();
        long invalid = rows.stream().filter(e -> e.getStatus() == TranslationStatus.INVALID).count();
        progressLabel.setText(rows.isEmpty() ? "" : translated + " / " + rows.size() + " translated" + (invalid > 0 ? ", " + invalid + " invalid" : ""));
    }

    private void updateFileLabel(){
        String sourceName = source == null ? "-" : source.path().getFileName().toString();
        String targetName = target == null ? "not saved" : target.path().getFileName().toString();
        fileLabel.setText("Source: " + sourceName + " -> Translation: " + targetName);
    }

    private Path chooseFile(String title, boolean save){
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON language file", "*.json"),
                new FileChooser.ExtensionFilter("Every file", "*.*"));

        if(source != null && source.path().getParent() != null){
            chooser.setInitialDirectory(source.path().getParent().toFile());
        }

        Window window = table.getScene().getWindow();
        File file = save ? chooser.showSaveDialog(window) : chooser.showOpenDialog(window);
        return file == null ? null : file.toPath();
    }

    private void showError(String header, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        return alert.showAndWait().filter(button -> button == ButtonType.YES).isPresent();
    }

}