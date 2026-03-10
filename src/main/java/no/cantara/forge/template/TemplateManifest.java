package no.cantara.forge.template;

import java.util.List;
import java.util.Map;

/**
 * Maps the top-level structure of a {@code forge-template.yaml} manifest.
 * <p>
 * SnakeYAML requires a no-arg constructor and JavaBean-style getters/setters
 * for automatic mapping from YAML.
 *
 * <h3>Full YAML structure:</h3>
 * <pre>{@code
 * forge_version: "0.1"
 * template:
 *   id: cantara/java-service
 *   name: "Cantara Java Microservice"
 *   version: "1.2.0"
 *   description: "Standard Cantara Java 21 + Picocli microservice"
 *   author: "Cantara"
 *   license: "Apache-2.0"
 *   updated: "2026-03-10"
 *   tags: [java, microservice, cantara]
 * extend: cantara/base-project
 * variables:
 *   - name: groupId
 *     type: string
 *     required: true
 * files:
 *   - source: templates/pom.xml.tmpl
 *     target: pom.xml
 * }</pre>
 */
public class TemplateManifest {

    private String forgeVersion;
    private TemplateInfo template;
    private String extend;
    private List<TemplateVariable> variables;
    private List<TemplateFile> files;

    /**
     * Optional hooks block. Structure:
     * <pre>{@code
     * hooks:
     *   post_generate:
     *     - command: "git init"
     *       description: "Initialize git repository"
     * }</pre>
     */
    private Map<String, List<Map<String, String>>> hooks;

    public TemplateManifest() {
    }

    public String getForgeVersion() {
        return forgeVersion;
    }

    public void setForgeVersion(String forgeVersion) {
        this.forgeVersion = forgeVersion;
    }

    /** SnakeYAML maps {@code forge_version} via this alias setter. */
    public void setForge_version(String forgeVersion) {
        this.forgeVersion = forgeVersion;
    }

    public TemplateInfo getTemplate() {
        return template;
    }

    public void setTemplate(TemplateInfo template) {
        this.template = template;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public List<TemplateVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<TemplateVariable> variables) {
        this.variables = variables;
    }

    public List<TemplateFile> getFiles() {
        return files;
    }

    public void setFiles(List<TemplateFile> files) {
        this.files = files;
    }

    public Map<String, List<Map<String, String>>> getHooks() {
        return hooks;
    }

    public void setHooks(Map<String, List<Map<String, String>>> hooks) {
        this.hooks = hooks;
    }

    /**
     * Nested class capturing the {@code template:} block metadata.
     */
    public static class TemplateInfo {

        private String id;
        private String name;
        private String version;
        private String description;
        private String author;
        private String license;
        private String updated;
        private List<String> tags;

        public TemplateInfo() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getLicense() {
            return license;
        }

        public void setLicense(String license) {
            this.license = license;
        }

        public String getUpdated() {
            return updated;
        }

        public void setUpdated(String updated) {
            this.updated = updated;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        @Override
        public String toString() {
            return "TemplateInfo{id='" + id + "', version='" + version + "'}";
        }
    }

    @Override
    public String toString() {
        return "TemplateManifest{forgeVersion='" + forgeVersion + "', template=" + template + "}";
    }
}
