package no.cantara.forge.config;

import no.cantara.forge.registry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level configuration POJO for {@code ~/.forge/config.yaml}.
 * <p>
 * SnakeYAML requires a no-arg constructor and JavaBean-style getters/setters.
 *
 * <h3>Example config.yaml:</h3>
 * <pre>{@code
 * registries:
 *   - name: cantara
 *     url: https://github.com/cantara/forge-templates.git
 *     branch: main
 *     path: templates
 * registryCacheTtlHours: 24
 * }</pre>
 */
public class ForgeConfig {

    private List<RegistryEntry> registries = new ArrayList<>();
    private int registryCacheTtlHours = 24;

    public ForgeConfig() {
    }

    public List<RegistryEntry> getRegistries() {
        return registries;
    }

    public void setRegistries(List<RegistryEntry> registries) {
        this.registries = registries != null ? registries : new ArrayList<>();
    }

    public int getRegistryCacheTtlHours() {
        return registryCacheTtlHours;
    }

    public void setRegistryCacheTtlHours(int registryCacheTtlHours) {
        this.registryCacheTtlHours = registryCacheTtlHours;
    }

    /** SnakeYAML alias for the snake_case key {@code registry_cache_ttl_hours}. */
    public void setRegistry_cache_ttl_hours(int hours) {
        this.registryCacheTtlHours = hours;
    }
}
