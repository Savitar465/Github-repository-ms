$version: "2"

namespace com.github.repo

use com.github.common#BadRequestError
use com.github.common#ForbiddenError
use com.github.common#InternalServerError
use com.github.common#NotFoundError
use com.github.common#UnauthorizedError
use smithy.api#documentation
use smithy.api#http
use smithy.api#idempotent
use smithy.api#readonly

@http(method: "GET", uri: "/v1/repos/{owner}/{repo}/contents", code: 200)
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

@http(method: "PUT", uri: "/v1/repos/{owner}/{repo}/contents", code: 201)
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

@http(method: "DELETE", uri: "/v1/repos/{owner}/{repo}/contents", code: 204)
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
