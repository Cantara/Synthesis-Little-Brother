package no.cantara.forge.generate;

import no.cantara.forge.template.TemplateFile;
import no.cantara.forge.template.TemplateManifest;
import no.cantara.forge.template.TemplateManifestLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Orchestrates the full project-generation pipeline:
 * <ol>
 *   <li>Load the template manifest from {@code templateDir}</li>
 *   <li>Validate and use the already-resolved variable map</li>
 *   <li>Create the output directory (or validate it is empty)</li>
 *   <li>Render each file (FTL → rendered, static → copied)</li>
 *   <li>Auto-discover files when the manifest has no explicit {@code files:} block</li>
 * </ol>
 */
public class ProjectGenerator {

    /**
     * Summary of a completed (or dry-run) generation.
     *
     * @param outputDir    the directory that was (or would have been) written to
     * @param filesCreated number of files written
     * @param skippedFiles relative paths of files that were skipped (e.g. due to a condition)
     */
    public record GenerateResult(Path outputDir, int filesCreated, List<String> skippedFiles) {}

    /**
     * Executes the full generation pipeline.
     *
     * @param templateDir  directory containing {@code forge-template.yaml} and template files
     * @param outputDir    directory to write generated files into
     * @param resolvedVars fully-resolved variable map (collected by {@link no.cantara.forge.engine.VariableCollector})
     * @param dryRun       when {@code true}, print the file tree but do not write anything
     * @return a {@link GenerateResult} with counts and skipped-file information
     * @throws IOException              if template files cannot be read or output cannot be written
     * @throws IllegalArgumentException if the output directory already exists (and dry-run is false)
     */
    public GenerateResult generate(Path templateDir,
                                   Path outputDir,
                                   Map<String, String> resolvedVars,
                                   boolean dryRun) throws IOException {

        TemplateManifest manifest = TemplateManifestLoader.load(templateDir);

        // Determine file list: explicit manifest entries or auto-discovered
        List<TemplateFile> fileEntries;
        if (manifest.getFiles() != null && !manifest.getFiles().isEmpty()) {
            fileEntries = manifest.getFiles();
        } else {
            fileEntries = discoverFiles(templateDir);
        }

        // Dry-run: print tree and return without writing
        if (dryRun) {
            System.out.println("Dry-run output for: " + outputDir);
            List<String> skipped = new ArrayList<>();
            int would = 0;
            for (TemplateFile entry : fileEntries) {
                String condition = entry.getCondition();
                if (condition != null && !condition.isBlank()
                        && !FileRenderer.evaluateCondition(condition, resolvedVars)) {
                    System.out.println("  [skip] " + entry.getSource());
                    skipped.add(entry.getSource());
                } else {
                    System.out.println("  [create] " + deriveTargetPath(entry, resolvedVars));
                    would++;
                }
            }
            return new GenerateResult(outputDir, would, skipped);
        }

        // Create output directory
        if (Files.exists(outputDir)) {
            throw new IllegalArgumentException(
                    "Output directory already exists: " + outputDir
                    + ". Use --force to overwrite (not yet implemented).");
        }
        Files.createDirectories(outputDir);

        // Render each file
        int filesCreated = 0;
        List<String> skippedFiles = new ArrayList<>();

        for (TemplateFile entry : fileEntries) {
            String condition = entry.getCondition();
            if (condition != null && !condition.isBlank()
                    && !FileRenderer.evaluateCondition(condition, resolvedVars)) {
                skippedFiles.add(entry.getSource());
                continue;
            }

            FileRenderer.render(entry, templateDir, outputDir, resolvedVars);
            filesCreated++;
        }

        return new GenerateResult(outputDir, filesCreated, skippedFiles);
    }

    /**
     * Derives the final target path for a file entry (after variable substitution and .ftl strip),
     * used only for display in dry-run mode.
     */
    private String deriveTargetPath(TemplateFile entry, Map<String, String> vars) {
        String target = entry.getTarget() != null ? entry.getTarget() : entry.getSource();
        target = no.cantara.forge.engine.TemplateEngine.render(target, vars);
        if (entry.getSource().endsWith(".ftl") && target.endsWith(".ftl")) {
            target = target.substring(0, target.length() - 4);
        }
        return target;
    }

    /**
     * Auto-discovers all files in {@code templateDir} recursively, excluding
     * {@code forge-template.yaml} itself. Returns {@link TemplateFile} entries whose
     * {@code source} and {@code target} are the relative path from {@code templateDir}.
     *
     * @param templateDir the template root directory
     * @return list of discovered file entries
     * @throws IOException if the directory cannot be traversed
     */
    List<TemplateFile> discoverFiles(Path templateDir) throws IOException {
        List<TemplateFile> entries = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(templateDir)) {
            stream.filter(Files::isRegularFile)
                  .filter(p -> !templateDir.relativize(p).toString().equals("forge-template.yaml"))
                  .sorted()
                  .forEach(p -> {
                      String relative = templateDir.relativize(p).toString();
                      TemplateFile tf = new TemplateFile();
                      tf.setSource(relative);
                      tf.setTarget(relative);
                      entries.add(tf);
                  });
        }
        return entries;
    }
}
