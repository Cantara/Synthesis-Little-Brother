package no.cantara.forge.registry;

/**
 * Describes a single template registry source — either a Git URL or a local path.
 * <p>
 * SnakeYAML requires a no-arg constructor and JavaBean-style getters/setters.
 *
 * <h3>YAML example:</h3>
 * <pre>{@code
 * registries:
 *   - name: cantara
 *     url: https://github.com/cantara/forge-templates.git
 *     branch: main
 *     path: templates
 * }</pre>
 */
public class RegistryEntry {

    private String name;
    private String url;
    private String branch = "main";
    private String path = "templates";

    public RegistryEntry() {
    }

    public RegistryEntry(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch != null ? branch : "main";
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path != null ? path : "templates";
    }

    /**
     * Returns {@code true} when this entry points to a local filesystem path
     * rather than a remote Git repository.
     *
     * @return {@code true} if the URL does not start with {@code http://},
     *         {@code https://}, or {@code git@} / {@code git://}
     */
    public boolean isLocal() {
        if (url == null) return true;
        String lower = url.toLowerCase();
        return !lower.startsWith("http://")
                && !lower.startsWith("https://")
                && !lower.startsWith("git@")
                && !lower.startsWith("git://");
    }

    @Override
    public String toString() {
        return "RegistryEntry{name='" + name + "', url='" + url + "'}";
    }
}
