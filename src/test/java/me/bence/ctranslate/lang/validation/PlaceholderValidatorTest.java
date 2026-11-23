package me.bence.ctranslate.lang.validation;

import me.bence.ctranslate.validation.PlaceholderValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlaceholderValidatorTest {
    @Test
    void samePlaceholdersAreValid() {
        assertTrue(PlaceholderValidator.validate("Hello %s!", "Szia %s!").isEmpty());
    }

    @Test
    void reorderedPlaceholdersAreValid() {
        assertTrue(PlaceholderValidator.validate("%1$s killed %2$s", "%2$s-t megölte %1$s").isEmpty());
    }

    @Test
    void missingPlaceholderIsInvalid() {
        assertTrue(PlaceholderValidator.validate("You have %d coins", "Van pénzed").isPresent());
    }

    @Test
    void formattingCodesAreChecked() {
        assertTrue(PlaceholderValidator.validate("§aGreen text", "Zöld szöveg").isPresent());
    }

    @Test
    void percentSignInTextIsNotAPlaceholder() {
        assertTrue(PlaceholderValidator.validate("50% chance", "50% esély").isEmpty());
    }
}