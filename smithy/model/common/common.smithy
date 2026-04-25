$version: "2.0"

namespace com.github.common

// ─── Tipos primitivos ─────────────────────────────────────────
@pattern("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")
string Uuid

@length(min: 3, max: 50)
@pattern("^[a-zA-Z0-9_-]+$")
string Username

@length(min: 5, max: 255)
string Email

@length(min: 8, max: 128)
@sensitive
string Password

@length(min: 1, max: 150)
@pattern("^[a-zA-Z0-9._-]+$")
string RepoName

@length(min: 1, max: 255)
string Title

@length(max: 65535)
string LongText

@length(max: 500)
string Url

@pattern("^#[0-9a-fA-F]{6}$")
string HexColor

@sensitive
@length(min: 10, max: 2048)
string JwtToken

// ─── Enumeraciones ────────────────────────────────────────────
enum RepoVisibility {
    PUBLIC = "public"
    PRIVATE = "private"
}

enum CollaboratorRole {
    READ = "read"
    WRITE = "write"
    ADMIN = "admin"
    MAINTAIN = "maintain"
}

// ─── Mixins compartidos ──────────────────────────────────────
@mixin
structure RepoScopedInputMixin {
    @required
    owner: Username

    @required
    repo: RepoName
}

// ─── Paginación ───────────────────────────────────────────────
structure PaginationMeta {
    @required
    page: Integer

    @required
    perPage: Integer

    @required
    total: Integer

    @required
    totalPages: Integer
}

// ─── Errores HTTP reutilizables ───────────────────────────────
@error("client")
@httpError(400)
structure BadRequestError {
    @required
    message: String
}

@error("client")
@httpError(401)
structure UnauthorizedError {
    @required
    message: String
}

@error("client")
@httpError(403)
structure ForbiddenError {
    @required
    message: String
}

@error("client")
@httpError(404)
structure NotFoundError {
    @required
    message: String
}

@error("client")
@httpError(409)
structure ConflictError {
    @required
    message: String
}

@error("client")
@httpError(422)
structure UnprocessableEntityError {
    @required
    message: String
}

@error("server")
@httpError(500)
structure InternalServerError {
    @required
    message: String
}

// ─── Tipos para gestión de archivos ──────────────────────────
/// Tipo de objeto Git (archivo o directorio)
enum GitObjectType {
    FILE = "file"
    DIRECTORY = "dir"
}

/// Identidad para autor/committer de commits
structure Identity {
    @required
    name: String

    @required
    email: Email
}
