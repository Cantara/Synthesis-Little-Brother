# CONTEXTO PARA IA - ESTÁNDAR CORPORATIVO (QUARKUS)
project_type: quarkus-api
architecture:
  framework: Quarkus 3.x
  java_version: 21
  web_engine: RESTEasy Reactive (Jakarta REST)
  dependency_injection: ArC (CDI compliant)

standards:
  - name: API-DESIGN
    rules: [RESTful, kebab-case-endpoints, SmallRye-OpenAPI]
  - name: NAMING-CONVENTIONS
    rules: [Jakarta-Resource-suffix-Resource, Use-Jakarta-Annotations]
  - name: DOCUMENTATION
    rules: [MicroProfile-OpenAPI, mandatory-operation-tags]

instructions_for_ai: |
  Cuando generes código para este proyecto:
  1. Usa siempre anotaciones de Jakarta REST (jakarta.ws.rs.*).
  2. Prefiere el uso de RESTEasy Reactive para el manejo de endpoints.
  3. Asegura que todos los Resources tengan anotaciones @Tag y @Operation de MicroProfile OpenAPI.
  4. Usa inyección de dependencias mediante @Inject de Jakarta.
