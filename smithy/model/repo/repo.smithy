$version: "2"

namespace com.github.repo

use aws.protocols#restJson1
use com.github.common#BadRequestError
use com.github.common#CollaboratorRole
use com.github.common#ConflictError
use com.github.common#ForbiddenError
use com.github.common#InternalServerError
use com.github.common#NotFoundError
use com.github.common#PaginationMeta
use com.github.common#RepoName
use com.github.common#RepoScopedInputMixin
use com.github.common#RepoVisibility
use com.github.common#UnauthorizedError
use com.github.common#Url
use com.github.common#Username
use com.github.common#Uuid

// ─── Estructuras de Repositorio ───────────────────────────────
structure RepositoryDTO {
    @required
    id: Uuid

    @required
    name: RepoName

    @required
    fullName: String

    description: String

    @required
    visibility: RepoVisibility

    @required
    ownerId: Uuid

    @required
    ownerUsername: Username

    @required
    starsCount: Integer

    @required
    forksCount: Integer

    @required
    defaultBranch: String

    language: String

    @required
    hasIssues: Boolean

    @required
    createdAt: String

    @required
    updatedAt: String
}

list RepositoryList {
    member: RepositoryDTO
}

// ─── HU-07: Listar repositorios del usuario ───────────────────
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

structure ListMyRepositoriesInput {
    @httpQuery("visibility")
    visibility: RepoVisibility

    @httpQuery("page")
    @range(min: 1)
    page: Integer

    @httpQuery("perPage")
    @range(min: 1, max: 100)
    perPage: Integer
}

structure ListMyRepositoriesOutput {
    @required
    @httpPayload
    body: ListRepositoriesBody
}

structure ListRepositoriesBody {
    @required
    repositories: RepositoryList

    @required
    pagination: PaginationMeta
}

// ─── HU-06: Crear repositorio ─────────────────────────────────
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

structure CreateRepositoryInput {
    @required
    @httpPayload
    body: CreateRepositoryBody
}

structure CreateRepositoryBody {
    @required
    name: RepoName

    description: String

    @required
    visibility: RepoVisibility

    initWithReadme: Boolean

    @length(max: 50)
    language: String
}

structure CreateRepositoryOutput {
    @required
    @httpPayload
    body: RepositoryDTO
}

// ─── Obtener repositorio ──────────────────────────────────────
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

structure GetRepositoryInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName
}

structure GetRepositoryOutput {
    @required
    @httpPayload
    body: RepositoryDTO
}

// ─── HU-08: Actualizar repositorio ───────────────────────────
@http(method: "PATCH", uri: "/v1/repos/{owner}/{repo}", code: 200)
@documentation("Actualiza descripción, visibilidad u opciones del repositorio. Solo el owner.")
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

structure UpdateRepositoryInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpPayload
    body: UpdateRepositoryBody
}

structure UpdateRepositoryBody {
    description: String
    visibility: RepoVisibility
    hasIssues: Boolean
    language: String
}

structure UpdateRepositoryOutput {
    @required
    @httpPayload
    body: RepositoryDTO
}

// ─── HU-09: Eliminar repositorio ─────────────────────────────
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

structure DeleteRepositoryInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName
}

// ─── HU-09: Fork de repositorio público ──────────────────────
@http(method: "POST", uri: "/v1/repos/{owner}/{repo}/forks", code: 201)
@documentation("Crea un fork de un repositorio público en la cuenta del usuario autenticado.")
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

structure ForkRepositoryInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @httpPayload
    body: ForkRepositoryBody
}

structure ForkRepositoryBody {
    // Opcional: nombre del nuevo fork
    name: RepoName

    // Opcional: organización/owner destino
    targetOwner: String
}

