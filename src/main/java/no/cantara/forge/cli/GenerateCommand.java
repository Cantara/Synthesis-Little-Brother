package no.cantara.forge.cli;

import no.cantara.forge.config.ForgeConfig;
import no.cantara.forge.config.ForgeConfigLoader;
import no.cantara.forge.engine.VariableCollector;
import no.cantara.forge.generate.HookRunner;
import no.cantara.forge.generate.ProjectGenerator;
import no.cantara.forge.registry.RegistryManager;
import no.cantara.forge.template.TemplateManifest;
import no.cantara.forge.template.TemplateManifestLoader;
import no.cantara.forge.util.AnsiOutput;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.io.PrintWriter;
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
 * forge generate java-base
 * forge generate cantara/java-service --output-dir ./my-project
 * forge generate cantara/java-service --var groupId=no.cantara --var artifactId=my-service
 * forge generate cantara/java-service --vars-file vars.yaml --dry-run
 * }</pre>
 *
 * <h3>Template resolution order:</h3>
 * <ol>
 *   <li>Direct filesystem path</li>
 *   <li>{@code ~/.forge/templates/<id>/} — user-local templates</li>
 *   <li>Configured registries (first match wins)</li>
 *   <li>Built-in classpath templates bundled with the JAR</li>
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
            // 1. Resolve template directory via RegistryManager
            AnsiOutput.printHeader("Generating project from template: " + templateId);
            System.out.println();

            ForgeConfig config = ForgeConfigLoader.load();
            RegistryManager manager = new RegistryManager(
                    config,
                    ForgeConfigLoader.getRegistriesDir(),
                    ForgeConfigLoader.getTemplatesDir()
            );

            Path templateDir = manager.resolve(templateId);
            if (templateDir == null) {
                AnsiOutput.printError("Template not found: " + templateId);
                System.err.println("  Try: forge list  — to see available templates");
                System.err.println("  Try: forge registry add <url>  — to add a template registry");
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

            // 7. Run post-generate hooks (only on real generation, not dry-run)
            if (!dryRun) {
                HookRunner.runAll(manifest, result.outputDir());
            }

            // 8. Print summary
            System.out.println();
            if (dryRun) {
                AnsiOutput.printSuccess("Dry-run complete. Would create "
                        + result.filesCreated() + " file(s) in: " + result.outputDir());
            } else {
                AnsiOutput.printSuccess("Project created: " + result.outputDir().toAbsolutePath());
                System.out.println();
                System.out.println(AnsiOutput.bold("Next steps:"));
                System.out.println("  cd " + result.outputDir().toAbsolutePath());
                System.out.println("  mvn test");
            }

            if (!result.skippedFiles().isEmpty()) {
                System.out.println();
                AnsiOutput.printDim("Skipped " + result.skippedFiles().size() + " file(s):");
                result.skippedFiles().forEach(f -> AnsiOutput.printDim("  - " + f));
            }

            return 0;

        } catch (IllegalStateException | IllegalArgumentException e) {
            AnsiOutput.printError(e.getMessage());
            return 1;
        } catch (IOException e) {
            AnsiOutput.printError("I/O error: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            AnsiOutput.printError("Unexpected error: " + e.getMessage());
            return 1;
        }
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
