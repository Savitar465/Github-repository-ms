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

@http(method: "GET", uri: "/v1/repos/{owner}/{repo}/branches", code: 200)
@readonly
@documentation("Lista todas las ramas del repositorio.")
operation ListBranches {
    input: RepoScopeInput
    output: ListBranchesOutput
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/v1/repos/{owner}/{repo}/branches/{branch}", code: 200)
@readonly
@documentation("Obtiene el detalle de una rama especifica.")
operation GetBranch {
    input: GetBranchInput
    output: GetBranchOutput
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "POST", uri: "/v1/repos/{owner}/{repo}/branches", code: 201)
@documentation("Crea una rama nueva a partir de otra existente.")
operation CreateBranch {
    input: CreateBranchInput
    output: CreateBranchOutput
    errors: [
        BadRequestError
        UnauthorizedError
        ForbiddenError
        NotFoundError
        ConflictError
        InternalServerError
    ]
}

@http(method: "DELETE", uri: "/v1/repos/{owner}/{repo}/branches/{branch}", code: 204)
@idempotent
@documentation("Elimina una rama (no puede ser la rama por defecto).")
operation DeleteBranch {
    input: DeleteBranchInput
    output: Unit
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        ConflictError
        InternalServerError
    ]
}
