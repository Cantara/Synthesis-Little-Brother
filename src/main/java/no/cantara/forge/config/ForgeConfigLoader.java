package no.cantara.forge.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads and saves the Forge user configuration from {@code ~/.forge/config.yaml}.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * ForgeConfig config = ForgeConfigLoader.load();
 * config.getRegistries().add(new RegistryEntry(...));
 * ForgeConfigLoader.save(config);
 * }</pre>
 */
public final class ForgeConfigLoader {

    private static final Path CONFIG_FILE =
            Paths.get(System.getProperty("user.home"), ".forge", "config.yaml");

    private ForgeConfigLoader() {
        // static utility — not instantiable
    }

    /**
     * Loads the Forge configuration from {@code ~/.forge/config.yaml}.
     * Returns an empty {@link ForgeConfig} (with defaults) if the file does not exist.
     *
     * @return the loaded or default configuration
     * @throws IOException if the file exists but cannot be read
     */
    public static ForgeConfig load() throws IOException {
        if (!Files.exists(CONFIG_FILE)) {
            return new ForgeConfig();
        }
        Yaml yaml = new Yaml(new Constructor(ForgeConfig.class, new LoaderOptions()));
        try (InputStream is = Files.newInputStream(CONFIG_FILE)) {
            ForgeConfig config = yaml.load(is);
            return config != null ? config : new ForgeConfig();
        }
    }

    /**
     * Saves the given configuration to {@code ~/.forge/config.yaml}.
     * Parent directories are created if they do not exist.
     *
     * @param config the configuration to persist
     * @throws IOException if the file cannot be written
     */
    public static void save(ForgeConfig config) throws IOException {
        Files.createDirectories(CONFIG_FILE.getParent());
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        Representer representer = new Representer(opts);
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(representer, opts);
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            yaml.dump(config, writer);
        }
    }

    /**
     * Returns the Forge configuration directory: {@code ~/.forge/}.
     *
     * @return path to the configuration directory
     */
    public static Path getConfigDir() {
        return Paths.get(System.getProperty("user.home"), ".forge");
    }

    /**
     * Returns the user-local templates directory: {@code ~/.forge/templates/}.
     *
     * @return path to the user templates directory
     */
    public static Path getTemplatesDir() {
        return getConfigDir().resolve("templates");
    }

    /**
     * Returns the registry cache directory: {@code ~/.forge/registries/}.
     *
     * @return path to the registries cache directory
     */
    public static Path getRegistriesDir() {
        return getConfigDir().resolve("registries");
    }
}
