package no.cantara.forge.generate;

import no.cantara.forge.engine.TemplateEngine;
import no.cantara.forge.template.TemplateFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * Handles rendering individual files from a template directory to an output directory.
 *
 * <ul>
 *   <li>Files ending with {@code .ftl} are rendered via {@link TemplateEngine} and the
 *       {@code .ftl} suffix is stripped from the output filename.</li>
 *   <li>All other files are copied as-is.</li>
 *   <li>Variable placeholders ({@code {{varName}}}) in the <em>target path</em> itself are
 *       substituted before the output file is written.</li>
 *   <li>If a {@code condition} is set on the entry and evaluates to {@code false}, the file
 *       is skipped entirely.</li>
 * </ul>
 */
public class FileRenderer {

    private FileRenderer() {
        // static utility — not instantiable
    }

    /**
     * Renders a single template file entry to the output directory.
     *
     * @param fileEntry   the manifest entry describing source, target, and optional condition
     * @param templateDir the root directory of the template
     * @param outputDir   the root directory where output is written
     * @param vars        resolved variable map for substitution
     * @throws IOException if a file cannot be read or written
     */
    public static void render(TemplateFile fileEntry,
                              Path templateDir,
                              Path outputDir,
                              Map<String, String> vars) throws IOException {

        // Evaluate condition — skip if condition is false
        String condition = fileEntry.getCondition();
        if (condition != null && !condition.isBlank()) {
            if (!evaluateCondition(condition, vars)) {
                return;
            }
        }

        String sourcePath = fileEntry.getSource();
        String targetPath = fileEntry.getTarget() != null ? fileEntry.getTarget() : sourcePath;

        // Resolve {{variables}} in the target path itself
        targetPath = TemplateEngine.render(targetPath, vars);

        // Strip .ftl suffix from target name if source is an FTL template
        boolean isFtl = sourcePath.endsWith(".ftl");
        if (isFtl && targetPath.endsWith(".ftl")) {
            targetPath = targetPath.substring(0, targetPath.length() - 4);
        }

        Path sourceFile = templateDir.resolve(sourcePath);
        Path targetFile = outputDir.resolve(targetPath);

        // Ensure parent directories exist
        if (targetFile.getParent() != null) {
            Files.createDirectories(targetFile.getParent());
        }

        if (isFtl) {
            TemplateEngine.renderFile(sourceFile, targetFile, vars);
        } else {
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Evaluates a condition expression such as {@code "{{includeDocker}}"}.
     *
     * <p>The variable referenced in the expression is looked up in {@code vars}. The result
     * is {@code true} when the value (case-insensitive) is {@code "true"}, {@code "yes"}, or
     * {@code "1"}. All other values — including empty, {@code "false"}, {@code "no"}, {@code "0"},
     * or an unknown variable — evaluate to {@code false}.
     *
     * @param condition the condition string (may contain a {@code {{varName}}} placeholder)
     * @param vars      the resolved variable map
     * @return {@code true} if the condition is satisfied
     */
    static boolean evaluateCondition(String condition, Map<String, String> vars) {
        // Resolve any {{var}} placeholders in the condition
        String resolved = TemplateEngine.render(condition.trim(), vars);

        // Strip surrounding {{ }} if the variable was not found (placeholder preserved)
        // In that case the variable is unknown → false
        if (resolved.startsWith("{{") && resolved.endsWith("}}")) {
            return false;
        }

        return switch (resolved.trim().toLowerCase()) {
            case "true", "yes", "1" -> true;
            default -> false;
        };
    }
}
