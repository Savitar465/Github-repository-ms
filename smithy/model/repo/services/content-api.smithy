$version: "2"

namespace com.github.repo

use aws.protocols#restJson1
use smithy.api#documentation
use smithy.api#httpBearerAuth
use smithy.api#title

@title("GitHub Content API")
@restJson1
@httpBearerAuth
@documentation("Servicio de dominio para contenidos y descarga de repositorio.")
service ContentApi {
    version: "1.0.0"
    operations: [
        GetRepoContents
        UploadFile
        DeleteFile
        DownloadArchive
    ]
}
