$version: "2"

namespace com.github.repo

use com.github.common#BadRequestError
use com.github.common#ConflictError
use com.github.common#ForbiddenError
use com.github.common#InternalServerError
use com.github.common#NotFoundError
use com.github.common#UnauthorizedError
use smithy.api#documentation
use smithy.api#http
use smithy.api#idempotent
use smithy.api#readonly

@http(method: "GET", uri: "/v1/repos", code: 200)
@readonly
@documentation("Lista los repositorios del usuario autenticado.")
operation ListMyRepositories {
    input: ListMyRepositoriesInput
    output: ListMyRepositoriesOutput
    errors: [
        UnauthorizedError
        InternalServerError
    ]
}

@http(method: "POST", uri: "/v1/repos", code: 201)
@documentation("Crea un repositorio nuevo para el usuario autenticado.")
operation CreateRepository {
    input: CreateRepositoryInput
    output: CreateRepositoryOutput
    errors: [
        BadRequestError
        UnauthorizedError
        ConflictError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/v1/repos/{owner}/{repo}", code: 200)
@readonly
@documentation("Obtiene los datos de un repositorio.")
operation GetRepository {
    input: GetRepositoryInput
    output: GetRepositoryOutput
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PATCH", uri: "/v1/repos/{owner}/{repo}", code: 200)
@documentation("Actualiza descripcion, visibilidad u opciones del repositorio. Solo el owner.")
operation UpdateRepository {
    input: UpdateRepositoryInput
    output: UpdateRepositoryOutput
    errors: [
        BadRequestError
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "DELETE", uri: "/v1/repos/{owner}/{repo}", code: 204)
@idempotent
@documentation("Elimina el repositorio y todos sus datos. Solo el owner.")
operation DeleteRepository {
    input: DeleteRepositoryInput
    output: Unit
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "POST", uri: "/v1/repos/{owner}/{repo}/forks", code: 201)
@documentation("Crea un fork de un repositorio publico en la cuenta del usuario autenticado.")
operation ForkRepository {
    input: ForkRepositoryInput
    output: ForkRepositoryOutput
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        ConflictError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/v1/repos/{owner}/{repo}/forks", code: 200)
@readonly
@documentation("Lista los forks de un repositorio.")
operation ListRepositoryForks {
    input: RepoScopeInput
    output: ListRepositoryForksOutput
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/v1/repos/{owner}/{repo}/star", code: 204)
@idempotent
@documentation("Da estrella a un repositorio.")
operation StarRepository {
    input: RepoScopeInput
    output: Unit
    errors: [
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "DELETE", uri: "/v1/repos/{owner}/{repo}/star", code: 204)
@idempotent
@documentation("Quita la estrella de un repositorio.")
operation UnstarRepository {
    input: RepoScopeInput
    output: Unit
    errors: [
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/v1/user/starred/{owner}/{repo}", code: 204)
@idempotent
@documentation("Da estrella a un repositorio con ruta.")
operation StarRepositoryForAuthenticatedUser {
    input: RepoScopeInput
    output: Unit
    errors: [
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "DELETE", uri: "/v1/user/starred/{owner}/{repo}", code: 204)
@idempotent
@documentation("Quita estrella a un repositorio con ruta.")
operation UnstarRepositoryForAuthenticatedUser {
    input: RepoScopeInput
    output: Unit
    errors: [
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}
