@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.rooms.service.data

import arrow.core.nonEmptyListOf
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.common.utils.arrow.toNonEmptyList
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomSeatTable
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomTable
import pl.kskarzynski.multiplex.rooms.service.util.DEFAULT_ROOM_ID
import pl.kskarzynski.multiplex.rooms.service.util.room
import pl.kskarzynski.multiplex.rooms.service.util.seat
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import kotlin.uuid.ExperimentalUuidApi

class DatabaseRoomRepositoryTest : FeatureSpec({

    isolationMode = IsolationMode.InstancePerTest

    val roomRepository: RoomRepository = DatabaseRoomRepository()

    beforeSpec {
        val dataSource = installPostgresContainer()
        initializeDatabase(dataSource, RoomTable, RoomSeatTable)
    }

    feature("Finding rooms") {
        scenario("Table is empty") {
            // given:
            val nonExistentRoomId = DEFAULT_ROOM_ID

            // when:
            val result = roomRepository.findById(nonExistentRoomId)

            // then:
            expectThat(result).isNull()
        }

        scenario("Table contains other room") {
            // given:
            val existentRoom = room()
            val nonExistentRoomId = RoomId.generate()

            insertRoom(existentRoom)

            // when:
            val result = roomRepository.findById(nonExistentRoomId)

            // then:
            expectThat(result).isNull()
        }

        scenario("Room is in the table") {
            // given:
            val existentRoom = room()
            insertRoom(existentRoom)

            // when:
            val result = roomRepository.findById(existentRoom.id)

            // then:
            expectThat(result).isNotNull() and {
                get { id } isEqualTo existentRoom.id
                get { number } isEqualTo existentRoom.number
                get { seats } isEqualTo existentRoom.seats
            }
        }
    }

    feature("Saving rooms") {
        scenario("Table is empty") {
            // given:
            val room = room()

            // when:
            roomRepository.save(room)

            // then:
            val foundRoom = findRoom(room.id)

            expectThat(foundRoom).isNotNull() and {
                get { id } isEqualTo room.id
                get { number } isEqualTo room.number
                get { seats } isEqualTo room.seats
            }
        }

        scenario("Table contains other room") {
            // given:
            val existentRoom = room(number = 1)
            val newRoom = room(number = 2)

            insertRoom(existentRoom)

            // when:
            roomRepository.save(newRoom)

            // then:
            val foundExistentRoom = findRoom(existentRoom.id)
            val foundNewRoom = findRoom(newRoom.id)

            expectThat(foundExistentRoom).isNotNull() and {
                get { id } isEqualTo existentRoom.id
                get { number } isEqualTo existentRoom.number
                get { seats } isEqualTo existentRoom.seats
            }

            expectThat(foundNewRoom).isNotNull() and {
                get { id } isEqualTo newRoom.id
                get { number } isEqualTo newRoom.number
                get { seats } isEqualTo newRoom.seats
            }
        }

        scenario("Table contains the same room with other seat configuration") {
            // given:
            val roomId = RoomId.generate()
            val existentRoom =
                room(
                    id = roomId.value,
                    number = 1,
                    seats = nonEmptyListOf(
                        seat(1, 1), seat(1, 2), seat(1, 3),
                        seat(2, 1), seat(2, 2), seat(2, 3),
                    ),
                )

            insertRoom(existentRoom)

            // when:
            val updatedRoom =
                existentRoom.copy(
                    seats = nonEmptyListOf(
                        seat(1, 1), seat(1, 2),
                        seat(2, 1), seat(2, 2),
                    )
                )

            roomRepository.save(updatedRoom)

            // then:
            val foundRoom = findRoom(roomId)

            expectThat(foundRoom).isNotNull() and {
                get { id } isEqualTo updatedRoom.id
                get { number } isEqualTo updatedRoom.number
                get { seats } isEqualTo updatedRoom.seats
            }
        }
    }

})

private suspend fun insertRoom(room: Room) {
    suspendTransaction {
        RoomTable.insert {
            it[id] = room.id.value
            it[number] = room.number.value
        }

        for (seat in room.seats) {
            RoomSeatTable.insert {
                it[roomId] = room.id.value
                it[row] = seat.row.value
                it[number] = seat.number.value
            }
        }
    }
}

private suspend fun findRoom(roomId: RoomId): Room? =
    suspendTransaction {
        RoomTable.selectAll()
            .where { RoomTable.id eq roomId.value }
            .firstOrNull()
            ?.let { row ->
                room(
                    id = roomId.value,
                    number = row[RoomTable.number],
                    seats =
                        RoomSeatTable.selectAll()
                            .where { RoomSeatTable.roomId eq roomId.value }
                            .map { seatRow ->
                                seat(seatRow[RoomSeatTable.row], seatRow[RoomSeatTable.number])
                            }
                            .toNonEmptyList()
                )
            }
    }
