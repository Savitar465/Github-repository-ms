# GitHub — Proyecto Smithy

Modelo de API REST en Smithy 2.0 para el proyecto GitHub.

## Visión general

Este módulo mantiene el contrato de la API en Smithy y genera artefactos derivados para OpenAPI y Spring Boot.

Flujo recomendado:

```text
Smithy model -> smithyBuild -> OpenAPI -> openapi-generator (Spring / TypeScript)
```

## Estructura

```text
smithy/
├── build.gradle
├── settings.gradle
├── smithy-build.json
├── model/
│   ├── common/
│   │   └── common.smithy
│   └── repo/
│       ├── repo.smithy
│       ├── services/
│       │   ├── repo-api.smithy
│       │   ├── content-api.smithy
│       │   ├── branch-api.smithy
│       │   ├── collaborator-api.smithy
│       │   └── social-api.smithy
│       ├── operations/
│       │   ├── repository-operations.smithy
│       │   ├── content-operations.smithy
│       │   ├── branch-operations.smithy
│       │   ├── collaborator-operations.smithy
│       │   └── social-operations.smithy
│       └── structures/
│           ├── shared.smithy
│           ├── repository.smithy
│           ├── content.smithy
│           ├── branch.smithy
│           └── collaborator.smithy
└── README.md
```

## Requisitos

- Java 17 o superior
- Conexión a internet para descargar dependencias la primera vez

## Primeros pasos

### 1) Validar el modelo Smithy

```powershell
Set-Location "C:\Projects\Github-repository-ms\smithy"
.\gradlew.bat smithyBuild --no-daemon
```

Esto valida el modelo y genera las proyecciones configuradas en `smithy-build.json`.

### 2) Generar OpenAPI

```powershell
Set-Location "C:\Projects\Github-repository-ms\smithy"
.\gradlew.bat smithyBuild --no-daemon
```

La salida OpenAPI queda en `build/smithyprojections/.../openapi/`.

### 3) Generar código Spring / TypeScript desde OpenAPI

```powershell
Set-Location "C:\Projects\Github-repository-ms\smithy"
.\gradlew.bat generateAllCodegen --no-daemon
```

O, si quieres por separado:

```powershell
Set-Location "C:\Projects\Github-repository-ms\smithy"
.\gradlew.bat generateAllTypeScriptClients --no-daemon
.\gradlew.bat generateAllJavaServers --no-daemon
```

## Salidas generadas

- **OpenAPI**: `build/smithyprojections/<project>/<projection>/openapi/*.openapi.json`
- **Cliente TypeScript**: `build/generated/typescript/{domain}-client`
  - `repository-client`, `content-client`, `branch-client`, `collaborator-client`, `social-client`
- **Server Java Spring** (módulos independientes por dominio): `build/generated/spring/{domain}-module`
  - `repository-module`, `content-module`, `branch-module`, `collaborator-module`, `social-module`

Cada módulo Spring es completamente independiente e incluye:
- Controllers y delegados (delegate pattern)
- Modelos con enums propios
- `EnumConverterConfiguration.java` para conversión automática de enumeraciones
- `pom.xml` con todas las dependencias necesarias
- Aplicación Spring Boot autoejecutable

Estructura de paquetes en cada módulo:
```
com.smithy.g.repo.server.{domain}.api      (controllers)
com.smithy.g.repo.server.{domain}.model    (DTOs y enums)
com.smithy.g.repo.server.{domain}.invoker  (app principal)
org.openapitools.configuration             (enum converters)

## Generación por dominio

Cada dominio genera un módulo Spring independiente. Esto permite:
- **Independencia**: cada módulo puede desplegarse, versionarse y testearse por separado
- **Claridad**: cada módulo contiene solo sus operaciones, modelos y conversores de enums
- **Escalabilidad**: fácil agregar nuevos dominios o eliminar existentes

Generar un dominio específico:

```powershell
.\gradlew.bat generateRepositoryJavaServer --no-daemon
.\gradlew.bat generateContentJavaServer --no-daemon
```

## Convenciones del modelo

- Las operaciones de contenidos del repositorio usan `path` como query param: `/v1/repos/{owner}/{repo}/contents?path=...`
- Esto evita greedy labels (`{filePath+}`) y mejora la compatibilidad con herramientas OpenAPI.

## Autenticación

Las operaciones protegidas usan `@httpBearerAuth`.

Header requerido:

```text
Authorization: Bearer <jwt_token>
```

El token se obtiene en `POST /v1/auth/login`.
