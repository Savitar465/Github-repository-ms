$version: "2"

namespace com.github.repo

use aws.protocols#restJson1
use smithy.api#documentation
use smithy.api#httpBearerAuth
use smithy.api#title

@title("GitHub Collaborator API")
@restJson1
@httpBearerAuth
@documentation("Servicio de dominio para colaboradores y roles.")
service CollaboratorApi {
    version: "1.0.0"
    operations: [
        ListCollaborators
        GetCollaborator
        AddCollaboratorByUsername
        AddCollaboratorWithRole
        UpdateCollaboratorRole
        RemoveCollaborator
    ]
}
