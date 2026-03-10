package no.cantara.forge.generate;

import no.cantara.forge.template.TemplateFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileRendererTest {

    // -------------------------------------------------------------------------
    // render — FTL files
    // -------------------------------------------------------------------------

    @Test
    void render_ftlFile_substitutesVariables(@TempDir Path tempDir) throws Exception {
        Path templateDir = tempDir.resolve("template");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(templateDir);
        Files.createDirectories(outputDir);

        // Create a .ftl source file
        Files.writeString(templateDir.resolve("pom.xml.ftl"),
                "<groupId>{{groupId}}</groupId>\n<artifactId>{{artifactId}}</artifactId>");

        TemplateFile entry = new TemplateFile();
        entry.setSource("pom.xml.ftl");
        entry.setTarget("pom.xml.ftl");  // renderer should strip .ftl from target

        FileRenderer.render(entry, templateDir, outputDir,
                Map.of("groupId", "no.cantara", "artifactId", "my-service"));

        Path rendered = outputDir.resolve("pom.xml");
        assertTrue(Files.exists(rendered), "pom.xml should exist (stripped .ftl suffix)");
        String content = Files.readString(rendered);
        assertTrue(content.contains("<groupId>no.cantara</groupId>"), "groupId substituted");
        assertTrue(content.contains("<artifactId>my-service</artifactId>"), "artifactId substituted");
    }

    @Test
    void render_staticFile_copiedAsIs(@TempDir Path tempDir) throws Exception {
        Path templateDir = tempDir.resolve("template");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(templateDir);
        Files.createDirectories(outputDir);

        String gitignoreContent = "target/\n*.class\n.idea/\n*.iml\n";
        Files.writeString(templateDir.resolve(".gitignore"), gitignoreContent);

        TemplateFile entry = new TemplateFile();
        entry.setSource(".gitignore");
        entry.setTarget(".gitignore");

        FileRenderer.render(entry, templateDir, outputDir, Map.of());

        Path copied = outputDir.resolve(".gitignore");
        assertTrue(Files.exists(copied), ".gitignore should be copied");
        assertEquals(gitignoreContent, Files.readString(copied), "Content must be identical");
    }

    @Test
    void render_targetPathWithVariables(@TempDir Path tempDir) throws Exception {
        // Target path contains a {{packageDir}} placeholder — common for Java source files
        Path templateDir = tempDir.resolve("template");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(templateDir);

        // Nest the source file in a subdirectory to mirror a realistic template layout
        Path srcDir = templateDir.resolve("src");
        Files.createDirectories(srcDir);
        Files.writeString(srcDir.resolve("App.java.ftl"), "package {{groupId}};\npublic class App {}");

        TemplateFile entry = new TemplateFile();
        entry.setSource("src/App.java.ftl");
        entry.setTarget("{{packageDir}}/App.java.ftl");

        Map<String, String> vars = Map.of(
                "packageDir", "src/main/java/no/cantara",
                "groupId", "no.cantara"
        );

        FileRenderer.render(entry, templateDir, outputDir, vars);

        Path expected = outputDir.resolve("src/main/java/no/cantara/App.java");
        assertTrue(Files.exists(expected), "File should be written at the substituted target path");
        String content = Files.readString(expected);
        assertTrue(content.contains("package no.cantara;"), "Package substituted in content");
    }

    // -------------------------------------------------------------------------
    // evaluateCondition
    // -------------------------------------------------------------------------

    @Test
    void evaluateCondition_trueForTrue() {
        assertTrue(FileRenderer.evaluateCondition("{{flag}}", Map.of("flag", "true")));
    }

    @Test
    void evaluateCondition_trueForYes() {
        assertTrue(FileRenderer.evaluateCondition("{{flag}}", Map.of("flag", "yes")));
    }

    @Test
    void evaluateCondition_trueForOne() {
        assertTrue(FileRenderer.evaluateCondition("{{flag}}", Map.of("flag", "1")));
    }

    @Test
    void evaluateCondition_trueForUppercaseTrue() {
        assertTrue(FileRenderer.evaluateCondition("{{flag}}", Map.of("flag", "TRUE")));
    }

    @Test
    void evaluateCondition_falseForFalse() {
        assertFalse(FileRenderer.evaluateCondition("{{flag}}", Map.of("flag", "false")));
    }

    @Test
    void evaluateCondition_falseForNo() {
        assertFalse(FileRenderer.evaluateCondition("{{flag}}", Map.of("flag", "no")));
    }

    @Test
    void evaluateCondition_falseForZero() {
        assertFalse(FileRenderer.evaluateCondition("{{flag}}", Map.of("flag", "0")));
    }

    @Test
    void evaluateCondition_falseForUnknownVariable() {
        assertFalse(FileRenderer.evaluateCondition("{{unknownVar}}", Map.of()));
    }

    @Test
    void evaluateCondition_falseForBlank() {
        assertFalse(FileRenderer.evaluateCondition("{{flag}}", Map.of("flag", "")));
    }

    // -------------------------------------------------------------------------
    // conditional skip
    // -------------------------------------------------------------------------

    @Test
    void render_conditionalFile_skippedWhenConditionFalse(@TempDir Path tempDir) throws Exception {
        Path templateDir = tempDir.resolve("template");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(templateDir);
        Files.createDirectories(outputDir);

        Files.writeString(templateDir.resolve("Dockerfile"), "FROM openjdk:21");

        TemplateFile entry = new TemplateFile();
        entry.setSource("Dockerfile");
        entry.setTarget("Dockerfile");
        entry.setCondition("{{includeDocker}}");

        FileRenderer.render(entry, templateDir, outputDir, Map.of("includeDocker", "false"));

        assertFalse(Files.exists(outputDir.resolve("Dockerfile")),
                "Dockerfile should be skipped when condition is false");
    }

    @Test
    void render_conditionalFile_renderedWhenConditionTrue(@TempDir Path tempDir) throws Exception {
        Path templateDir = tempDir.resolve("template");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(templateDir);
        Files.createDirectories(outputDir);

        Files.writeString(templateDir.resolve("Dockerfile"), "FROM openjdk:21");

        TemplateFile entry = new TemplateFile();
        entry.setSource("Dockerfile");
        entry.setTarget("Dockerfile");
        entry.setCondition("{{includeDocker}}");

        FileRenderer.render(entry, templateDir, outputDir, Map.of("includeDocker", "true"));

        assertTrue(Files.exists(outputDir.resolve("Dockerfile")),
                "Dockerfile should be written when condition is true");
    }
}
