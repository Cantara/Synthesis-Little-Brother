package no.cantara.forge.engine;

import no.cantara.forge.template.TemplateVariable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VariableCollectorTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static TemplateVariable var(String name, String defaultValue, boolean required) {
        TemplateVariable v = new TemplateVariable();
        v.setName(name);
        v.setDefaultValue(defaultValue);
        v.setRequired(required);
        v.setPrompt("Enter " + name);
        return v;
    }

    private static PrintWriter nullWriter() {
        return new PrintWriter(new StringWriter());
    }

    // -------------------------------------------------------------------------
    // CLI vars take priority
    // -------------------------------------------------------------------------

    @Test
    void collect_usesCliVarsFirst() throws IOException {
        TemplateVariable groupId = var("groupId", "com.default", false);

        VariableCollector collector = new VariableCollector(
                List.of(groupId),
                Map.of("groupId", "no.cantara"),   // CLI override
                null,
                true,                               // no-interactive
                nullWriter()
        );

        Map<String, String> result = collector.collect();
        assertEquals("no.cantara", result.get("groupId"), "CLI var must take priority");
    }

    // -------------------------------------------------------------------------
    // Default value fallback
    // -------------------------------------------------------------------------

    @Test
    void collect_usesDefaultWhenNoInput() throws IOException {
        TemplateVariable javaVersion = var("javaVersion", "21", false);

        VariableCollector collector = new VariableCollector(
                List.of(javaVersion),
                Map.of(),          // no CLI vars
                null,
                true,              // no-interactive — no prompting
                nullWriter()
        );

        Map<String, String> result = collector.collect();
        assertEquals("21", result.get("javaVersion"), "Default value should be used when no input given");
    }

    // -------------------------------------------------------------------------
    // Required variable with no value
    // -------------------------------------------------------------------------

    @Test
    void collect_throwsWhenRequiredMissing() {
        TemplateVariable artifactId = var("artifactId", null, true);  // required, no default

        VariableCollector collector = new VariableCollector(
                List.of(artifactId),
                Map.of(),   // no CLI vars
                null,
                true,       // no-interactive
                nullWriter()
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, collector::collect);
        assertTrue(ex.getMessage().contains("artifactId"), "Exception should mention the variable name");
    }

    // -------------------------------------------------------------------------
    // Vars file loading
    // -------------------------------------------------------------------------

    @Test
    void collect_loadsVarsFile(@TempDir Path tempDir) throws IOException {
        Path varsFile = tempDir.resolve("vars.yaml");
        Files.writeString(varsFile,
                "groupId: no.cantara.test\n"
                + "artifactId: file-loaded-app\n"
                + "javaVersion: \"17\"\n"
        );

        TemplateVariable groupId = var("groupId", "com.example", false);
        TemplateVariable artifactId = var("artifactId", null, true);
        TemplateVariable javaVersion = var("javaVersion", "21", false);

        VariableCollector collector = new VariableCollector(
                List.of(groupId, artifactId, javaVersion),
                Map.of(),     // no CLI vars
                varsFile,
                true,
                nullWriter()
        );

        Map<String, String> result = collector.collect();
        assertEquals("no.cantara.test", result.get("groupId"), "groupId from vars file");
        assertEquals("file-loaded-app", result.get("artifactId"), "artifactId from vars file");
        assertEquals("17", result.get("javaVersion"), "javaVersion from vars file");
    }

    // -------------------------------------------------------------------------
    // CLI takes priority over vars file
    // -------------------------------------------------------------------------

    @Test
    void collect_cliOverridesVarsFile(@TempDir Path tempDir) throws IOException {
        Path varsFile = tempDir.resolve("vars.yaml");
        Files.writeString(varsFile, "groupId: from.file\n");

        TemplateVariable groupId = var("groupId", "com.default", false);

        VariableCollector collector = new VariableCollector(
                List.of(groupId),
                Map.of("groupId", "from.cli"),  // CLI wins
                varsFile,
                true,
                nullWriter()
        );

        Map<String, String> result = collector.collect();
        assertEquals("from.cli", result.get("groupId"), "CLI var should override vars file");
    }

    // -------------------------------------------------------------------------
    // Optional variable with no value anywhere — not required, not added
    // -------------------------------------------------------------------------

    @Test
    void collect_optionalVariableWithNoValue_notIncluded() throws IOException {
        TemplateVariable opt = var("optionalFlag", null, false);

        VariableCollector collector = new VariableCollector(
                List.of(opt),
                Map.of(),
                null,
                true,
                nullWriter()
        );

        Map<String, String> result = collector.collect();
        assertFalse(result.containsKey("optionalFlag"),
                "Optional variable with no value should not appear in result");
    }

    // -------------------------------------------------------------------------
    // Empty variables list — returns empty map
    // -------------------------------------------------------------------------

    @Test
    void collect_emptyVariableList_returnsEmptyMap() throws IOException {
        VariableCollector collector = new VariableCollector(
                List.of(),
                Map.of(),
                null,
                true,
                nullWriter()
        );

        Map<String, String> result = collector.collect();
        assertTrue(result.isEmpty(), "Empty variable list should produce empty map");
    }

    // -------------------------------------------------------------------------
    // Null variables list — treated as empty
    // -------------------------------------------------------------------------

    @Test
    void collect_nullVariableList_returnsEmptyMap() throws IOException {
        VariableCollector collector = new VariableCollector(
                null,
                Map.of(),
                null,
                true,
                nullWriter()
        );

        Map<String, String> result = collector.collect();
        assertTrue(result.isEmpty(), "Null variable list should produce empty map");
    }
}
