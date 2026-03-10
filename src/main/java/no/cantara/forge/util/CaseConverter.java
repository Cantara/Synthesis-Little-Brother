package no.cantara.forge.util;

/**
 * Static utility for converting strings between common naming conventions.
 * <p>
 * Used by {@link no.cantara.forge.engine.TemplateEngine} to apply named filters
 * in template placeholders such as {@code {{projectName | pascal-case}}}.
 */
public final class CaseConverter {

    private CaseConverter() {
        // static utility — not instantiable
    }

    /**
     * Converts the input to UPPER CASE.
     *
     * @param s input string
     * @return upper-cased string, or empty string if input is null/blank
     */
    public static String toUpperCase(String s) {
        if (s == null) return "";
        return s.toUpperCase();
    }

    /**
     * Converts the input to lower case.
     *
     * @param s input string
     * @return lower-cased string, or empty string if input is null/blank
     */
    public static String toLowerCase(String s) {
        if (s == null) return "";
        return s.toLowerCase();
    }

    /**
     * Converts the input to PascalCase (UpperCamelCase).
     * <p>
     * Splits on hyphens, underscores, and whitespace; capitalises the first letter
     * of each word and concatenates them.
     *
     * <h3>Examples:</h3>
     * <pre>
     *   "my-project"  → "MyProject"
     *   "my_project"  → "MyProject"
     *   "my project"  → "MyProject"
     *   "myProject"   → "Myproject"  (single token — first letter capitalised)
     * </pre>
     *
     * @param s input string
     * @return PascalCase string
     */
    public static String toPascalCase(String s) {
        if (s == null || s.isBlank()) return "";
        String[] parts = s.split("[-_\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * Converts the input to camelCase (lowerCamelCase).
     * <p>
     * Same word-splitting logic as {@link #toPascalCase}, but the first word
     * is kept entirely lower-case.
     *
     * <h3>Examples:</h3>
     * <pre>
     *   "my-project"  → "myProject"
     *   "my_project"  → "myProject"
     *   "MyProject"   → "myproject"  (single token)
     * </pre>
     *
     * @param s input string
     * @return camelCase string
     */
    public static String toCamelCase(String s) {
        if (s == null || s.isBlank()) return "";
        String pascal = toPascalCase(s);
        if (pascal.isEmpty()) return "";
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }

    /**
     * Converts the input to kebab-case (lower-hyphen-separated).
     * <p>
     * Splits on: transitions from lowercase/digit to uppercase, existing hyphens,
     * underscores, and whitespace. All segments are lower-cased and joined with {@code -}.
     *
     * <h3>Examples:</h3>
     * <pre>
     *   "MyProject"    → "my-project"
     *   "myProject"    → "my-project"
     *   "my_project"   → "my-project"
     *   "my-project"   → "my-project"
     *   "MY_PROJECT"   → "my-project"
     * </pre>
     *
     * @param s input string
     * @return kebab-case string
     */
    public static String toKebabCase(String s) {
        if (s == null || s.isBlank()) return "";
        // Insert hyphen before each uppercase letter that follows a lowercase letter or digit
        String withHyphens = s
                .replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2");
        // Normalise existing underscores and whitespace to hyphens
        return withHyphens
                .replaceAll("[_\\s]+", "-")
                .replaceAll("-{2,}", "-")
                .toLowerCase();
    }
}
