$version: "2"

namespace com.github.repo

use aws.protocols#restJson1
use smithy.api#documentation
use smithy.api#httpBearerAuth
use smithy.api#title

@title("GitHub Social API")
@restJson1
@httpBearerAuth
@documentation("Servicio de dominio para acciones sociales del repositorio (stars).")
service SocialApi {
    version: "1.0.0"
    operations: [
        StarRepository
        UnstarRepository
        StarRepositoryForAuthenticatedUser
        UnstarRepositoryForAuthenticatedUser
    ]
}
