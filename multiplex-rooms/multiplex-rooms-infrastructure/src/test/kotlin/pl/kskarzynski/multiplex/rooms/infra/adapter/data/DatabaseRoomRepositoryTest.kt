package pl.kskarzynski.multiplex.rooms.infra.adapter.data

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.domain.ports.RoomRepository
import pl.kskarzynski.multiplex.rooms.infra.adapter.data.table.RoomSeatTable
import pl.kskarzynski.multiplex.rooms.infra.adapter.data.table.RoomTable
import pl.kskarzynski.multiplex.rooms.infra.util.DEFAULT_ROOM_ID
import pl.kskarzynski.multiplex.rooms.infra.util.room
import pl.kskarzynski.multiplex.rooms.infra.util.seat
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

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
            val result = roomRepository.findRoom(nonExistentRoomId)

            // then:
            expectThat(result).isNull()
        }

        scenario("Table contains other room") {
            // given:
            val existentRoom = room()
            val nonExistentRoomId = RoomId.generate()

            insertRoom(existentRoom)

            // when:
            val result = roomRepository.findRoom(nonExistentRoomId)

            // then:
            expectThat(result).isNull()
        }

        scenario("Room is in the table") {
            // given:
            val existentRoom = room()
            insertRoom(existentRoom)

            // when:
            val result = roomRepository.findRoom(existentRoom.id)

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
            roomRepository.saveRoom(room)

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
            roomRepository.saveRoom(newRoom)

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
                    seats = listOf(
                        seat(1, 1), seat(1, 2), seat(1, 3),
                        seat(2, 1), seat(2, 2), seat(2, 3),
                    ),
                )

            insertRoom(existentRoom)

            // when:
            val updatedRoom =
                existentRoom.copy(
                    seats = listOf(
                        seat(1, 1), seat(1, 2),
                        seat(2, 1), seat(2, 2),
                    )
                )

            roomRepository.saveRoom(updatedRoom)

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

private fun insertRoom(room: Room) {
    transaction {
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

private fun findRoom(roomId: RoomId): Room? =
    transaction {
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
                )
            }
    }
