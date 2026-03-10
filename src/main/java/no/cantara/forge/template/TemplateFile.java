package no.cantara.forge.template;

/**
 * Represents a single file entry in a forge template manifest.
 * <p>
 * SnakeYAML requires a no-arg constructor and JavaBean-style getters/setters.
 *
 * <h3>YAML example:</h3>
 * <pre>{@code
 * files:
 *   - source: templates/pom.xml.tmpl
 *     target: pom.xml
 *   - source: templates/README.md.tmpl
 *     target: README.md
 *     condition: "includeReadme"
 * }</pre>
 */
public class TemplateFile {

    private String source;
    private String target;

    /**
     * Optional boolean expression. When evaluated to {@code false}, this file
     * is skipped during generation.
     */
    private String condition;

    public TemplateFile() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "TemplateFile{source='" + source + "', target='" + target + "'}";
    }
}
