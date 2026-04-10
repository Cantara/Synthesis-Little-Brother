# CONTEXTO PARA IA - ESTÁNDAR CORPORATIVO (ANGULAR)
project_type: angular-enterprise
architecture:
  framework: Angular 17+
  styling: Angular Material
  components: Standalone

standards:
  - name: COMPONENTS
    rules: [standalone-components, mat-prefixed-styles, company-grid-standard]
  - name: STATE-MANAGEMENT
    rules: [signals-api-only]

instructions_for_ai: |
  Cuando generes componentes de UI para este proyecto:
  1. Usa Angular 17 Signals para el manejo de estado.
  2. Prefiere @angular/material para los elementos básicos.
  3. Sigue el patrón de componentes Standalone.
  4. Si necesitas una grilla, usa el componente corporativo `company-grid`.
