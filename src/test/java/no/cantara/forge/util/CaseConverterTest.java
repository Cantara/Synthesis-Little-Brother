package no.cantara.forge.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaseConverterTest {

    // ── toUpperCase ──────────────────────────────────────────────────────────

    @Test
    void toUpperCase_basicString() {
        assertEquals("HELLO WORLD", CaseConverter.toUpperCase("hello world"));
    }

    @Test
    void toUpperCase_alreadyUpper() {
        assertEquals("FORGE", CaseConverter.toUpperCase("FORGE"));
    }

    @Test
    void toUpperCase_nullReturnsEmpty() {
        assertEquals("", CaseConverter.toUpperCase(null));
    }

    // ── toLowerCase ──────────────────────────────────────────────────────────

    @Test
    void toLowerCase_basicString() {
        assertEquals("hello world", CaseConverter.toLowerCase("HELLO WORLD"));
    }

    @Test
    void toLowerCase_mixed() {
        assertEquals("forge-cli", CaseConverter.toLowerCase("Forge-CLI"));
    }

    @Test
    void toLowerCase_nullReturnsEmpty() {
        assertEquals("", CaseConverter.toLowerCase(null));
    }

    // ── toPascalCase ─────────────────────────────────────────────────────────

    @Test
    void toPascalCase_hyphenated() {
        assertEquals("MyProject", CaseConverter.toPascalCase("my-project"));
    }

    @Test
    void toPascalCase_underscored() {
        assertEquals("MyProject", CaseConverter.toPascalCase("my_project"));
    }

    @Test
    void toPascalCase_spaced() {
        assertEquals("MyProject", CaseConverter.toPascalCase("my project"));
    }

    @Test
    void toPascalCase_multiWord() {
        assertEquals("MyAwesomeProject", CaseConverter.toPascalCase("my-awesome-project"));
    }

    @Test
    void toPascalCase_singleWord() {
        assertEquals("Forge", CaseConverter.toPascalCase("forge"));
    }

    @Test
    void toPascalCase_nullOrBlankReturnsEmpty() {
        assertEquals("", CaseConverter.toPascalCase(null));
        assertEquals("", CaseConverter.toPascalCase("   "));
    }

    // ── toCamelCase ──────────────────────────────────────────────────────────

    @Test
    void toCamelCase_hyphenated() {
        assertEquals("myProject", CaseConverter.toCamelCase("my-project"));
    }

    @Test
    void toCamelCase_underscored() {
        assertEquals("myProject", CaseConverter.toCamelCase("my_project"));
    }

    @Test
    void toCamelCase_multiWord() {
        assertEquals("myAwesomeProject", CaseConverter.toCamelCase("my-awesome-project"));
    }

    @Test
    void toCamelCase_singleWord() {
        assertEquals("forge", CaseConverter.toCamelCase("Forge"));
    }

    @Test
    void toCamelCase_nullOrBlankReturnsEmpty() {
        assertEquals("", CaseConverter.toCamelCase(null));
        assertEquals("", CaseConverter.toCamelCase(""));
    }

    // ── toKebabCase ──────────────────────────────────────────────────────────

    @Test
    void toKebabCase_pascalCase() {
        assertEquals("my-project", CaseConverter.toKebabCase("MyProject"));
    }

    @Test
    void toKebabCase_camelCase() {
        assertEquals("my-project", CaseConverter.toKebabCase("myProject"));
    }

    @Test
    void toKebabCase_underscored() {
        assertEquals("my-project", CaseConverter.toKebabCase("my_project"));
    }

    @Test
    void toKebabCase_alreadyKebab() {
        assertEquals("my-project", CaseConverter.toKebabCase("my-project"));
    }

    @Test
    void toKebabCase_upperSnakeCase() {
        assertEquals("my-project", CaseConverter.toKebabCase("MY_PROJECT"));
    }

    @Test
    void toKebabCase_multiWord() {
        assertEquals("my-awesome-project", CaseConverter.toKebabCase("MyAwesomeProject"));
    }

    @Test
    void toKebabCase_nullOrBlankReturnsEmpty() {
        assertEquals("", CaseConverter.toKebabCase(null));
        assertEquals("", CaseConverter.toKebabCase("   "));
    }
}
