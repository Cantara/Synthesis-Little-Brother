# 🚀 Guía de Demo: Forge Enterprise POC

Esta guía contiene los pasos exactos para ejecutar la demostración de **Forge** como herramienta de estandarización de arquitecturas (Spring Boot, Angular y próximamente Quarkus).

## 1. Preparación del Entorno
Antes de la demo, asegúrate de compilar la última versión que incluye las plantillas corporativas:

```bash
# Compilar el proyecto (genera el forge.jar actualizado)
mvn package -DskipTests -q
```

---

## 2. Paso 1: El Catálogo de Estándares
Muestra al cliente cómo los desarrolladores ven solo las arquitecturas aprobadas por el equipo de Arquitectura.

```bash
# Listar plantillas disponibles
java -jar target/forge.jar list
```
*Observación: Resalta las plantillas `company/spring-boot-api`, `company/quarkus-api` y `company/angular-enterprise`.*

---

## 3. Paso 2: Generación de Backend (Quarkus)
Muestra la alternativa moderna para microservicios.

```bash
# Generar el proyecto de Quarkus
java -jar target/forge.jar generate quarkus-api \
  --var groupId=com.empresa.inventory \
  --var artifactId=inventory-service \
  --var serviceName="Gestión de Inventario" \
  -o ./demo-quarkus -y
```

**Puntos a destacar:**
- `src/main/java/.../GreetingResource.java`: Uso de Jakarta REST y MicroProfile OpenAPI.
- `pom.xml`: Basado en el BOM de Quarkus 3.x.
- `KNOWLEDGE.yaml`: Adaptado específicamente para el estilo de programación de Quarkus.

---

## 4. Paso 3: Generación de Backend (Spring Boot 3)
Genera un microservicio que ya cumple con OpenAPI, Java 21 y estructura de paquetes corporativa.

```bash
# Generar el proyecto de backend
java -jar target/forge.jar generate spring-boot-api \
  --var groupId=com.empresa.nomina \
  --var artifactId=servicio-empleados \
  --var serviceName="Gestión de Empleados" \
  -o ./demo-backend -y
```

**Puntos a destacar en la carpeta `demo-backend`:**
- `src/main/java/.../config/OpenApiConfig.java`: Documentación automática.
- `src/main/java/.../controller/HealthController.java`: Endpoint de salud estándar.
- `KNOWLEDGE.yaml`: El archivo de contexto para la IA.

---

## 5. Paso 4: Generación de Frontend (Angular 17)
Muestra cómo se pueden incluir o excluir componentes modulares (Piezas de Lego).

```bash
# Generar el proyecto de frontend con Grillas incluidas
java -jar target/forge.jar generate angular-enterprise \
  --var projectName=admin-portal \
  --var includeGrids=true \
  -o ./demo-frontend -y
```

**Nota sobre Permisos (macOS):**
Si al ejecutar `npm install` obtienes un error `EPERM`, asegúrate de que tu terminal tenga permisos de escritura en la carpeta (puedes probar moviendo el proyecto fuera de `Downloads` o usando `sudo chown -R $USER .` si es necesario).

**Puntos a destacar:**
- `src/app/shared/components/grid/`: El componente de grilla estándar inyectado automáticamente.
- `package.json`: Versiones de Angular y Material aprobadas.

---

## 5. Paso 4: El superpoder de la IA (Contexto Corporativo)
Abre el archivo `./demo-backend/KNOWLEDGE.yaml` y explica:
> "Si el desarrollador quiere usar ChatGPT o Copilot, solo tiene que pasarle este archivo. La IA ahora 'conoce' que debe usar Jakarta Persistence y seguir nuestras reglas de naming, eliminando errores de estilo."

---

## Próximos Pasos para la POC
1.  **Personalización Dinámica:** Añadir más variables para elegir bases de datos (Postgres, Oracle, etc.) directamente desde el comando.
2.  **Dashboard de Arquitectura:** Una interfaz para que el equipo de arquitectura pueda gestionar sus manifiestos centralmente.

---
*Generado por Junie para la POC de Forge Enterprise.*
