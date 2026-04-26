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
│       │   └── repo-api.smithy
│       ├── operations/
│       │   ├── repo-operations.smithy
│       │   ├── content-operations.smithy
│       │   ├── branch-operations.smithy
│       │   └── collaborator-operations.smithy
│       └── structures/
│           ├── repo-structures.smithy
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

- OpenAPI: `build/smithyprojections/<project>/<projection>/openapi/*.openapi.json`
- Cliente TypeScript: `build/generated/openapi/*-typescript-client`
- Server Java Spring: `build/generated/openapi/*-java-server`

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
