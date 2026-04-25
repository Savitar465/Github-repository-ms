$version: "2"

namespace com.github.repo

use com.github.common#RepoName
use com.github.common#Username
use smithy.api#httpLabel
use smithy.api#httpPayload
use smithy.api#length
use smithy.api#required

structure BranchDTO {
    @required
    name: String

    @required
    isDefault: Boolean

    @required
    commitSha: String
}

structure ListBranchesOutput {
    @required
    @httpPayload
    body: ListBranchesBody
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
