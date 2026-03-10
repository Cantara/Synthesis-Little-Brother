package no.cantara.forge.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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
     * Adds a new template registry by URL.
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
            System.out.println("(not yet implemented)");
            return 0;
        }
    }

    /**
     * Lists all configured template registries.
     */
    @Command(
            name = "list",
            description = "Show configured registries",
            mixinStandardHelpOptions = true
    )
    public static class ListCommand implements Callable<Integer> {

        @Override
        public Integer call() {
            System.out.println("(not yet implemented)");
            return 0;
        }
    }
}
