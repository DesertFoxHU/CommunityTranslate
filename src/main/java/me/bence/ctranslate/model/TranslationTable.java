package me.bence.ctranslate.model;

import impl.org.controlsfx.i18n.Translation;
import me.bence.ctranslate.lang.LangFile;

import java.util.ArrayList;
import java.util.List;

public final class TranslationTable {

    private TranslationTable() {
    }

    public static List<TranslationEntry> build(LangFile source, LangFile target){
        List<TranslationEntry> rows = new ArrayList<>();
        for(String key : source.keys()){
            String existing = target != null ? target.get(key) : null;
            rows.add(new TranslationEntry(key, source.get(key), existing));
        }
        return rows;
    }

    public static void applyTo(List<TranslationEntry> rows, LangFile target){
        for(TranslationEntry row : rows){
            String value = row.getTranslation();
            if(value == null || value.isBlank()){
                target.remove(row.getKey());
            }
            else {
                target.put(row.getKey(), value);
            }
        }
    }
}