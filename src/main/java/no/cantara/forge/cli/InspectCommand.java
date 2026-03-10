package no.cantara.forge.cli;

import no.cantara.forge.config.ForgeConfig;
import no.cantara.forge.config.ForgeConfigLoader;
import no.cantara.forge.registry.RegistryManager;
import no.cantara.forge.template.TemplateFile;
import no.cantara.forge.template.TemplateManifest;
import no.cantara.forge.template.TemplateManifestLoader;
import no.cantara.forge.template.TemplateVariable;
import no.cantara.forge.util.AnsiOutput;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Shows template details, variables, and a file structure preview.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * forge inspect java-base
 * forge inspect cantara/java-service
 * forge inspect /path/to/my-template
 * }</pre>
 */
@Command(
        name = "inspect",
        description = "Show template details, variables, and file structure preview",
        mixinStandardHelpOptions = true
)
public class InspectCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Template ID or path to inspect")
    String templateId;

    @Override
    public Integer call() {
        try {
            ForgeConfig config = ForgeConfigLoader.load();
            RegistryManager manager = new RegistryManager(
                    config,
                    ForgeConfigLoader.getRegistriesDir(),
                    ForgeConfigLoader.getTemplatesDir()
            );

            Path templateDir = manager.resolve(templateId);
            if (templateDir == null) {
                AnsiOutput.printError("Template not found: " + templateId);
                return 1;
            }

            TemplateManifest manifest = TemplateManifestLoader.load(templateDir);
            TemplateManifest.TemplateInfo info = manifest.getTemplate();

            // Header
            AnsiOutput.printHeader("Template: " + (info != null ? info.getId() : templateId));
            System.out.println();

            // Template metadata
            if (info != null) {
                System.out.println(AnsiOutput.bold("Name:        ") + (info.getName() != null ? info.getName() : "-"));
                System.out.println(AnsiOutput.bold("Version:     ") + (info.getVersion() != null ? info.getVersion() : "-"));
                System.out.println(AnsiOutput.bold("Description: ") + (info.getDescription() != null ? info.getDescription() : "-"));
                System.out.println(AnsiOutput.bold("Author:      ") + (info.getAuthor() != null ? info.getAuthor() : "-"));
                System.out.println(AnsiOutput.bold("License:     ") + (info.getLicense() != null ? info.getLicense() : "-"));
                if (info.getTags() != null && !info.getTags().isEmpty()) {
                    System.out.println(AnsiOutput.bold("Tags:        ") + String.join(", ", info.getTags()));
                }
                System.out.println(AnsiOutput.bold("Path:        ") + templateDir);
            }

            // Variables table
            List<TemplateVariable> vars = manifest.getVariables();
            if (vars != null && !vars.isEmpty()) {
                System.out.println();
                AnsiOutput.printHeader("Variables:");
                System.out.println();

                int nameW = Math.max(8, vars.stream().mapToInt(v -> v.getName() != null ? v.getName().length() : 4).max().orElse(8));
                int typeW = Math.max(6, vars.stream().mapToInt(v -> v.getType() != null ? v.getType().length() : 6).max().orElse(6));
                int defW  = Math.max(7, vars.stream().mapToInt(v -> v.getDefaultValue() != null ? v.getDefaultValue().length() : 7).max().orElse(7));
                String fmt = "  %-" + nameW + "s  %-" + typeW + "s  %-8s  %-" + defW + "s  %s%n";

                System.out.printf(fmt, "NAME", "TYPE", "REQUIRED", "DEFAULT", "DESCRIPTION");
                System.out.printf(fmt,
                        "-".repeat(nameW), "-".repeat(typeW), "--------",
                        "-".repeat(defW), "-----------");

                for (TemplateVariable v : vars) {
                    System.out.printf(fmt,
                            v.getName() != null ? v.getName() : "",
                            v.getType() != null ? v.getType() : "string",
                            v.isRequired() ? "yes" : "no",
                            v.getDefaultValue() != null ? v.getDefaultValue() : "",
                            v.getDescription() != null ? v.getDescription() : "");
                }
            } else {
                System.out.println();
                AnsiOutput.printDim("  (no variables defined)");
            }

            // Files list
            List<TemplateFile> files = manifest.getFiles();
            if (files != null && !files.isEmpty()) {
                System.out.println();
                AnsiOutput.printHeader("Files:");
                System.out.println();
                for (TemplateFile f : files) {
                    String target = f.getTarget() != null ? f.getTarget() : f.getSource();
                    String cond = f.getCondition() != null ? AnsiOutput.dim("  [if " + f.getCondition() + "]") : "";
                    System.out.println("  " + target + cond);
                }
            } else {
                System.out.println();
                AnsiOutput.printDim("  (no explicit files block — all files will be auto-discovered)");
            }

            System.out.println();
            return 0;

        } catch (Exception e) {
            AnsiOutput.printError("Inspect failed: " + e.getMessage());
            return 1;
        }
    }
}
