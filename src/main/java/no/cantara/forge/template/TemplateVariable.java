package no.cantara.forge.template;

import java.util.List;

/**
 * Defines a single variable in a forge template manifest.
 * <p>
 * SnakeYAML requires a no-arg constructor and JavaBean-style getters/setters
 * for automatic mapping from YAML.
 *
 * <h3>YAML example:</h3>
 * <pre>{@code
 * variables:
 *   - name: projectName
 *     description: "Name of the project"
 *     type: string
 *     required: true
 *     default: "my-project"
 *     prompt: "Enter project name"
 *     validation: "^[a-z][a-z0-9-]*$"
 *   - name: javaVersion
 *     type: choice
 *     choices: ["17", "21"]
 *     default: "21"
 * }</pre>
 */
public class TemplateVariable {

    private String name;
    private String description;

    /**
     * Variable type: {@code string}, {@code boolean}, {@code choice}, or {@code number}.
     */
    private String type;

    private boolean required = false;
    private String defaultValue;
    private String prompt;

    /**
     * Optional regex pattern for input validation.
     */
    private String validation;

    /**
     * Valid choices when {@code type} is {@code choice}.
     */
    private List<String> choices;

    public TemplateVariable() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /** SnakeYAML also maps the {@code default} key via this alias setter. */
    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    @Override
    public String toString() {
        return "TemplateVariable{name='" + name + "', type='" + type + "', required=" + required + "}";
    }
}
