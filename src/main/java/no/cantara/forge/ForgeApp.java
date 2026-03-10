package no.cantara.forge;

import no.cantara.forge.cli.GenerateCommand;
import no.cantara.forge.cli.InspectCommand;
import no.cantara.forge.cli.ListCommand;
import no.cantara.forge.cli.RegistryCommand;
import no.cantara.forge.cli.ValidateCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Forge CLI entry point.
 * <p>
 * Usage: {@code java -jar forge.jar <command>}
 */
@Command(
        name = "forge",
        description = "KCP-native project scaffolding — generate company-standard projects from versioned template manifests",
        version = "0.1.0-SNAPSHOT",
        mixinStandardHelpOptions = true,
        subcommands = {
                GenerateCommand.class,
                ListCommand.class,
                InspectCommand.class,
                RegistryCommand.class,
                ValidateCommand.class
        }
)
public final class ForgeApp implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ForgeApp()).execute(args);
        System.exit(exitCode);
    }
}
