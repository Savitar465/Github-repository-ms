$version: "2"

namespace com.github.repo

use com.github.common#RepoName
use com.github.common#Url
use com.github.common#Username
use smithy.api#httpHeader
use smithy.api#httpLabel
use smithy.api#httpPayload
use smithy.api#httpQuery
use smithy.api#length
use smithy.api#required

enum FileType {
    FILE = "file"
    DIRECTORY = "directory"
}

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

list FileEntryList {
    member: FileEntryDTO
}

structure GetRepoContentsInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpQuery("path")
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

structure UploadFileInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpQuery("path")
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

structure DeleteFileInput {
    @required
    @httpLabel
    owner: Username

    @required
    @httpLabel
    repo: RepoName

    @required
    @httpQuery("path")
    filePath: String

    @required
    @httpQuery("message")
    @length(min: 1, max: 500)
    message: String

    @httpQuery("branch")
    branch: String
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
