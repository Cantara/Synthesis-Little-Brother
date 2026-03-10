package no.cantara.forge.template;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and parses a {@code forge-template.yaml} manifest from a template directory.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * TemplateManifest manifest = TemplateManifestLoader.load(Path.of("./my-template"));
 * System.out.println(manifest.getTemplate().getId());
 * }</pre>
 */
public class TemplateManifestLoader {

    private TemplateManifestLoader() {
        // static utility — not instantiable
    }

    /**
     * Loads and parses the {@code forge-template.yaml} file from the given template directory.
     *
     * @param templateDir path to the directory containing {@code forge-template.yaml}
     * @return the parsed {@link TemplateManifest}
     * @throws IllegalArgumentException if no {@code forge-template.yaml} is found in the directory
     * @throws IOException              if the file cannot be read
     */
    public static TemplateManifest load(Path templateDir) throws IOException {
        Path manifestFile = templateDir.resolve("forge-template.yaml");
        if (!Files.exists(manifestFile)) {
            throw new IllegalArgumentException("No forge-template.yaml found in: " + templateDir);
        }
        Yaml yaml = new Yaml(new Constructor(TemplateManifest.class, new LoaderOptions()));
        try (InputStream is = Files.newInputStream(manifestFile)) {
            return yaml.load(is);
        }
    }
}
