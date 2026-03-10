package no.cantara.forge.cli;

import no.cantara.forge.config.ForgeConfig;
import no.cantara.forge.config.ForgeConfigLoader;
import no.cantara.forge.registry.RegistryEntry;
import no.cantara.forge.registry.RegistryManager;
import no.cantara.forge.util.AnsiOutput;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Manages template registries — add, remove, and list configured sources.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * forge registry add https://github.com/cantara/forge-templates
 * forge registry add https://github.com/cantara/forge-templates --name cantara
 * forge registry list
 * }</pre>
 */
@Command(
        name = "registry",
        description = "Manage template registries",
        mixinStandardHelpOptions = true,
        subcommands = {
                RegistryCommand.AddCommand.class,
                RegistryCommand.ListCommand.class
        }
)
public class RegistryCommand implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    /**
     * Adds a new template registry by URL and saves it to {@code ~/.forge/config.yaml}.
     */
    @Command(
            name = "add",
            description = "Add a template registry",
            mixinStandardHelpOptions = true
    )
    public static class AddCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "URL of the template registry to add")
        String url;

        @Option(names = "--name",
                description = "Local alias for the registry (default: derived from URL)")
        String name;

        @Override
        public Integer call() {
            try {
                ForgeConfig config = ForgeConfigLoader.load();
                RegistryManager manager = new RegistryManager(
                        config,
                        ForgeConfigLoader.getRegistriesDir(),
                        ForgeConfigLoader.getTemplatesDir()
                );

                // Derive name if not provided
                String resolvedName = (name != null && !name.isBlank())
                        ? name
                        : RegistryManager.deriveRegistryName(url);

                // Check for duplicates
                boolean exists = config.getRegistries().stream()
                        .anyMatch(r -> url.equals(r.getUrl()) || resolvedName.equals(r.getName()));
                if (exists) {
                    AnsiOutput.printWarning("Registry '" + resolvedName + "' is already configured.");
                    return 0;
                }

                manager.addRegistry(url, resolvedName);
                AnsiOutput.printSuccess("Added registry '" + resolvedName + "' \u2192 " + url);
                AnsiOutput.printDim("  Config saved to: " + ForgeConfigLoader.getConfigDir().resolve("config.yaml"));
                return 0;

            } catch (Exception e) {
                AnsiOutput.printError("Failed to add registry: " + e.getMessage());
                return 1;
            }
        }
    }

    /**
     * Lists all configured template registries from {@code ~/.forge/config.yaml}.
     */
    @Command(
            name = "list",
            description = "Show configured registries",
            mixinStandardHelpOptions = true
    )
    public static class ListCommand implements Callable<Integer> {

        @Override
        public Integer call() {
            try {
                ForgeConfig config = ForgeConfigLoader.load();
                List<RegistryEntry> registries = config.getRegistries();

                if (registries == null || registries.isEmpty()) {
                    System.out.println("No registries configured.");
                    System.out.println("Add one with: forge registry add <url>");
                    return 0;
                }

                AnsiOutput.printHeader("Configured registries:");
                System.out.println();

                int nameW = Math.max(4, registries.stream()
                        .mapToInt(r -> r.getName() != null ? r.getName().length() : 4).max().orElse(4));
                int branchW = Math.max(6, registries.stream()
                        .mapToInt(r -> r.getBranch() != null ? r.getBranch().length() : 4).max().orElse(6));

                String fmt = "  %-" + nameW + "s  %-" + branchW + "s  %s%n";
                System.out.printf(fmt, "NAME", "BRANCH", "URL");
                System.out.printf(fmt, "-".repeat(nameW), "-".repeat(branchW), "---");

                for (RegistryEntry r : registries) {
                    System.out.printf(fmt,
                            r.getName() != null ? r.getName() : "-",
                            r.getBranch() != null ? r.getBranch() : "main",
                            r.getUrl() != null ? r.getUrl() : "-");
                }

                System.out.println();
                return 0;

            } catch (Exception e) {
                AnsiOutput.printError("Failed to list registries: " + e.getMessage());
                return 1;
            }
        }
    }
}
