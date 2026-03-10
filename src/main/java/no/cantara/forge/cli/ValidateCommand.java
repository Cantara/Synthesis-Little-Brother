package no.cantara.forge.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * Validates a forge-template.yaml manifest for correctness.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * forge validate ./my-template/
 * forge validate /path/to/template-dir
 * }</pre>
 */
@Command(
        name = "validate",
        description = "Validate a forge-template.yaml manifest",
        mixinStandardHelpOptions = true
)
public class ValidateCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to the template directory containing forge-template.yaml")
    String path;

    @Override
    public Integer call() {
        System.out.println("Validating template at: " + path);
        System.out.println("(not yet implemented)");
        return 0;
    }
}
