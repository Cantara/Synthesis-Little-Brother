package no.cantara.forge.generate;

import no.cantara.forge.template.TemplateFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProjectGeneratorTest {

    /** Resolves the bundled java-base test template directory from the test classpath. */
    private Path javaBaseTemplate() throws URISyntaxException {
        URL url = getClass().getClassLoader().getResource("templates/java-base");
        assertNotNull(url, "java-base template not found on classpath");
        return Path.of(url.toURI());
    }

    @Test
    void generate_createsFilesWithSubstitution(@TempDir Path tempDir) throws Exception {
        Path templateDir = javaBaseTemplate();
        Path outputDir = tempDir.resolve("my-app");

        Map<String, String> vars = Map.of(
                "groupId", "com.example",
                "artifactId", "my-app",
                "javaVersion", "21"
        );

        ProjectGenerator generator = new ProjectGenerator();
        ProjectGenerator.GenerateResult result = generator.generate(templateDir, outputDir, vars, false);

        // Output directory was created
        assertTrue(Files.isDirectory(outputDir), "Output directory should exist");

        // pom.xml exists and has correct content
        Path pom = outputDir.resolve("pom.xml");
        assertTrue(Files.exists(pom), "pom.xml should exist");
        String pomContent = Files.readString(pom);
        assertTrue(pomContent.contains("<groupId>com.example</groupId>"), "groupId substituted");
        assertTrue(pomContent.contains("<artifactId>my-app</artifactId>"), "artifactId substituted");
        assertTrue(pomContent.contains("<java.version>21</java.version>"), "javaVersion substituted");

        // README.md exists and has pascal-case artifactId
        Path readme = outputDir.resolve("README.md");
        assertTrue(Files.exists(readme), "README.md should exist");
        String readmeContent = Files.readString(readme);
        assertTrue(readmeContent.contains("MyApp"), "README should contain pascal-case project name");

        // .gitignore exists
        assertTrue(Files.exists(outputDir.resolve(".gitignore")), ".gitignore should exist");

        // Result counts correct: 3 files (pom.xml, README.md, .gitignore)
        assertEquals(3, result.filesCreated(), "Should create 3 files");
        assertTrue(result.skippedFiles().isEmpty(), "No files should be skipped");
    }

    @Test
    void generate_dryRun_doesNotCreateFiles(@TempDir Path tempDir) throws Exception {
        Path templateDir = javaBaseTemplate();
        Path outputDir = tempDir.resolve("dry-run-output");

        Map<String, String> vars = Map.of(
                "groupId", "com.example",
                "artifactId", "dry-test",
                "javaVersion", "21"
        );

        ProjectGenerator generator = new ProjectGenerator();
        ProjectGenerator.GenerateResult result = generator.generate(templateDir, outputDir, vars, true);

        // Output directory must NOT be created in dry-run mode
        assertFalse(Files.exists(outputDir), "Output directory should NOT be created during dry-run");

        // Result still reports what would have been created
        assertTrue(result.filesCreated() > 0, "Dry-run should report file count");
    }

    @Test
    void generate_discoversFilesAutomatically(@TempDir Path tempDir) throws Exception {
        // Build a template dir with NO files: block in forge-template.yaml
        Path templateDir = tempDir.resolve("auto-template");
        Files.createDirectories(templateDir);

        // Minimal manifest with no files: block
        Files.writeString(templateDir.resolve("forge-template.yaml"),
                "forge_version: \"0.1\"\n"
                + "template:\n"
                + "  id: auto-test\n"
                + "  name: \"Auto Test\"\n"
                + "  version: \"1.0.0\"\n"
        );

        // Add a couple of static files to auto-discover
        Files.writeString(templateDir.resolve("hello.txt"), "Hello world");
        Path subDir = templateDir.resolve("subdir");
        Files.createDirectories(subDir);
        Files.writeString(subDir.resolve("sub.txt"), "sub content");

        Path outputDir = tempDir.resolve("auto-output");
        Map<String, String> vars = Map.of();

        ProjectGenerator generator = new ProjectGenerator();
        ProjectGenerator.GenerateResult result = generator.generate(templateDir, outputDir, vars, false);

        // Both files should have been discovered and copied
        assertTrue(Files.exists(outputDir.resolve("hello.txt")), "hello.txt should be generated");
        assertTrue(Files.exists(outputDir.resolve("subdir").resolve("sub.txt")),
                "subdir/sub.txt should be generated");
        assertEquals(2, result.filesCreated(), "Should auto-discover and create 2 files");
    }

    @Test
    void generate_conditionalFile_skippedWhenFalse(@TempDir Path tempDir) throws Exception {
        // Build a template dir with a conditional file
        Path templateDir = tempDir.resolve("cond-template");
        Files.createDirectories(templateDir);

        Files.writeString(templateDir.resolve("forge-template.yaml"),
                "forge_version: \"0.1\"\n"
                + "template:\n"
                + "  id: cond-test\n"
                + "  name: \"Conditional Test\"\n"
                + "  version: \"1.0.0\"\n"
                + "variables:\n"
                + "  - name: includeDocker\n"
                + "    type: boolean\n"
                + "    default: \"false\"\n"
                + "files:\n"
                + "  - source: always.txt\n"
                + "    target: always.txt\n"
                + "  - source: Dockerfile\n"
                + "    target: Dockerfile\n"
                + "    condition: \"{{includeDocker}}\"\n"
        );

        Files.writeString(templateDir.resolve("always.txt"), "always here");
        Files.writeString(templateDir.resolve("Dockerfile"), "FROM openjdk:21");

        Path outputDir = tempDir.resolve("cond-output");
        Map<String, String> vars = Map.of("includeDocker", "false");

        ProjectGenerator generator = new ProjectGenerator();
        ProjectGenerator.GenerateResult result = generator.generate(templateDir, outputDir, vars, false);

        // always.txt must exist
        assertTrue(Files.exists(outputDir.resolve("always.txt")), "always.txt should exist");
        // Dockerfile must be skipped
        assertFalse(Files.exists(outputDir.resolve("Dockerfile")), "Dockerfile should be skipped");

        assertEquals(1, result.filesCreated(), "Only 1 file should be created");
        assertEquals(1, result.skippedFiles().size(), "1 file should be skipped");
        assertEquals("Dockerfile", result.skippedFiles().get(0));
    }

    @Test
    void discoverFiles_excludesManifest(@TempDir Path tempDir) throws IOException {
        Path templateDir = tempDir.resolve("discover-test");
        Files.createDirectories(templateDir);
        Files.writeString(templateDir.resolve("forge-template.yaml"), "forge_version: \"0.1\"");
        Files.writeString(templateDir.resolve("actual.txt"), "content");

        ProjectGenerator generator = new ProjectGenerator();
        List<TemplateFile> files = generator.discoverFiles(templateDir);

        assertEquals(1, files.size(), "Only actual.txt should be discovered");
        assertEquals("actual.txt", files.get(0).getSource());
    }
}
