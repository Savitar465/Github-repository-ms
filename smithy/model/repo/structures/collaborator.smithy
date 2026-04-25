$version: "2"

namespace com.github.repo

use com.github.common#CollaboratorRole
use com.github.common#RepoName
use com.github.common#Url
use com.github.common#Username
use com.github.common#Uuid
use smithy.api#httpLabel
use smithy.api#httpPayload
use smithy.api#required

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

structure ListCollaboratorsOutput {
    @required
    @httpPayload
    body: ListCollaboratorsBody
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

structure GetCollaboratorOutput {
    @required
    @httpPayload
    body: CollaboratorDTO
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
