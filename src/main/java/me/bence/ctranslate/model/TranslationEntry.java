package me.bence.ctranslate.model;

import javafx.beans.property.*;
import me.bence.ctranslate.validation.PlaceholderValidator;

import java.util.Optional;

public class TranslationEntry {
    private final String key;
    private final StringProperty sourceText = new SimpleStringProperty();
    private final StringProperty translation = new SimpleStringProperty();
    private final ObjectProperty<TranslationStatus> status = new SimpleObjectProperty<>();
    private final ReadOnlyStringWrapper problem = new ReadOnlyStringWrapper();

    public TranslationEntry(String key, String sourceText, String translation){
        this.key = key;
        this.sourceText.set(sourceText);
        this.translation.addListener((old, oldValue, newValue) -> updateStatus());
        this.translation.set(translation == null ? "" : translation);
        updateStatus();
    }

    private void updateStatus() {
        String value = translation.get();
        if (value == null || value.isBlank()) {
            problem.set(null);
            status.set(TranslationStatus.MISSING);
            return;
        }
        Optional<String> issue = PlaceholderValidator.validate(sourceText.get(), value);
        problem.set(issue.orElse(null));
        status.set(issue.isPresent() ? TranslationStatus.INVALID : TranslationStatus.TRANSLATED);
    }

    public String getKey() { return key; }

    public StringProperty sourceTextProperty() { return sourceText; }
    public String getSourceText() { return sourceText.get(); }

    public StringProperty translationProperty() { return translation; }
    public String getTranslation() { return translation.get(); }
    public void setTranslation(String value) { translation.set(value); }

    public ReadOnlyObjectProperty<TranslationStatus> statusProperty() { return status; }
    public TranslationStatus getStatus() { return status.get(); }

    public ReadOnlyStringProperty problemProperty() { return problem.getReadOnlyProperty(); }
    public String getProblem() { return problem.get(); }
}