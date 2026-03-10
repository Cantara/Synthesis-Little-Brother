package no.cantara.forge.util;

/**
 * Simple ANSI-coloured terminal output utilities for the Forge CLI.
 * <p>
 * All methods write directly to {@code System.out}. Inline formatting helpers
 * ({@link #bold}, {@link #green}, etc.) return decorated strings for embedding
 * inside larger messages.
 */
public final class AnsiOutput {

    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String DIM    = "\033[2m";
    private static final String RED    = "\033[31m";
    private static final String GREEN  = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String CYAN   = "\033[36m";

    private AnsiOutput() {
        // static utility — not instantiable
    }

    // -------------------------------------------------------------------------
    // Print helpers
    // -------------------------------------------------------------------------

    /**
     * Prints a bold cyan header line.
     *
     * @param text the header text
     */
    public static void printHeader(String text) {
        System.out.println(BOLD + CYAN + text + RESET);
    }

    /**
     * Prints a green success line prefixed with a check mark.
     *
     * @param text the success message
     */
    public static void printSuccess(String text) {
        System.out.println(GREEN + "\u2713 " + text + RESET);
    }

    /**
     * Prints a yellow warning line prefixed with a warning sign.
     *
     * @param text the warning message
     */
    public static void printWarning(String text) {
        System.out.println(YELLOW + "\u26A0 " + text + RESET);
    }

    /**
     * Prints a red error line to {@code System.err} prefixed with a cross.
     *
     * @param text the error message
     */
    public static void printError(String text) {
        System.err.println(RED + "\u2717 " + text + RESET);
    }

    /**
     * Prints a dim (grey) informational line.
     *
     * @param text the message
     */
    public static void printDim(String text) {
        System.out.println(DIM + text + RESET);
    }

    /**
     * Prints a cyan file path.
     *
     * @param path the path to display
     */
    public static void printFile(String path) {
        System.out.println(CYAN + path + RESET);
    }

    // -------------------------------------------------------------------------
    // Inline formatters (return decorated strings)
    // -------------------------------------------------------------------------

    /**
     * Returns the text wrapped in bold ANSI codes.
     *
     * @param text the text to bold
     * @return ANSI-decorated string
     */
    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    /**
     * Returns the text wrapped in green ANSI codes.
     *
     * @param text the text to colour green
     * @return ANSI-decorated string
     */
    public static String green(String text) {
        return GREEN + text + RESET;
    }

    /**
     * Returns the text wrapped in yellow ANSI codes.
     *
     * @param text the text to colour yellow
     * @return ANSI-decorated string
     */
    public static String yellow(String text) {
        return YELLOW + text + RESET;
    }

    /**
     * Returns the text wrapped in dim ANSI codes.
     *
     * @param text the text to dim
     * @return ANSI-decorated string
     */
    public static String dim(String text) {
        return DIM + text + RESET;
    }
}
