package no.cantara.forge.cli;

import no.cantara.forge.template.TemplateFile;
import no.cantara.forge.template.TemplateManifest;
import no.cantara.forge.template.TemplateManifestLoader;
import no.cantara.forge.template.TemplateVariable;
import no.cantara.forge.util.AnsiOutput;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates a forge-template.yaml manifest for correctness.
 *
 * <h3>Checks performed:</h3>
 * <ul>
 *   <li>Required manifest fields: {@code template.id}, {@code template.name}, {@code template.version}</li>
 *   <li>All variables have a non-empty {@code name}</li>
 *   <li>All {@code files.source} paths exist on disk in the template directory</li>
 *   <li>Variables referenced in {@code files.target} paths exist in the variable definitions</li>
 * </ul>
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

    /** Matches {{variableName}} and {{variableName | filter}} placeholders. */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}|]+?)(?:\\s*\\|[^}]*)?\\}\\}");

    @Parameters(index = "0", description = "Path to the template directory containing forge-template.yaml")
    String path;

    @Override
    public Integer call() {
        Path templateDir = Path.of(path);
        List<String> errors = new ArrayList<>();

        // Check directory exists
        if (!Files.isDirectory(templateDir)) {
            AnsiOutput.printError("Not a directory: " + path);
            return 1;
        }

        // Load manifest
        TemplateManifest manifest;
        try {
            manifest = TemplateManifestLoader.load(templateDir);
        } catch (IllegalArgumentException e) {
            AnsiOutput.printError("No forge-template.yaml found in: " + path);
            return 1;
        } catch (Exception e) {
            AnsiOutput.printError("Failed to parse forge-template.yaml: " + e.getMessage());
            return 1;
        }

        // Check required template fields
        TemplateManifest.TemplateInfo info = manifest.getTemplate();
        if (info == null) {
            errors.add("Missing required block: template:");
        } else {
            if (info.getId() == null || info.getId().isBlank()) {
                errors.add("Missing required field: template.id");
            }
            if (info.getName() == null || info.getName().isBlank()) {
                errors.add("Missing required field: template.name");
            }
            if (info.getVersion() == null || info.getVersion().isBlank()) {
                errors.add("Missing required field: template.version");
            }
        }

        // Build set of known variable names
        Set<String> knownVarNames = new HashSet<>();
        List<TemplateVariable> variables = manifest.getVariables();
        if (variables != null) {
            for (int i = 0; i < variables.size(); i++) {
                TemplateVariable v = variables.get(i);
                if (v.getName() == null || v.getName().isBlank()) {
                    errors.add("Variable at index " + i + " is missing a name");
                } else {
                    knownVarNames.add(v.getName());
                }
            }
        }

        // Check files block
        List<TemplateFile> files = manifest.getFiles();
        if (files != null) {
            for (TemplateFile f : files) {
                // Check source file exists
                if (f.getSource() != null && !f.getSource().isBlank()) {
                    Path sourceFile = templateDir.resolve(f.getSource());
                    if (!Files.exists(sourceFile)) {
                        errors.add("File source not found: " + f.getSource());
                    }
                }

                // Check variables referenced in target path are defined
                String target = f.getTarget() != null ? f.getTarget() : f.getSource();
                if (target != null) {
                    Matcher m = PLACEHOLDER.matcher(target);
                    while (m.find()) {
                        String varName = m.group(1).trim();
                        if (!knownVarNames.contains(varName)) {
                            errors.add("Target path '" + target + "' references undefined variable: " + varName);
                        }
                    }
                }
            }
        }

        // Report results
        if (errors.isEmpty()) {
            String id = (info != null && info.getId() != null) ? info.getId() : path;
            String version = (info != null && info.getVersion() != null) ? info.getVersion() : "?";
            AnsiOutput.printSuccess("Valid template: " + id + " v" + version);
            return 0;
        } else {
            AnsiOutput.printError("Template validation failed: " + errors.size() + " error(s)");
            for (String error : errors) {
                System.err.println("  - " + error);
            }
            return 1;
        }
    }
}
