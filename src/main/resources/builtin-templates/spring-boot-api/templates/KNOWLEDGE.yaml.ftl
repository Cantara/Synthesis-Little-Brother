# AI CONTEXT FOR {{serviceName}}
# This file describes the company architectural standards for this project.

project_type: spring-boot-api
standards:
  - name: API-DESIGN
    rules: [RESTful, kebab-case-endpoints, standard-error-response]
  - name: NAMING
    rules: [package-private-where-possible, services-interface-suffix-impl]
  - name: DOCUMENTATION
    rules: [OpenAPI-3, mandatory-javadoc-on-public-methods]

instructions_for_ai: |
  When generating code for this project:
  1. Always use Spring Boot 3.x annotations.
  2. Use Jakarta Persistence for entities.
  3. Ensure all Controllers have @Tag annotations for OpenAPI.
  4. Follow the existing package structure in src/main/java/{{groupId | replace('.','/')}}.
