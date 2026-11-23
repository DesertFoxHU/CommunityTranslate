package me.bence.ctranslate.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderValidator {

    private static final Pattern PLACEHOLDER = Pattern.compile(
            "%(?:\\d+\\$)?[-#+0,(]*\\d*(?:\\.\\d+)?[a-zA-Z]" + "|\\{[^{}\\s]*}" + "|§[0-9a-fk-orA-FK-OR]"
    );

    public PlaceholderValidator() {
    }

    public static List<String> extract(String text){
        List<String> found = new ArrayList<>();
        if(text == null){
            return found;
        }
        Matcher matcher = PLACEHOLDER.matcher(text);
        while(matcher.find()){
            found.add(matcher.group());
        }
        Collections.sort(found);
        return found;
    }

    public static Optional<String> validate(String source, String translation){
        List<String> expected = extract(source);
        List<String> actual = extract(translation);
        if(expected.equals(actual)){
            return Optional.empty();
        }
        return Optional.of("Expected placeholders " + expected + ", found " + actual);
    }
}