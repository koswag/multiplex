package pl.kskarzynski.multiplex.rooms.service.config

import org.koin.dsl.module
import pl.kskarzynski.multiplex.rooms.api.service.RoomService
import pl.kskarzynski.multiplex.rooms.service.api.service.RoomServiceImpl
import pl.kskarzynski.multiplex.rooms.service.data.DatabaseRoomRepository
import pl.kskarzynski.multiplex.rooms.service.data.RoomRepository

val RoomModule = module {
    single<RoomRepository> { DatabaseRoomRepository() }
    single<RoomService> { RoomServiceImpl(roomQueries = get()) }
}
