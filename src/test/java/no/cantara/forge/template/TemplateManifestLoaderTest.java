package no.cantara.forge.template;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TemplateManifestLoaderTest {

    @Test
    void load_minimialManifestFromTestResources() throws IOException {
        // Locate the test resource directory via classloader
        var resourceUrl = getClass().getClassLoader().getResource("test-template");
        assertNotNull(resourceUrl, "test-template resource directory not found on classpath");

        Path templateDir = Path.of(resourceUrl.getPath());
        TemplateManifest manifest = TemplateManifestLoader.load(templateDir);

        assertNotNull(manifest, "manifest should not be null");
        assertEquals("0.1", manifest.getForgeVersion());

        TemplateManifest.TemplateInfo info = manifest.getTemplate();
        assertNotNull(info);
        assertEquals("test-template", info.getId());
        assertEquals("Test Template", info.getName());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("A test template", info.getDescription());
    }

    @Test
    void load_parsesVariables() throws IOException {
        var resourceUrl = getClass().getClassLoader().getResource("test-template");
        assertNotNull(resourceUrl);

        Path templateDir = Path.of(resourceUrl.getPath());
        TemplateManifest manifest = TemplateManifestLoader.load(templateDir);

        assertNotNull(manifest.getVariables());
        assertFalse(manifest.getVariables().isEmpty());

        TemplateVariable firstVar = manifest.getVariables().get(0);
        assertEquals("projectName", firstVar.getName());
        assertEquals("Project name", firstVar.getDescription());
        assertEquals("string", firstVar.getType());
        assertTrue(firstVar.isRequired());
    }

    @Test
    void load_throwsWhenManifestMissing(@TempDir Path emptyDir) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TemplateManifestLoader.load(emptyDir)
        );
        assertTrue(ex.getMessage().contains("No forge-template.yaml found in"),
                "Exception message should indicate missing manifest, was: " + ex.getMessage());
    }

    @Test
    void load_customManifestWithAllFields(@TempDir Path tempDir) throws IOException {
        String yaml = """
                forge_version: "0.2"
                template:
                  id: cantara/java-service
                  name: "Cantara Java Service"
                  version: "2.0.0"
                  description: "Standard Java 21 microservice"
                  author: "Cantara"
                  license: "Apache-2.0"
                  tags:
                    - java
                    - microservice
                variables:
                  - name: groupId
                    type: string
                    required: true
                  - name: artifactId
                    type: string
                    required: true
                    default: "my-service"
                files:
                  - source: templates/pom.xml.tmpl
                    target: pom.xml
                """;

        Files.writeString(tempDir.resolve("forge-template.yaml"), yaml);
        TemplateManifest manifest = TemplateManifestLoader.load(tempDir);

        assertEquals("0.2", manifest.getForgeVersion());
        assertEquals("cantara/java-service", manifest.getTemplate().getId());
        assertEquals("Cantara", manifest.getTemplate().getAuthor());
        assertEquals("Apache-2.0", manifest.getTemplate().getLicense());

        assertNotNull(manifest.getTemplate().getTags());
        assertEquals(2, manifest.getTemplate().getTags().size());
        assertTrue(manifest.getTemplate().getTags().contains("java"));

        assertEquals(2, manifest.getVariables().size());
        assertEquals("groupId", manifest.getVariables().get(0).getName());
        assertTrue(manifest.getVariables().get(0).isRequired());

        assertNotNull(manifest.getFiles());
        assertEquals(1, manifest.getFiles().size());
        assertEquals("templates/pom.xml.tmpl", manifest.getFiles().get(0).getSource());
        assertEquals("pom.xml", manifest.getFiles().get(0).getTarget());
    }
}
