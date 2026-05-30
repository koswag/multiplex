@file:UseSerializers(RoomIdSerializer::class)

package pl.kskarzynski.multiplex.rooms.service.rest

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.resources.*
import io.ktor.server.application.Application
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.routing
import kotlinx.serialization.UseSerializers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.kskarzynski.multiplex.common.infra.json.serializer.RoomIdSerializer
import pl.kskarzynski.multiplex.common.infra.ktor.respond
import pl.kskarzynski.multiplex.rooms.service.rest.RoomValidationResult.Failure
import pl.kskarzynski.multiplex.rooms.service.rest.RoomValidationResult.Success
import pl.kskarzynski.multiplex.rooms.service.rest.dto.CreateRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.PatchRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError
import pl.kskarzynski.multiplex.shared.room.RoomId

@Resource("/api/rooms")
private class Rooms {

    @Resource("/{roomId}")
    class Get(val parent: Rooms, val roomId: RoomId)

    @Resource("")
    class Create(val parent: Rooms)

    @Resource("/{roomId}")
    class Update(val parent: Rooms, val roomId: RoomId)
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
                when (
                    val creationResult = roomRestService.createRoom(dto)
                ) {
                    is Success -> call.respond(Created, creationResult.room)
                    is Failure -> call.respond<RoomValidationError>(BadRequest, creationResult.errors)
                }
            }

            patch<Rooms.Update> { params ->
                val patch = call.receive<PatchRoomDto>()
                when (
                    val updateResult = roomRestService.updateRoom(params.roomId, patch)
                ) {
                    null -> call.respond(NotFound)
                    is Success -> call.respond(updateResult.room)
                    is Failure -> call.respond<RoomValidationError>(BadRequest, updateResult.errors)
                }
            }
        }
    }
}
