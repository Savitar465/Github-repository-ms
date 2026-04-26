$version: "2"

namespace com.github.repo

use aws.protocols#restJson1
use smithy.api#documentation
use smithy.api#httpBearerAuth
use smithy.api#title

@title("GitHub Repository API")
@restJson1
@httpBearerAuth
@documentation("Servicio de dominio para repositorios (CRUD y forks).")
service RepositoryApi {
    version: "1.0.0"
    operations: [
        ListMyRepositories
        CreateRepository
        GetRepository
        UpdateRepository
        DeleteRepository
        ForkRepository
        ListRepositoryForks
    ]
}
