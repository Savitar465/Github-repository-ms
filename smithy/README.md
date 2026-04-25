# GitHub — Proyecto Smithy

Modelo de API REST en Smithy 2.0 para el proyecto académico Mini-GitHub.

## Estructura

```
mini-github-smithy/
├── setup.sh                          ← Ejecutar primero (genera el wrapper)
├── build.gradle                      ← Dependencias y plugin Smithy
├── settings.gradle                   ← Nombre del proyecto
├── smithy-build.json                 ← Configuración del build y proyecciones
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
└── model/
    ├── service.smithy                ← Servicio principal (punto de entrada)
    ├── common/
    │   └── common.smithy             ← Tipos, enums y errores compartidos
    ├── auth/
    │   └── auth.smithy               ← Auth Service (HU-01 a HU-05)
    ├── repo/
    │   ├── services/
    │   │   └── repo-api.smithy       ← Service RepoApi
    │   ├── operations/               ← Operaciones por dominio
    │   └── structures/               ← DTOs, inputs, outputs y listas
    ├── issue/
    │   └── issue.smithy              ← Issue Service + Pull Requests (HU-19-21)
    └── search/
        └── search.smithy             ← Search Service (RF05)
```

## Requisitos

- Java 17 o superior
- Conexión a internet (para descargar Gradle y dependencias la primera vez)

## Primeros pasos

### Paso 1 — Generar el Gradle Wrapper

```bash
cd mini-github-smithy
bash setup.sh
```

Esto descarga Gradle 8.10.2 y genera los archivos `gradlew`, `gradlew.bat` y `gradle-wrapper.jar`.

### Paso 2 — Compilar y validar el modelo

```bash
./gradlew build
```

Si el build es exitoso, el modelo Smithy es válido. Los artefactos generados quedan en:

```
build/smithy-output/
```

### Paso 3 — Generar OpenAPI (Swagger)

```bash
./gradlew smithyBuild
```

Genera el archivo OpenAPI en:

```
swagger-ui-watcher build/smithyprojections/mini-github-smithy/openapi/openapi/GitHubApi.openapi.json
```

## Operaciones por servicio

| Servicio | Puerto | Operaciones |
|---|---|---|
| Auth Service | 3001 | Register, Login, Logout, RefreshToken, GetMe, UpdateProfile, OAuth x2, ForgotPassword, ResetPassword, GetUserByUsername |
| Repo Service | 3002 | CRUD repos, Upload/Delete/Get archivos, DownloadArchive, Branches x3, Star/Unstar, Colaboradores x4 |
| Issue Service | 3003 | CRUD issues, Comentarios x2, Labels x2, Pull Requests x5 |
| Search Service | 3004 | SearchRepositories, SearchUsers, SearchIssues |

## Generacion de codigo (Auth)

```bash
./gradlew smithyBuild
./gradlew generateAuthCodegen
```

Salida esperada:
- OpenAPI Auth Public: `build/smithyprojections/mini-github-smithy/openapi-auth-public/openapi/AuthPublicApi.openapi.json`
- OpenAPI Auth Account: `build/smithyprojections/mini-github-smithy/openapi-auth-account/openapi/AuthAccountApi.openapi.json`
- Cliente TypeScript: `build/generated/openapi/authPublic-typescript-client`
- Server Java (services stubs): `build/generated/openapi/authAccount-java-server`

## Generacion de codigo (todos los dominios)

```bash
./gradlew smithyBuild
./gradlew generateAllCodegen
```

Incluye codegen para `auth`, `repo`, `issue`, `issueComments`, `files` y `search`:
- Clientes TypeScript: `build/generated/openapi/*-typescript-client`
- Servers Java con delegates/services: `build/generated/openapi/*-java-server`

Nota: las operaciones de contenidos del repositorio usan `path` como query (`/contents?path=...`) para evitar labels greedy y mejorar compatibilidad con herramientas OpenAPI.

## Autenticación

Todas las operaciones protegidas usan `@httpBearerAuth`.  
Header requerido: `Authorization: Bearer <jwt_token>`  
El token se obtiene en `POST /v1/auth/login`.
