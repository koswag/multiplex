package pl.kskarzynski.multiplex.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.Date
import pl.kskarzynski.multiplex.common.infra.auth.JWT_AUTH

object AuthenticationModule {

    // TODO: Move to config
    private const val SECRET = "secret"
    private const val ISSUER = "http://0.0.0.0:8080/"
    private const val AUDIENCE = "http://0.0.0.0:8080/hello"
    private const val REALM = "Access to Multiplex"

    fun Application.authModule() {
        routing {
            post("/login") {
                val user = call.receive<UserDto>()

                val token = JWT.create()
                    .withAudience(AUDIENCE)
                    .withIssuer(ISSUER)
                    .withClaim("username", user.username)
                    .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                    .sign(Algorithm.HMAC256(SECRET))

                call.respond(hashMapOf("token" to token))
            }
        }

        install(Authentication) {
            jwt(JWT_AUTH) {
                realm = REALM
                verifier(
                    JWT.require(Algorithm.HMAC256(SECRET))
                        .withAudience(AUDIENCE)
                        .withIssuer(ISSUER)
                        .build()
                )
            }
        }
    }
}
