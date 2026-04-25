$version: "2"

namespace com.github.repo

use aws.protocols#restJson1
use smithy.api#documentation
use smithy.api#httpBearerAuth
use smithy.api#title

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
