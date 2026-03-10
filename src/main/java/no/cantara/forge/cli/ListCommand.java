package no.cantara.forge.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * Lists available templates from all configured registries.
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
        System.out.println("(no templates found — add a registry with: forge registry add <url>)");
        return 0;
    }
}
