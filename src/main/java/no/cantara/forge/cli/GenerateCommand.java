package no.cantara.forge.cli;

import no.cantara.forge.engine.VariableCollector;
import no.cantara.forge.generate.ProjectGenerator;
import no.cantara.forge.template.TemplateManifest;
import no.cantara.forge.template.TemplateManifestLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
 *
 * <h3>Template resolution order:</h3>
 * <ol>
 *   <li>If the template ID is an existing filesystem path, use it directly.</li>
 *   <li>{@code ~/.forge/templates/<templateId>/} — user-local templates.</li>
 *   <li>If neither exists, print an error and exit with code 1.</li>
 * </ol>
 */
@Command(
        name = "generate",
        description = "Scaffold a new project from a template manifest",
        mixinStandardHelpOptions = true
)
public class GenerateCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Template ID or path to template directory")
    String templateId;

    @Option(names = {"-o", "--output-dir"},
            description = "Output directory (default: ./<templateId>)")
    Path outputDir;

    @Option(names = "--var",
            description = "Variable override in key=value format (repeatable)",
            arity = "1")
    List<String> varOverrides = new ArrayList<>();

    @Option(names = "--vars-file",
            description = "Path to a YAML file containing variable overrides")
    Path varsFile;

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
        try {
            // 1. Resolve template directory
            Path templateDir = resolveTemplateDir(templateId);
            if (templateDir == null) {
                System.err.println("Template not found: " + templateId);
                return 1;
            }

            // 2. Parse --var flags into Map<String, String>
            Map<String, String> cliVars = parseVarOverrides(varOverrides);

            // 3. Load manifest to get variable definitions
            TemplateManifest manifest = TemplateManifestLoader.load(templateDir);

            // 4. Resolve output directory
            Path resolvedOutput = outputDir != null
                    ? outputDir
                    : Path.of(".").resolve(templateId.contains("/")
                            ? templateId.substring(templateId.lastIndexOf('/') + 1)
                            : templateId);

            // 5. Collect variables
            PrintWriter out = new PrintWriter(System.out, true);
            VariableCollector collector = new VariableCollector(
                    manifest.getVariables(),
                    cliVars,
                    varsFile,
                    noInteractive,
                    out
            );
            Map<String, String> resolvedVars = collector.collect();

            // 6. Generate
            ProjectGenerator generator = new ProjectGenerator();
            ProjectGenerator.GenerateResult result = generator.generate(
                    templateDir, resolvedOutput, resolvedVars, dryRun);

            // 7. Print summary
            if (dryRun) {
                System.out.println("Dry-run complete. Would create "
                        + result.filesCreated() + " file(s) in: " + result.outputDir());
            } else {
                System.out.println("Generated " + result.filesCreated()
                        + " file(s) in: " + result.outputDir());
            }
            if (!result.skippedFiles().isEmpty()) {
                System.out.println("Skipped " + result.skippedFiles().size() + " file(s):");
                result.skippedFiles().forEach(f -> System.out.println("  - " + f));
            }
            return 0;

        } catch (IllegalStateException | IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Resolves the template directory from a template ID or path.
     *
     * @param id the template ID (e.g. "java-base" or "cantara/java-service") or a filesystem path
     * @return the resolved directory, or {@code null} if not found
     */
    private Path resolveTemplateDir(String id) {
        // 1. Check if it is a direct path to an existing directory
        Path direct = Path.of(id);
        if (Files.isDirectory(direct)) {
            return direct;
        }

        // 2. Check ~/.forge/templates/<id>/
        Path userLocal = Path.of(System.getProperty("user.home"))
                .resolve(".forge")
                .resolve("templates")
                .resolve(id);
        if (Files.isDirectory(userLocal)) {
            return userLocal;
        }

        return null;
    }

    /**
     * Parses a list of {@code "key=value"} strings into a map.
     * Entries that do not contain {@code =} are ignored.
     *
     * @param overrides list of {@code "key=value"} strings from --var flags
     * @return parsed map
     */
    private Map<String, String> parseVarOverrides(List<String> overrides) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String override : overrides) {
            int eq = override.indexOf('=');
            if (eq > 0) {
                String key = override.substring(0, eq).trim();
                String value = override.substring(eq + 1);
                result.put(key, value);
            }
        }
        return result;
    }
}
