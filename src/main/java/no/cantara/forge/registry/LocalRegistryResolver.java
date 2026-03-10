package no.cantara.forge.registry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Resolves templates from a local filesystem directory.
 * <p>
 * A valid template is any subdirectory that contains a {@code forge-template.yaml} file.
 *
 * <h3>Expected directory layout:</h3>
 * <pre>{@code
 * baseDir/
 *   java-base/
 *     forge-template.yaml
 *     pom.xml.ftl
 *   spring-boot-service/
 *     forge-template.yaml
 *     ...
 * }</pre>
 */
public class LocalRegistryResolver {

    private final Path baseDir;

    /**
     * @param baseDir the root directory containing template subdirectories
     */
    public LocalRegistryResolver(Path baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Lists the IDs of all templates found in this registry.
     * <p>
     * Each subdirectory of {@code baseDir} that contains a {@code forge-template.yaml}
     * is treated as a template; its directory name is the template ID.
     *
     * @return list of template IDs (directory names), sorted alphabetically
     * @throws IOException if the base directory cannot be read
     */
    public List<String> listTemplateIds() throws IOException {
        if (!Files.isDirectory(baseDir)) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        try (Stream<Path> entries = Files.list(baseDir)) {
            entries.filter(Files::isDirectory)
                   .filter(dir -> Files.exists(dir.resolve("forge-template.yaml")))
                   .map(dir -> dir.getFileName().toString())
                   .sorted()
                   .forEach(ids::add);
        }
        return ids;
    }

    /**
     * Resolves a template directory by ID.
     *
     * @param templateId the template identifier (must match a subdirectory name)
     * @return the path to the template directory, or {@code null} if not found
     * @throws IOException if the base directory cannot be read
     */
    public Path resolve(String templateId) throws IOException {
        if (!Files.isDirectory(baseDir)) {
            return null;
        }
        Path candidate = baseDir.resolve(templateId);
        if (Files.isDirectory(candidate) && Files.exists(candidate.resolve("forge-template.yaml"))) {
            return candidate;
        }
        return null;
    }

    /**
     * Returns the base directory this resolver operates on.
     *
     * @return the base directory path
     */
    public Path getBaseDir() {
        return baseDir;
    }
}
