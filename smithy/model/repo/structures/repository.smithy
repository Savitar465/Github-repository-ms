$version: "2"

namespace com.github.repo

use com.github.common#RepoName
use com.github.common#RepoVisibility
use com.github.common#Username
use com.github.common#Uuid
use smithy.api#httpLabel
use smithy.api#httpPayload
use smithy.api#httpQuery
use smithy.api#length
use smithy.api#range
use smithy.api#required

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

structure DeleteRepositoryInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName
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
    name: RepoName
    targetOwner: String
}

structure ForkRepositoryOutput {
    @required
    @httpPayload
    body: RepositoryDTO
}

structure ListRepositoryForksOutput {
    @required
    @httpPayload
    body: ListRepositoryForksBody
}
