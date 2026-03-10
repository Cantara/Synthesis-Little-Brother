package no.cantara.forge.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Scaffolds a new project from a named template manifest.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * forge generate cantara/java-service
 * forge generate cantara/java-service --output-dir ./my-project
 * forge generate cantara/java-service --var groupId=no.cantara --var artifactId=my-service
 * forge generate cantara/java-service --vars-file vars.yaml --dry-run
 * }</pre>
 */
@Command(
        name = "generate",
        description = "Scaffold a new project from a template manifest",
        mixinStandardHelpOptions = true
)
public class GenerateCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Template ID to generate from (e.g. cantara/java-service)")
    String templateId;

    @Option(names = {"-o", "--output-dir"},
            description = "Output directory (default: ./<artifactId>)")
    String outputDir;

    @Option(names = "--var",
            description = "Variable override in key=value format (repeatable)",
            mapFallbackValue = "")
    Map<String, String> vars = new LinkedHashMap<>();

    @Option(names = "--vars-file",
            description = "Path to a YAML file containing variable overrides")
    String varsFile;

    @Option(names = "--dry-run",
            description = "Print the file tree that would be generated without writing anything",
            defaultValue = "false")
    boolean dryRun;

    @Option(names = {"-y", "--no-interactive"},
            description = "Skip interactive prompts and use default values",
            defaultValue = "false")
    boolean noInteractive;

    @Override
    public Integer call() {
        System.out.println("Generating from template: " + templateId);
        System.out.println("(not yet implemented)");
        return 0;
    }
}
