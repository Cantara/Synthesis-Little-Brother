package no.cantara.forge.registry;

import no.cantara.forge.config.ForgeConfig;
import no.cantara.forge.config.ForgeConfigLoader;
import no.cantara.forge.template.TemplateManifest;
import no.cantara.forge.template.TemplateManifestLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Central orchestrator for template discovery and resolution.
 *
 * <h3>Resolution order for {@link #resolve(String)}:</h3>
 * <ol>
 *   <li>Direct filesystem path (if the argument names an existing directory)</li>
 *   <li>User-local templates: {@code ~/.forge/templates/<id>/}</li>
 *   <li>Configured registries (first match wins)</li>
 *   <li>Built-in classpath templates (bundled with the JAR)</li>
 * </ol>
 */
public class RegistryManager {

    /** Classpath prefix for templates bundled inside the JAR. */
    private static final String BUILTIN_PREFIX = "builtin-templates";

    private final ForgeConfig config;
    private final Path registriesDir;
    private final Path userTemplatesDir;

    /**
     * @param config          the loaded Forge configuration
     * @param registriesDir   {@code ~/.forge/registries/}
     * @param userTemplatesDir {@code ~/.forge/templates/}
     */
    public RegistryManager(ForgeConfig config, Path registriesDir, Path userTemplatesDir) {
        this.config = config;
        this.registriesDir = registriesDir;
        this.userTemplatesDir = userTemplatesDir;
    }

    /**
     * Summary of a template available for use.
     *
     * @param id           template identifier (e.g. {@code java-base})
     * @param name         human-readable name
     * @param version      semantic version string
     * @param description  short description
     * @param tags         list of tags
     * @param registryName name of the registry this template came from
     */
    public record AvailableTemplate(
            String id,
            String name,
            String version,
            String description,
            List<String> tags,
            String registryName
    ) {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Lists all templates available across every source: built-in, user-local,
     * and all configured registries (which are refreshed if stale).
     *
     * @return list of available templates, ordered: built-in first, then user-local,
     *         then registry order
     * @throws Exception if a Git registry cannot be cloned for the first time
     */
    public List<AvailableTemplate> listAll() throws Exception {
        List<AvailableTemplate> result = new ArrayList<>();

        // 1. Built-in templates (classpath)
        result.addAll(listBuiltins());

        // 2. User-local templates (~/.forge/templates/)
        if (Files.isDirectory(userTemplatesDir)) {
            LocalRegistryResolver local = new LocalRegistryResolver(userTemplatesDir);
            for (String id : local.listTemplateIds()) {
                Path dir = local.resolve(id);
                result.add(toAvailableTemplate(id, dir, "local"));
            }
        }

        // 3. Configured registries
        for (RegistryEntry entry : config.getRegistries()) {
            String registryName = entry.getName() != null ? entry.getName() : deriveRegistryName(entry.getUrl());
            Path templatesDir;

            if (entry.isLocal()) {
                Path base = Path.of(entry.getUrl()).resolve(entry.getPath() != null ? entry.getPath() : "templates");
                LocalRegistryResolver resolver = new LocalRegistryResolver(base);
                for (String id : resolver.listTemplateIds()) {
                    Path dir = resolver.resolve(id);
                    result.add(toAvailableTemplate(id, dir, registryName));
                }
            } else {
                Path cacheDir = registriesDir.resolve(registryName);
                GitRegistryResolver git = new GitRegistryResolver(entry, cacheDir);
                try {
                    git.ensureFresh();
                } catch (Exception e) {
                    System.err.println("Warning: could not fetch registry '" + registryName + "': " + e.getMessage());
                    continue;
                }
                LocalRegistryResolver resolver = new LocalRegistryResolver(git.getTemplatesDir());
                for (String id : resolver.listTemplateIds()) {
                    Path dir = resolver.resolve(id);
                    result.add(toAvailableTemplate(id, dir, registryName));
                }
            }
        }

        return result;
    }

    /**
     * Resolves a template to its directory path.
     * <p>
     * Resolution order: direct path → user-local → configured registries → built-in.
     *
     * @param templateIdOrPath template ID or a filesystem path
     * @return the resolved template directory, or {@code null} if not found
     * @throws Exception if a Git registry operation fails
     */
    public Path resolve(String templateIdOrPath) throws Exception {
        // 1. Direct filesystem path
        Path direct = Path.of(templateIdOrPath);
        if (Files.isDirectory(direct) && Files.exists(direct.resolve("forge-template.yaml"))) {
            return direct;
        }

        // 2. User-local templates
        if (Files.isDirectory(userTemplatesDir)) {
            LocalRegistryResolver local = new LocalRegistryResolver(userTemplatesDir);
            Path found = local.resolve(templateIdOrPath);
            if (found != null) return found;
        }

        // 3. Configured registries
        for (RegistryEntry entry : config.getRegistries()) {
            String registryName = entry.getName() != null ? entry.getName() : deriveRegistryName(entry.getUrl());

            if (entry.isLocal()) {
                Path base = Path.of(entry.getUrl()).resolve(entry.getPath() != null ? entry.getPath() : "templates");
                Path found = new LocalRegistryResolver(base).resolve(templateIdOrPath);
                if (found != null) return found;
            } else {
                Path cacheDir = registriesDir.resolve(registryName);
                GitRegistryResolver git = new GitRegistryResolver(entry, cacheDir);
                try {
                    git.ensureFresh();
                } catch (Exception e) {
                    System.err.println("Warning: could not fetch registry '" + registryName + "': " + e.getMessage());
                    continue;
                }
                Path found = new LocalRegistryResolver(git.getTemplatesDir()).resolve(templateIdOrPath);
                if (found != null) return found;
            }
        }

        // 4. Built-in classpath templates
        Path builtin = resolveBuiltin(templateIdOrPath);
        if (builtin != null) return builtin;

        return null;
    }

    /**
     * Adds a new registry entry to the configuration and persists it.
     *
     * @param url  the Git URL or local path of the registry
     * @param name the local alias for the registry (derived from URL if null)
     * @throws IOException if the configuration cannot be saved
     */
    public void addRegistry(String url, String name) throws IOException {
        String resolvedName = (name != null && !name.isBlank())
                ? name
                : deriveRegistryName(url);

        RegistryEntry entry = new RegistryEntry(resolvedName, url);
        config.getRegistries().add(entry);
        ForgeConfigLoader.save(config);
    }

    // -------------------------------------------------------------------------
    // Built-in template helpers
    // -------------------------------------------------------------------------

    private List<AvailableTemplate> listBuiltins() {
        List<AvailableTemplate> result = new ArrayList<>();
        // We know the only built-in is java-base; enumerate them from the classpath
        String[] builtinIds = {"java-base"};
        for (String id : builtinIds) {
            try {
                Path dir = resolveBuiltin(id);
                if (dir != null) {
                    result.add(toAvailableTemplate(id, dir, "builtin"));
                }
            } catch (Exception e) {
                // skip if unreadable
            }
        }
        return result;
    }

    /**
     * Resolves a built-in template from the classpath. Handles both exploded
     * directories (during tests) and JAR entries.
     */
    private Path resolveBuiltin(String templateId) {
        String resourcePath = BUILTIN_PREFIX + "/" + templateId;
        URL url = getClass().getClassLoader().getResource(resourcePath);
        if (url == null) return null;

        try {
            URI uri = url.toURI();
            if ("jar".equals(uri.getScheme())) {
                // Inside a JAR: open a ZipFileSystem and extract to a temp directory
                return extractBuiltinToTemp(uri, resourcePath, templateId);
            } else {
                // Exploded (test classpath or development)
                Path dir = Path.of(uri);
                if (Files.isDirectory(dir) && Files.exists(dir.resolve("forge-template.yaml"))) {
                    return dir;
                }
            }
        } catch (URISyntaxException | IOException e) {
            // fall through
        }
        return null;
    }

    /**
     * Extracts a built-in template from the JAR to a temporary directory so it
     * can be used as a normal filesystem path by {@link no.cantara.forge.generate.ProjectGenerator}.
     */
    private Path extractBuiltinToTemp(URI jarUri, String resourcePath, String templateId) throws IOException {
        // jarUri looks like: jar:file:/path/to/forge.jar!/builtin-templates/java-base
        // We need the outer JAR path for FileSystems.newFileSystem
        String jarPath = jarUri.toString();
        int bangIdx = jarPath.indexOf("!/");
        if (bangIdx < 0) return null;
        URI jarFileUri = URI.create(jarPath.substring(0, bangIdx));  // e.g. jar:file:/path/forge.jar

        Path tempDir = Files.createTempDirectory("forge-builtin-" + templateId + "-");
        tempDir.toFile().deleteOnExit();

        try (FileSystem fs = FileSystems.newFileSystem(jarFileUri, Map.of())) {
            Path inJar = fs.getPath("/" + resourcePath);
            if (!Files.exists(inJar)) return null;
            // Copy all files recursively
            try (var walk = Files.walk(inJar)) {
                walk.forEach(src -> {
                    try {
                        Path relative = inJar.relativize(src);
                        Path dest = tempDir.resolve(relative.toString());
                        if (Files.isDirectory(src)) {
                            Files.createDirectories(dest);
                        } else {
                            Files.createDirectories(dest.getParent());
                            try (InputStream in = Files.newInputStream(src)) {
                                Files.copy(in, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
        }
        return tempDir;
    }

    // -------------------------------------------------------------------------
    // Utility helpers
    // -------------------------------------------------------------------------

    private AvailableTemplate toAvailableTemplate(String id, Path dir, String registryName) {
        if (dir == null) return new AvailableTemplate(id, id, "?", "", List.of(), registryName);
        try {
            TemplateManifest manifest = TemplateManifestLoader.load(dir);
            TemplateManifest.TemplateInfo info = manifest.getTemplate();
            if (info == null) {
                return new AvailableTemplate(id, id, "?", "", List.of(), registryName);
            }
            return new AvailableTemplate(
                    info.getId() != null ? info.getId() : id,
                    info.getName() != null ? info.getName() : id,
                    info.getVersion() != null ? info.getVersion() : "?",
                    info.getDescription() != null ? info.getDescription() : "",
                    info.getTags() != null ? info.getTags() : List.of(),
                    registryName
            );
        } catch (Exception e) {
            return new AvailableTemplate(id, id, "?", "", List.of(), registryName);
        }
    }

    /**
     * Derives a short registry name from a URL, e.g.
     * {@code https://github.com/cantara/forge-templates.git} → {@code forge-templates}.
     */
    public static String deriveRegistryName(String url) {
        if (url == null || url.isBlank()) return "registry";
        String name = url;
        // Strip trailing .git
        if (name.endsWith(".git")) name = name.substring(0, name.length() - 4);
        // Take the last path segment
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf(':'));
        if (slash >= 0 && slash < name.length() - 1) {
            name = name.substring(slash + 1);
        }
        return name.isBlank() ? "registry" : name;
    }
}
