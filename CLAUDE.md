# Forge — CLAUDE.md

KCP-native project scaffolding CLI. Sibling to Synthesis: Synthesis indexes what exists, Forge creates what's needed.

## Stack
- Java 21, Picocli 4.7.6, SnakeYAML 2.2, JGit 7.1.0
- Maven fat JAR → `forge.jar`
- JUnit Jupiter 5.10.2 (no TestNG)

## Build & Test
```bash
mvn test                        # run tests
mvn package -q                  # build forge.jar
java -jar target/forge.jar      # run CLI
```

## Architecture

```
no.cantara.forge
  ForgeApp.java           CLI entry point (Picocli)
  cli/                    One class per command
  template/               TemplateManifest + TemplateVariable + TemplateFile POJOs
  engine/                 TemplateEngine ({{var | filter}} substitution)
  registry/               RegistryManager + resolvers
  generate/               ProjectGenerator (orchestrates generation pipeline)
  util/                   CaseConverter, AnsiOutput
```

## Template Format (`forge-template.yaml`)
```yaml
forge_version: "0.1"
template:
  id: my-template
  name: "My Template"
  version: "1.0.0"
variables:
  - name: projectName
    type: string
    required: true
    default: "my-project"
files:
  - source: "pom.xml.ftl"
    target: "pom.xml"
```

Template files use `{{variableName}}` substitution with filters:
`upper-case` | `lower-case` | `pascal-case` | `camel-case` | `kebab-case` | `replace('from','to')`

## Key Conventions
- SnakeYAML bean mapping: POJOs need getters+setters (not records)
- `default` is a Java keyword — use `setDefault()` alias setter in TemplateVariable
- Unknown variables in templates are left as `{{placeholder}}` (never throw)
- Stub commands return 0 with "(not yet implemented)" message

## Plan
Full implementation plan: `/home/totto/.claude/plans/forge-cli.md`
