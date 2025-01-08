@file:UseSerializers(UuidSerializer::class)

package pl.kskarzynski.multiplex.rooms.service.rest

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import java.util.UUID
import kotlinx.serialization.UseSerializers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.common.infra.ktor.respond
import pl.kskarzynski.multiplex.rooms.service.rest.RoomValidationResult.Failure
import pl.kskarzynski.multiplex.rooms.service.rest.RoomValidationResult.Success
import pl.kskarzynski.multiplex.rooms.service.rest.dto.CreateRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.PatchRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError
import pl.kskarzynski.multiplex.shared.room.RoomId

@Resource("/api/rooms")
private class Rooms {

    @Resource("/{id}")
    class Get(val parent: Rooms, val id: UUID) {
        val roomId get() = RoomId(id)
    }

    @Resource("")
    class Create(val parent: Rooms)

    @Resource("/{id}")
    class Update(val parent: Rooms, val id: UUID) {
        val roomId get() = RoomId(id)
    }
}

object RoomRestModule : KoinComponent {

    private val roomRestService by inject<RoomRestService>()

    fun Application.roomModule() {
        routing {
            get<Rooms.Get> { params ->
                val room = roomRestService.getRoom(params.roomId)

                if (room != null) {
                    call.respond(room)
                } else {
                    call.respond(NotFound)
                }
            }

            post<Rooms.Create> {
                val dto = call.receive<CreateRoomDto>()
                val creationResult = roomRestService.createRoom(dto)

                when (creationResult) {
                    is Success -> call.respond(Created, creationResult.room)
                    is Failure -> call.respond<RoomValidationError>(BadRequest, creationResult.errors)
                }
            }

            patch<Rooms.Update> { params ->
                val patch = call.receive<PatchRoomDto>()
                val updateResult = roomRestService.updateRoom(params.roomId, patch)

                when (updateResult) {
                    null -> call.respond(NotFound)
                    is Success -> call.respond(updateResult.room)
                    is Failure -> call.respond<RoomValidationError>(BadRequest, updateResult.errors)
                }
            }
        }
    }
}
