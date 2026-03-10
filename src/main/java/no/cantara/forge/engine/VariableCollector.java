package no.cantara.forge.engine;

import no.cantara.forge.template.TemplateVariable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Collects variable values in priority order:
 * CLI --var flags > --vars-file > interactive prompt > default value.
 *
 * <p>If a required variable has no value from any source (and interactive prompting
 * is disabled or yields no input), an {@link IllegalStateException} is thrown.
 */
public class VariableCollector {

    private final List<TemplateVariable> variables;
    private final Map<String, String> cliVars;
    private final Path varsFile;
    private final boolean noInteractive;
    private final PrintWriter out;

    /**
     * @param variables     list of variable definitions from the template manifest
     * @param cliVars       variable overrides supplied via --var flags (key=value)
     * @param varsFile      path to a YAML vars file, or {@code null} if not provided
     * @param noInteractive when {@code true}, skip interactive prompts and use defaults
     * @param out           writer for prompt output (typically wraps System.out)
     */
    public VariableCollector(List<TemplateVariable> variables,
                             Map<String, String> cliVars,
                             Path varsFile,
                             boolean noInteractive,
                             PrintWriter out) {
        this.variables = variables != null ? variables : List.of();
        this.cliVars = cliVars != null ? cliVars : Map.of();
        this.varsFile = varsFile;
        this.noInteractive = noInteractive;
        this.out = out;
    }

    /**
     * Collects all variable values following the priority order:
     * CLI flags > vars-file > interactive prompt > default value.
     *
     * @return resolved map of variable name to value
     * @throws IOException           if the vars file cannot be read
     * @throws IllegalStateException if a required variable has no value after all sources
     */
    public Map<String, String> collect() throws IOException {
        // 1. Load vars file if provided
        Map<String, String> fileVars = new LinkedHashMap<>();
        if (varsFile != null) {
            fileVars = loadVarsFile(varsFile);
        }

        Map<String, String> resolved = new LinkedHashMap<>();
        Scanner scanner = noInteractive ? null : new Scanner(System.in);

        // 2. For each variable: cli > file > prompt > default
        for (TemplateVariable var : variables) {
            String name = var.getName();
            String value = null;

            // Priority 1: CLI --var flag
            if (cliVars.containsKey(name)) {
                value = cliVars.get(name);
            }
            // Priority 2: vars-file
            else if (fileVars.containsKey(name)) {
                value = fileVars.get(name);
            }
            // Priority 3: interactive prompt (if allowed)
            else if (!noInteractive && scanner != null) {
                value = promptUser(var, scanner);
                if (value != null && value.isBlank()) {
                    value = null; // treat blank as no-input, fall through to default
                }
            }

            // Priority 4: default value
            if (value == null || value.isBlank()) {
                value = var.getDefaultValue();
            }

            // 3. Throw if required variable has no value
            if ((value == null || value.isBlank()) && var.isRequired()) {
                throw new IllegalStateException(
                        "Required variable '" + name + "' has no value. "
                        + "Provide it with --var " + name + "=<value> or in a vars file.");
            }

            if (value != null) {
                resolved.put(name, value);
            }
        }

        return resolved;
    }

    /**
     * Prompts the user for a variable value on the given writer/scanner pair.
     * Displays the prompt text and, if a default exists, shows it in brackets.
     * Returns the default if the user enters an empty line.
     *
     * @param var     the variable to prompt for
     * @param scanner the scanner reading user input
     * @return the user-supplied value, or the default if the user pressed Enter
     */
    private String promptUser(TemplateVariable var, Scanner scanner) {
        String promptText = var.getPrompt() != null ? var.getPrompt() : var.getName();
        String defaultValue = var.getDefaultValue();

        if (defaultValue != null && !defaultValue.isBlank()) {
            out.print("  " + promptText + " [" + defaultValue + "]: ");
        } else {
            out.print("  " + promptText + ": ");
        }
        out.flush();

        String line = scanner.hasNextLine() ? scanner.nextLine() : "";
        if (line == null || line.isBlank()) {
            return defaultValue;
        }
        return line.trim();
    }

    /**
     * Loads key-value pairs from a YAML file. The YAML file must be a flat
     * mapping at the top level. All values are converted to {@code String}.
     *
     * @param path path to the YAML vars file
     * @return map of variable names to string values
     * @throws IOException if the file cannot be read
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> loadVarsFile(Path path) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        Yaml yaml = new Yaml();
        try (InputStream is = Files.newInputStream(path)) {
            Object raw = yaml.load(is);
            if (raw instanceof Map<?, ?> rawMap) {
                Map<String, Object> typedMap = (Map<String, Object>) rawMap;
                for (Map.Entry<String, Object> entry : typedMap.entrySet()) {
                    if (entry.getValue() != null) {
                        result.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
            }
        }
        return result;
    }
}
