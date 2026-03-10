# Forge — KCP-Native Project Scaffolding

> Sibling to [Synthesis](https://github.com/exoreaction/Synthesis): Synthesis indexes what exists. Forge creates what's needed.

Generate company-standard projects from versioned [KCP](https://github.com/Cantara/knowledge-context-protocol) template manifests. Define your architecture standards once, enforce them everywhere — in CI/CD, IDE, and developer laptops.

## Quick Start

```bash
# Install
curl -L https://github.com/Cantara/Synthesis-Little-Brother/releases/latest/download/forge.jar -o forge.jar

# Generate a project
java -jar forge.jar generate java-base --var groupId=com.example --var artifactId=my-app

# Non-interactive (CI/CD)
java -jar forge.jar generate java-base -y \
  --var groupId=com.example \
  --var artifactId=my-app
```

## Commands

| Command | Description |
|---------|-------------|
| `forge generate <template>` | Scaffold a new project |
| `forge list` | List available templates |
| `forge inspect <template>` | Show template details and variables |
| `forge validate <path>` | Validate a template manifest |
| `forge registry add <url>` | Add a Git template registry |
| `forge registry list` | Show configured registries |

### `forge generate`

```
forge generate <template-id-or-path> [options]

Options:
  -o, --output-dir <dir>       Output directory (default: ./<template-id>)
  --var <key=value>            Variable override (repeatable)
  --vars-file <file.yaml>      YAML file with variable values
  -y, --no-interactive         Skip prompts, use defaults
  --dry-run                    Show file tree without writing
```

## Template Format (`forge-template.yaml`)

```yaml
forge_version: "0.1"
template:
  id: my-template
  name: "My Template"
  version: "1.0.0"
  description: "What this template creates"
  tags: [java, maven]
  author: "Your Company"

variables:
  - name: groupId
    description: "Maven groupId"
    type: string          # string | boolean | choice | number
    required: true
    default: "com.example"
    prompt: "Maven groupId"

files:
  - source: "pom.xml.ftl"    # .ftl = rendered; no .ftl = copied as-is
    target: "pom.xml"
  - source: "App.java.ftl"
    target: "src/main/java/{{groupId | replace('.','/')}}/App.java"
  - source: "Dockerfile.ftl"
    target: "Dockerfile"
    condition: "{{includeDocker}}"  # skip if variable is false/0/empty

hooks:
  post_generate:
    - command: "git init"
    - command: "git add ."
```

### Variable Substitution

Use `{{variableName}}` in template files and target paths. Apply filters with `|`:

| Filter | Example | Result |
|--------|---------|--------|
| `upper-case` | `{{name \| upper-case}}` | `MY-APP` |
| `lower-case` | `{{name \| lower-case}}` | `my-app` |
| `pascal-case` | `{{name \| pascal-case}}` | `MyApp` |
| `camel-case` | `{{name \| camel-case}}` | `myApp` |
| `kebab-case` | `{{name \| kebab-case}}` | `my-app` |
| `replace('a','b')` | `{{pkg \| replace('.','/')}}` | `com/example` |

## Template Registries

Templates are stored in Git repositories. Add a registry once, use all its templates:

```bash
# Add a registry (clones to ~/.forge/registries/)
forge registry add https://github.com/your-org/your-templates.git --name myorg

# List all templates from all registries
forge list

# Generate from registry template
forge generate myorg/spring-boot-service --var artifactId=my-service
```

### Registry structure

```
your-templates/
  templates/
    spring-boot-service/
      forge-template.yaml
      pom.xml.ftl
      src/
        main/java/App.java.ftl
    react-app/
      forge-template.yaml
      package.json.ftl
```

### Variables file (for CI/CD)

```yaml
# project.yaml
groupId: com.example
artifactId: my-service
javaVersion: "21"
includeDocker: true
```

```bash
forge generate java-base --vars-file project.yaml --no-interactive
```

## Built-in Templates

| Template | Description |
|----------|-------------|
| `java-base` | Minimal Java 21 Maven project with JUnit 5 |

## Build from Source

```bash
git clone https://github.com/Cantara/Synthesis-Little-Brother.git
cd Synthesis-Little-Brother
mvn package -q
java -jar target/forge.jar --help
```

## License

Apache 2.0
