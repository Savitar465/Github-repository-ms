$version: "2"

namespace com.github.repo

use com.github.common#PaginationMeta
use com.github.common#RepoName
use com.github.common#RepoScopedInputMixin
use com.github.common#Username
use smithy.api#httpLabel
use smithy.api#required

list RepositoryList {
    member: RepositoryDTO
}

structure RepoScopeInput with [RepoScopedInputMixin] {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName
}

structure ListRepositoriesBody {
    @required
    repositories: RepositoryList

    @required
    pagination: PaginationMeta
}

structure ListRepositoryForksBody {
    @required
    repositories: RepositoryList
}

list BranchList {
    member: BranchDTO
}

structure ListBranchesBody {
    @required
    branches: BranchList
}

list CollaboratorList {
    member: CollaboratorDTO
}

structure ListCollaboratorsBody {
    @required
    collaborators: CollaboratorList
}
