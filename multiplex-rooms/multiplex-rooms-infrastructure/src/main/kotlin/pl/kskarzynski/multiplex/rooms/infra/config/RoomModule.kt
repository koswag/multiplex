package pl.kskarzynski.multiplex.rooms.infra.config

import org.koin.dsl.module
import pl.kskarzynski.multiplex.domain.ports.RoomRepository
import pl.kskarzynski.multiplex.rooms.api.service.RoomService
import pl.kskarzynski.multiplex.rooms.infra.adapter.api.service.RoomServiceImpl
import pl.kskarzynski.multiplex.rooms.infra.adapter.data.DatabaseRoomRepository

val RoomModule =
    module {
        single<RoomRepository> { DatabaseRoomRepository() }
        single<RoomService> { RoomServiceImpl(roomQueries = get()) }
    }
