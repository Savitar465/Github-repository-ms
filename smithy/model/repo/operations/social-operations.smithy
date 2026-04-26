$version: "2"

namespace com.github.repo

use com.github.common#InternalServerError
use com.github.common#NotFoundError
use com.github.common#UnauthorizedError
use smithy.api#documentation
use smithy.api#http
use smithy.api#idempotent

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
