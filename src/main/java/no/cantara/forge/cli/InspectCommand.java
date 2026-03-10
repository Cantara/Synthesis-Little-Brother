package no.cantara.forge.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * Shows template details, variables, and a file structure preview.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * forge inspect cantara/java-service
 * }</pre>
 */
@Command(
        name = "inspect",
        description = "Show template details, variables, and file structure preview",
        mixinStandardHelpOptions = true
)
public class InspectCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Template ID to inspect (e.g. cantara/java-service)")
    String templateId;

    @Override
    public Integer call() {
        System.out.println("Inspecting template: " + templateId);
        System.out.println("(not yet implemented)");
        return 0;
    }
}
