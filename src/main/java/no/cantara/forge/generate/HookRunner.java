package no.cantara.forge.generate;

import no.cantara.forge.template.TemplateManifest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Executes shell hooks defined in a template manifest.
 *
 * <h3>Behaviour:</h3>
 * <ul>
 *   <li>Each command is printed before execution: {@code  → <command>}</li>
 *   <li>Commands run via {@code sh -c <command>} with the project directory as the working directory.</li>
 *   <li>If a command exits with a non-zero code, a warning is printed but execution continues.</li>
 * </ul>
 */
public final class HookRunner {

    private HookRunner() {
        // static utility — not instantiable
    }

    /**
     * Runs a single shell command in the given working directory.
     *
     * @param command    the shell command to execute
     * @param workingDir the directory to run the command in
     */
    public static void run(String command, Path workingDir) {
        System.out.println("  \u2192 " + command);
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            pb.directory(workingDir.toFile());
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Warning: hook exited with code " + exitCode + ": " + command);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Warning: hook failed (" + e.getMessage() + "): " + command);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Runs all {@code post_generate} hooks defined in the template manifest.
     * Does nothing if the manifest has no hooks block or no post_generate entries.
     *
     * @param manifest   the template manifest containing hook definitions
     * @param workingDir the generated project directory
     */
    public static void runAll(TemplateManifest manifest, Path workingDir) {
        if (manifest == null) return;

        Map<String, List<Map<String, String>>> hooks = manifest.getHooks();
        if (hooks == null) return;

        List<Map<String, String>> postGenerate = hooks.get("post_generate");
        if (postGenerate == null || postGenerate.isEmpty()) return;

        System.out.println("Running post-generate hooks...");
        for (Map<String, String> hook : postGenerate) {
            String command = hook.get("command");
            if (command != null && !command.isBlank()) {
                run(command, workingDir);
            }
        }
    }
}
