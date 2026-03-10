package no.cantara.forge.engine;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateEngineTest {

    @Test
    void render_simpleSubstitution() {
        String result = TemplateEngine.render("Hello, {{name}}!", Map.of("name", "Alice"));
        assertEquals("Hello, Alice!", result);
    }

    @Test
    void render_withUpperCaseFilter() {
        String result = TemplateEngine.render("{{name | upper-case}}", Map.of("name", "Alice"));
        assertEquals("ALICE", result);
    }

    @Test
    void render_withLowerCaseFilter() {
        String result = TemplateEngine.render("{{name | lower-case}}", Map.of("name", "ALICE"));
        assertEquals("alice", result);
    }

    @Test
    void render_withPascalCaseFilter() {
        String result = TemplateEngine.render("{{project-name | pascal-case}}", Map.of("project-name", "my-awesome-project"));
        assertEquals("MyAwesomeProject", result);
    }

    @Test
    void render_withKebabCaseFilter() {
        String result = TemplateEngine.render("{{projectName | kebab-case}}", Map.of("projectName", "MyAwesomeProject"));
        assertEquals("my-awesome-project", result);
    }

    @Test
    void render_withCamelCaseFilter() {
        String result = TemplateEngine.render("{{name | camel-case}}", Map.of("name", "my-project"));
        assertEquals("myProject", result);
    }

    @Test
    void render_unknownVariableLeftAsIs() {
        String result = TemplateEngine.render("Hello, {{unknown}}!", Map.of("name", "Alice"));
        assertEquals("Hello, {{unknown}}!", result);
    }

    @Test
    void render_multipleVariables() {
        Map<String, String> vars = Map.of(
                "groupId", "no.cantara",
                "artifactId", "my-service",
                "version", "1.0.0-SNAPSHOT"
        );
        String template = "<groupId>{{groupId}}</groupId>\n<artifactId>{{artifactId}}</artifactId>\n<version>{{version}}</version>";
        String result = TemplateEngine.render(template, vars);
        assertEquals("<groupId>no.cantara</groupId>\n<artifactId>my-service</artifactId>\n<version>1.0.0-SNAPSHOT</version>", result);
    }

    @Test
    void render_emptyStringReturnsEmpty() {
        String result = TemplateEngine.render("", Map.of("name", "Alice"));
        assertEquals("", result);
    }

    @Test
    void render_noPlaceholdersReturnsOriginal() {
        String result = TemplateEngine.render("no placeholders here", Map.of("name", "Alice"));
        assertEquals("no placeholders here", result);
    }

    @Test
    void render_replaceFilter() {
        String result = TemplateEngine.render("{{pkg | replace('.','/')}}", Map.of("pkg", "no.cantara.forge"));
        assertEquals("no/cantara/forge", result);
    }

    @Test
    void applyFilter_unknownFilterReturnsOriginalValue() {
        String result = TemplateEngine.applyFilter("hello", "nonexistent-filter");
        assertEquals("hello", result);
    }
}