structure ForkRepositoryOutput {
    @required
    @httpPayload
    body: RepositoryDTO
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

structure ListRepositoryForksOutput {
    @required
    @httpPayload
    body: ListRepositoryForksBody
}

structure ListRepositoryForksBody {
    @required
    repositories: RepositoryList
}

// ─── Archivos: listar contenidos ──────────────────────────────
structure FileEntryDTO {
    @required
    name: String

    @required
    path: String

    @required
    type: FileType

    size: Long

    contentType: String

    downloadUrl: Url

    @required
    branch: String

    @required
    updatedAt: String
}

enum FileType {
    FILE = "file"
    DIRECTORY = "directory"
}

list FileEntryList {
    member: FileEntryDTO
}

@http(method: "GET", uri: "/v1/repos/{owner}/{repo}/contents/{filePath+}", code: 200)
@readonly
@documentation("Lista archivos y carpetas de una ruta del repositorio.")
operation GetRepoContents {
    input: GetRepoContentsInput
    output: GetRepoContentsOutput
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

structure GetRepoContentsInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpLabel
    filePath: String

    @httpQuery("ref")
    ref: String
}

structure GetRepoContentsOutput {
    @required
    @httpPayload
    body: GetRepoContentsBody
}

structure GetRepoContentsBody {
    @required
    contents: FileEntryList
}

// ─── Archivos: subir ─────────────────────────────────────────
@http(method: "PUT", uri: "/v1/repos/{owner}/{repo}/contents/{filePath+}", code: 201)
@idempotent
@documentation("Sube o actualiza un archivo en el repositorio.")
operation UploadFile {
    input: UploadFileInput
    output: UploadFileOutput
    errors: [
        BadRequestError
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

structure UploadFileInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpLabel
    filePath: String

    @required
    @httpPayload
    body: UploadFileBody
}

structure UploadFileBody {
    @required
    content: String

    @required
    @length(min: 1, max: 500)
    message: String

    branch: String
}

structure UploadFileOutput {
    @required
    @httpPayload
    body: FileEntryDTO
}

// ─── Archivos: eliminar ───────────────────────────────────────
@http(method: "DELETE", uri: "/v1/repos/{owner}/{repo}/contents/{filePath+}", code: 204)
@idempotent
@documentation("Elimina un archivo generando un commit de borrado.")
operation DeleteFile {
    input: DeleteFileInput
    output: Unit
    errors: [
        BadRequestError
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

structure DeleteFileInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpLabel
    filePath: String

    @required
    @httpQuery("message")
    @length(min: 1, max: 500)
    message: String

    @httpQuery("branch")
    branch: String
}

// ─── Archivos: descargar ZIP ──────────────────────────────────
@http(method: "GET", uri: "/v1/repos/{owner}/{repo}/archive", code: 200)
@readonly
@documentation("Descarga el repositorio completo en ZIP.")
operation DownloadArchive {
    input: DownloadArchiveInput
    output: DownloadArchiveOutput
    errors: [
        UnauthorizedError
        ForbiddenError
        NotFoundError
        InternalServerError
    ]
}

structure DownloadArchiveInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @httpQuery("ref")
    ref: String
}

structure DownloadArchiveOutput {
    @required
    @httpHeader("Content-Type")
    contentType: String

    @required
    @httpHeader("Content-Disposition")
    contentDisposition: String
}

// ─── Branches ─────────────────────────────────────────────────
structure BranchDTO {
    @required
    name: String

    @required
    isDefault: Boolean

    @required
    commitSha: String
}

list BranchList {
    member: BranchDTO
}

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

structure RepoScopeInput with [RepoScopedInputMixin] {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName
}

structure ListBranchesOutput {
    @required
    @httpPayload
    body: ListBranchesBody
}

structure ListBranchesBody {
    @required
    branches: BranchList
}

@http(method: "GET", uri: "/v1/repos/{owner}/{repo}/branches/{branch}", code: 200)
@readonly
@documentation("Obtiene el detalle de una rama específica.")
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

structure GetBranchInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpLabel
    branch: String
}

structure GetBranchOutput {
    @required
    @httpPayload
    body: BranchDTO
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

structure CreateBranchInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpPayload
    body: CreateBranchBody
}

structure CreateBranchBody {
    @required
    @length(min: 1, max: 100)
    name: String

    @required
    fromBranch: String
}

structure CreateBranchOutput {
    @required
    @httpPayload
    body: BranchDTO
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

structure DeleteBranchInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpLabel
    branch: String
}

// ─── HU-23: Stars ─────────────────────────────────────────────
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

// ─── HU-22: Colaboradores ─────────────────────────────────────
structure CollaboratorDTO {
    @required
    userId: Uuid

    @required
    username: Username

    @required
    role: CollaboratorRole

    avatarUrl: Url

    @required
    addedAt: String
}

list CollaboratorList {
    member: CollaboratorDTO
}

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

structure ListCollaboratorsOutput {
    @required
    @httpPayload
    body: ListCollaboratorsBody
}

structure ListCollaboratorsBody {
    @required
    collaborators: CollaboratorList
}

@http(method: "GET", uri: "/v1/repos/{owner}/{repo}/collaborators/{collaboratorUsername}", code: 200)
@readonly
@documentation("Obtiene detalle de un colaborador específico.")
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

structure GetCollaboratorOutput {
    @required
    @httpPayload
    body: CollaboratorDTO
}

structure CollaboratorScopeInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpLabel
    collaboratorUsername: Username
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

structure AddCollaboratorInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpPayload
    body: AddCollaboratorBody
}

structure AddCollaboratorBody {
    @required
    username: Username

    @required
    role: CollaboratorRole
}

structure AddCollaboratorOutput {
    @required
    @httpPayload
    body: CollaboratorDTO
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

structure UpdateCollaboratorRoleInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpLabel
    collaboratorUsername: Username

    @required
    @httpPayload
    body: UpdateCollaboratorRoleBody
}

structure UpdateCollaboratorRoleBody {
    @required
    role: CollaboratorRole
}

structure UpdateCollaboratorRoleOutput {
    @required
    @httpPayload
    body: CollaboratorDTO
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

structure RemoveCollaboratorInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpLabel
    collaboratorUsername: Username
}

@title("GitHub Repo API")
@restJson1
@httpBearerAuth
@documentation("Servicio de repositorios, ramas, estrellas, colaboradores y contenido basico del repo.")
service RepoApi {
    version: "1.0.0"
    operations: [
        ListMyRepositories
        CreateRepository
        GetRepository
        UpdateRepository
        DeleteRepository
        ForkRepository
        ListRepositoryForks
        GetRepoContents
        UploadFile
        DeleteFile
        DownloadArchive
        ListBranches
        GetBranch
        CreateBranch
        DeleteBranch
        StarRepository
        UnstarRepository
        StarRepositoryForAuthenticatedUser
        UnstarRepositoryForAuthenticatedUser
        ListCollaborators
        GetCollaborator
        AddCollaboratorByUsername
        AddCollaboratorWithRole
        UpdateCollaboratorRole
        RemoveCollaborator
    ]
}
