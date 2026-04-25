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

@http(method: "GET", uri: "/v1/repos/{owner}/{repo}/collaborators", code: 200)
@readonly
@documentation("Lista colaboradores y sus roles.")
operation ListCollaborators {
    input: RepoScopeInput
    output: ListCollaboratorsOutput
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/v1/repos/{owner}/{repo}/collaborators/{collaboratorUsername}", code: 200)
@readonly
@documentation("Obtiene detalle de un colaborador especifico.")
operation GetCollaborator {
    input: CollaboratorScopeInput
    output: GetCollaboratorOutput
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/v1/repos/{owner}/{repo}/collaborators/{collaboratorUsername}", code: 204)
@idempotent
@documentation("Agrega o confirma un colaborador por username.")
operation AddCollaboratorByUsername {
    input: CollaboratorScopeInput
    output: Unit
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        ConflictError
        InternalServerError
    ]
}

@http(method: "POST", uri: "/v1/repos/{owner}/{repo}/collaborators", code: 201)
@documentation("Invita a un colaborador con un rol.")
operation AddCollaboratorWithRole {
    input: AddCollaboratorInput
    output: AddCollaboratorOutput
    errors: [
        BadRequestError
        UnauthorizedError
        ForbiddenError
        NotFoundError
        ConflictError
        InternalServerError
    ]
}

@http(method: "PATCH", uri: "/v1/repos/{owner}/{repo}/collaborators/{collaboratorUsername}", code: 200)
@documentation("Cambia el rol de un colaborador existente.")
operation UpdateCollaboratorRole {
    input: UpdateCollaboratorRoleInput
    output: UpdateCollaboratorRoleOutput
    errors: [
        BadRequestError
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "DELETE", uri: "/v1/repos/{owner}/{repo}/collaborators/{collaboratorUsername}", code: 204)
@idempotent
@documentation("Elimina un colaborador del repositorio.")
operation RemoveCollaborator {
    input: RemoveCollaboratorInput
    output: Unit
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}
