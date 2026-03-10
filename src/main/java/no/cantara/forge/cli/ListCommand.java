package no.cantara.forge.cli;

import no.cantara.forge.config.ForgeConfig;
import no.cantara.forge.config.ForgeConfigLoader;
import no.cantara.forge.registry.RegistryManager;
import no.cantara.forge.util.AnsiOutput;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Lists available templates from all configured registries and built-ins.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * forge list
 * forge list --registry cantara
 * forge list --tag microservice
 * }</pre>
 */
@Command(
        name = "list",
        description = "List available templates from configured registries",
        mixinStandardHelpOptions = true
)
public class ListCommand implements Callable<Integer> {

    @Option(names = "--registry",
            description = "Filter results by registry name")
    String registry;

    @Option(names = "--tag",
            description = "Filter results by tag")
    String tag;

    @Override
    public Integer call() {
        try {
            ForgeConfig config = ForgeConfigLoader.load();
            RegistryManager manager = new RegistryManager(
                    config,
                    ForgeConfigLoader.getRegistriesDir(),
                    ForgeConfigLoader.getTemplatesDir()
            );

            List<RegistryManager.AvailableTemplate> templates = manager.listAll();

            // Apply --registry filter
            if (registry != null && !registry.isBlank()) {
                templates = templates.stream()
                        .filter(t -> registry.equalsIgnoreCase(t.registryName()))
                        .toList();
            }

            // Apply --tag filter
            if (tag != null && !tag.isBlank()) {
                final String filterTag = tag.toLowerCase();
                templates = templates.stream()
                        .filter(t -> t.tags() != null &&
                                t.tags().stream().anyMatch(tg -> tg.toLowerCase().contains(filterTag)))
                        .toList();
            }

            if (templates.isEmpty()) {
                System.out.println("No templates found. Add a registry with: forge registry add <url>");
                return 0;
            }

            // Print header
            AnsiOutput.printHeader("Available templates:");
            System.out.println();

            // Column widths
            int idWidth      = Math.max(12, templates.stream().mapToInt(t -> t.id().length()).max().orElse(12));
            int verWidth     = Math.max(7, templates.stream().mapToInt(t -> t.version().length()).max().orElse(7));
            int regWidth     = Math.max(8, templates.stream().mapToInt(t -> t.registryName().length()).max().orElse(8));

            String fmt = "  %-" + idWidth + "s  %-" + verWidth + "s  %-" + regWidth + "s  %s%n";
            System.out.printf(fmt, "TEMPLATE", "VERSION", "REGISTRY", "DESCRIPTION");
            System.out.printf(fmt,
                    "-".repeat(idWidth),
                    "-".repeat(verWidth),
                    "-".repeat(regWidth),
                    "-----------");

            for (RegistryManager.AvailableTemplate t : templates) {
                System.out.printf(fmt, t.id(), t.version(), t.registryName(),
                        t.description() != null ? t.description() : "");
            }

            System.out.println();
            System.out.println(AnsiOutput.dim("Total: " + templates.size() + " template(s)"));
            return 0;

        } catch (Exception e) {
            AnsiOutput.printError("Failed to list templates: " + e.getMessage());
            return 1;
        }
    }
}
