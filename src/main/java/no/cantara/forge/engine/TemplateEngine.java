package no.cantara.forge.engine;

import no.cantara.forge.util.CaseConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Variable substitution engine for forge templates.
 * <p>
 * Replaces {@code {{variableName}}} and {@code {{variableName | filter}}} placeholders
 * in template strings and files.
 *
 * <h3>Supported filters:</h3>
 * <ul>
 *   <li>{@code upper-case} — converts value to UPPER CASE</li>
 *   <li>{@code lower-case} — converts value to lower case</li>
 *   <li>{@code pascal-case} — converts value to PascalCase</li>
 *   <li>{@code camel-case} — converts value to camelCase</li>
 *   <li>{@code kebab-case} — converts value to kebab-case</li>
 *   <li>{@code replace('from','to')} — replaces all occurrences of {@code from} with {@code to}</li>
 * </ul>
 *
 * <h3>Behaviour:</h3>
 * <ul>
 *   <li>Unknown variables are left as-is (placeholder preserved, no exception thrown).</li>
 *   <li>Unknown filters are ignored; the raw value is used.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * Map<String, String> vars = Map.of("projectName", "my-awesome-project");
 * String result = TemplateEngine.render("package {{projectName | pascal-case}};", vars);
 * // → "package MyAwesomeProject;"
 * }</pre>
 */
public class TemplateEngine {

    /** Matches {@code {{name}}} and {@code {{name | filter}}} placeholders. */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+?)\\}\\}");

    /** Matches {@code replace('from','to')} filter syntax. */
    private static final Pattern REPLACE_FILTER = Pattern.compile(
            "replace\\(['\"](.+?)['\"],\\s*['\"](.+?)['\"]\\)");

    private TemplateEngine() {
        // static utility — not instantiable
    }

    /**
     * Renders a template string by substituting all {@code {{...}}} placeholders
     * with values from the provided map.
     *
     * @param template  the template string (may contain zero or more placeholders)
     * @param variables map of variable names to their string values
     * @return the rendered string with placeholders replaced; unknown placeholders are preserved
     */
    public static String render(String template, Map<String, String> variables) {
        if (template == null || template.isEmpty()) return template;

        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String expression = matcher.group(1).trim();
            String replacement = resolveExpression(expression, variables);
            // If null, leave the placeholder as-is
            if (replacement == null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Resolves a placeholder expression (which may include a filter) to its final value.
     *
     * @param expression the content between {{ and }}, e.g. "name | upper-case"
     * @param variables  the variable map
     * @return the resolved value, or {@code null} if the variable is not found
     */
    private static String resolveExpression(String expression, Map<String, String> variables) {
        String varName;
        String filter = null;

        int pipeIdx = expression.indexOf('|');
        if (pipeIdx >= 0) {
            varName = expression.substring(0, pipeIdx).trim();
            filter = expression.substring(pipeIdx + 1).trim();
        } else {
            varName = expression;
        }

        String value = variables.get(varName);
        if (value == null) {
            return null; // unknown variable — preserve placeholder
        }

        if (filter != null && !filter.isEmpty()) {
            value = applyFilter(value, filter);
        }
        return value;
    }

    /**
     * Applies a named filter to a value.
     *
     * @param value  the input value
     * @param filter the filter name (e.g. "upper-case", "pascal-case")
     * @return the filtered value; returns {@code value} unchanged if filter is unrecognised
     */
    static String applyFilter(String value, String filter) {
        return switch (filter.toLowerCase()) {
            case "upper-case"  -> CaseConverter.toUpperCase(value);
            case "lower-case"  -> CaseConverter.toLowerCase(value);
            case "pascal-case" -> CaseConverter.toPascalCase(value);
            case "camel-case"  -> CaseConverter.toCamelCase(value);
            case "kebab-case"  -> CaseConverter.toKebabCase(value);
            default -> {
                // Handle replace('from','to') filter
                Matcher m = REPLACE_FILTER.matcher(filter);
                if (m.matches()) {
                    yield value.replace(m.group(1), m.group(2));
                }
                yield value; // unknown filter — return as-is
            }
        };
    }

    /**
     * Renders a template file: reads source, substitutes all placeholders, then writes
     * the result to the target path. Parent directories of {@code target} are created
     * if they do not exist.
     *
     * @param source    path to the source template file
     * @param target    path to write the rendered output
     * @param variables variable map for substitution
     * @throws IOException if the source cannot be read or the target cannot be written
     */
    public static void renderFile(Path source, Path target, Map<String, String> variables) throws IOException {
        String content = Files.readString(source);
        String rendered = render(content, variables);
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        Files.writeString(target, rendered);
    }
}
