@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.rooms.service

import arrow.core.nonEmptyListOf
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.koin.KoinExtension
import io.kotest.property.Arb
import io.kotest.property.arbitrary.negativeInt
import io.kotest.property.arbitrary.next
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import pl.kskarzynski.multiplex.common.test.arbs.*
import pl.kskarzynski.multiplex.rooms.service.data.RoomRepository
import pl.kskarzynski.multiplex.rooms.service.rest.RoomRestService
import pl.kskarzynski.multiplex.rooms.service.rest.RoomValidationResult.Failure
import pl.kskarzynski.multiplex.rooms.service.rest.RoomValidationResult.Success
import pl.kskarzynski.multiplex.rooms.service.rest.dto.CreateRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.PatchRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.*
import pl.kskarzynski.multiplex.rooms.service.rest.dto.SeatDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.toDto
import pl.kskarzynski.multiplex.rooms.service.util.FakeRoomRepository
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.uuid.ExperimentalUuidApi

private val TestRoomModule = module {
    single<RoomRepository> { FakeRoomRepository() }
    single<RoomRestService> { RoomRestService(roomRepository = get()) }
}

class RoomRestServiceTest : KoinTest, FeatureSpec() {

    private val roomRestService by inject<RoomRestService>()
    private val roomRepository by inject<RoomRepository>()

    init {
        isolationMode = IsolationMode.InstancePerLeaf
        extensions(KoinExtension(TestRoomModule))

        feature("Getting a Room") {
            scenario("Room does not exist") {
                // given:
                val nonExistentRoomId = Arb.roomId().next()

                // when:
                val room = roomRestService.getRoom(nonExistentRoomId)

                // then:
                expectThat(room).isNull()
            }

            scenario("Room exists") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                // when:
                val room = roomRestService.getRoom(existentRoom.id)

                // then:
                expectThat(room).isNotNull() and {
                    get { id } isEqualTo existentRoom.id.value
                    get { number } isEqualTo existentRoom.number.value
                    get { seats } isEqualTo existentRoom.seats.map { it.toDto() }
                }
            }
        }

        feature("Creating a Room") {
            scenario("Room is valid") {
                // given:
                val createRoomDto = Arb.room().next().toCreateRoomDto()

                // when:
                val result = roomRestService.createRoom(createRoomDto)

                // then:
                expectThat(result).isA<Success>() and {
                    get { room.number } isEqualTo createRoomDto.number
                    get { room.seats } isEqualTo createRoomDto.seats
                }

                val roomNumber = RoomNumber(createRoomDto.number)
                val savedRoom = roomRepository.findByNumber(roomNumber)
                expectThat(savedRoom).isNotNull()
            }

            scenario("Room number already exists") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val createRoomDto = CreateRoomDto(
                    number = existentRoom.number.value,
                    seats = nonEmptyListOf(SeatDto(1, 1)),
                )

                // when:
                val result = roomRestService.createRoom(createRoomDto)

                // then:
                expectThat(result).isA<Failure>() and {
                    get { errors }.containsExactlyInAnyOrder(
                        RoomNumberAlreadyExists(createRoomDto.number),
                    )
                }
            }

            scenario("Invalid seat") {
                // given:
                val invalidSeatRow = Arb.negativeInt().next()
                val invalidSeatNumber = Arb.negativeInt().next()

                val createRoomDto = CreateRoomDto(
                    number = Arb.roomNumber().next().value,
                    seats = nonEmptyListOf(
                        SeatDto(invalidSeatRow, invalidSeatNumber),
                    ),
                )

                // when:
                val result = roomRestService.createRoom(createRoomDto)

                // then:
                expectThat(result).isA<Failure>() and {
                    get { errors }.containsExactlyInAnyOrder(
                        InvalidSeatRow(invalidSeatRow, invalidSeatNumber),
                        InvalidSeatNumber(invalidSeatRow, invalidSeatNumber),
                    )
                }
            }

            scenario("Duplicated Seat") {
                // given:
                val seat = Arb.seat().next().toDto()

                val createRoomDto = CreateRoomDto(
                    number = Arb.roomNumber().next().value,
                    seats = nonEmptyListOf(seat, seat),
                )

                // when:
                val result = roomRestService.createRoom(createRoomDto)

                // then:
                expectThat(result).isA<Failure>() and {
                    get { errors }.containsExactlyInAnyOrder(
                        DuplicatedSeat(seat.row, seat.number),
                    )
                }
            }

            scenario("Missing Rows") {
                // given:
                val createRoomDto = CreateRoomDto(
                    number = Arb.roomNumber().next().value,
                    seats = nonEmptyListOf(
                        SeatDto(1, 1), SeatDto(1, 2), SeatDto(1, 3),
                        SeatDto(3, 1), SeatDto(3, 2), SeatDto(3, 3),
                        SeatDto(5, 1), SeatDto(5, 2), SeatDto(5, 3),
                    ),
                )

                // when:
                val result = roomRestService.createRoom(createRoomDto)

                // then:
                expectThat(result).isA<Failure>() and {
                    get { errors }.containsExactlyInAnyOrder(
                        MissingRow(2),
                        MissingRow(4),
                    )
                }
            }

            scenario("Missing Seats") {
                // given:
                val createRoomDto = CreateRoomDto(
                    number = Arb.roomNumber().next().value,
                    seats = nonEmptyListOf(
                        SeatDto(1, 1), SeatDto(1, 2), SeatDto(1, 3),
                        SeatDto(2, 1), SeatDto(2, 2), SeatDto(2, 3),
                        SeatDto(3, 1), SeatDto(3, 3),
                        SeatDto(4, 1), SeatDto(4, 2), SeatDto(4, 4),
                    ),
                )

                // when:
                val result = roomRestService.createRoom(createRoomDto)

                // then:
                expectThat(result).isA<Failure>() and {
                    get { errors }.containsExactlyInAnyOrder(
                        MissingSeat(3, 2),
                        MissingSeat(4, 3),
                    )
                }
            }
        }

        feature("Updating a Room") {
            scenario("Room does not exist") {
                // given:
                val nonExistentRoomId = Arb.roomId().next()
                val patchRoomDto = Arb.room().next().toPatchRoomDto()

                // when:
                val result = roomRestService.updateRoom(nonExistentRoomId, patchRoomDto)

                // then:
                expectThat(result).isNull()
            }

            scenario("Empty patch") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val patchRoomDto = PatchRoomDto()

                // when:
                val result = roomRestService.updateRoom(existentRoom.id, patchRoomDto)

                // then:
                expectThat(result).isA<Success>() and {
                    get { room } isEqualTo existentRoom.toDto()
                }

                val roomAfterUpdate = roomRepository.findById(existentRoom.id)
                expectThat(roomAfterUpdate) isEqualTo existentRoom
            }

            scenario("Patch with number only") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val newNumber = Arb.roomNumber().next()
                val patchRoomDto = PatchRoomDto(number = newNumber.value)

                // when:
                val result = roomRestService.updateRoom(existentRoom.id, patchRoomDto)

                // then:
                expectThat(result).isA<Success>() and {
                    get { room.id } isEqualTo existentRoom.id.value
                    get { room.number } isEqualTo newNumber.value
                    get { room.seats } isEqualTo existentRoom.seats.map { it.toDto() }
                }

                val roomAfterUpdate = roomRepository.findById(existentRoom.id)
                expectThat(roomAfterUpdate).isNotNull() and {
                    get { id } isEqualTo existentRoom.id
                    get { number } isEqualTo newNumber
                    get { seats } isEqualTo existentRoom.seats
                }
            }

            scenario("Patch with seats only") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val newSeats = Arb.seats().next()
                val patchRoomDto = PatchRoomDto(seats = newSeats.map { it.toDto() })

                // when:
                val result = roomRestService.updateRoom(existentRoom.id, patchRoomDto)

                // then:
                expectThat(result).isA<Success>() and {
                    get { room.id } isEqualTo existentRoom.id.value
                    get { room.number } isEqualTo existentRoom.number.value
                    get { room.seats } isEqualTo newSeats.map { it.toDto() }
                }

                val roomAfterUpdate = roomRepository.findById(existentRoom.id)
                expectThat(roomAfterUpdate).isNotNull() and {
                    get { id } isEqualTo existentRoom.id
                    get { number } isEqualTo existentRoom.number
                    get { seats } isEqualTo newSeats
                }
            }

            scenario("Complete patch") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val newNumber = Arb.roomNumber().next()
                val newSeats = Arb.seats().next()
                val patchRoomDto = PatchRoomDto(
                    number = newNumber.value,
                    seats = newSeats.map { it.toDto() },
                )

                // when:
                val result = roomRestService.updateRoom(existentRoom.id, patchRoomDto)

                // then:
                expectThat(result).isA<Success>() and {
                    get { room.id } isEqualTo existentRoom.id.value
                    get { room.number } isEqualTo newNumber.value
                    get { room.seats } isEqualTo newSeats.map { it.toDto() }
                }

                val roomAfterUpdate = roomRepository.findById(existentRoom.id)
                expectThat(roomAfterUpdate).isNotNull() and {
                    get { id } isEqualTo existentRoom.id
                    get { number } isEqualTo newNumber
                    get { seats } isEqualTo newSeats
                }
            }

            scenario("Room number already exists") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val otherExistentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val patchRoomDto = PatchRoomDto(number = otherExistentRoom.number.value)

                // when:
                val result = roomRestService.updateRoom(existentRoom.id, patchRoomDto)

                // then:
                expectThat(result).isA<Failure>() and {
                    get { errors }.containsExactlyInAnyOrder(
                        RoomNumberAlreadyExists(otherExistentRoom.number.value),
                    )
                }
            }

            scenario("Invalid seat") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val invalidSeatRow = Arb.negativeInt().next()
                val invalidSeatNumber = Arb.negativeInt().next()

                val patchRoomDto = PatchRoomDto(
                    seats = nonEmptyListOf(
                        SeatDto(invalidSeatRow, invalidSeatNumber),
                    ),
                )

                // when:
                val result = roomRestService.updateRoom(existentRoom.id, patchRoomDto)

                // then:
                expectThat(result).isA<Failure>() and {
                    get { errors }.containsExactlyInAnyOrder(
                        InvalidSeatRow(invalidSeatRow, invalidSeatNumber),
                        InvalidSeatNumber(invalidSeatRow, invalidSeatNumber),
                    )
                }
            }

            scenario("Duplicated Seat") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val seat = Arb.seat().next().toDto()

                val patchRoomDto = PatchRoomDto(
                    seats = nonEmptyListOf(seat, seat),
                )

                // when:
                val result = roomRestService.updateRoom(existentRoom.id, patchRoomDto)

                // then:
                expectThat(result).isA<Failure>() and {
                    get { errors }.containsExactlyInAnyOrder(
                        DuplicatedSeat(seat.row, seat.number),
                    )
                }
            }

            scenario("Missing Rows") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val patchRoomDto = PatchRoomDto(
                    number = Arb.roomNumber().next().value,
                    seats = nonEmptyListOf(
                        SeatDto(1, 1), SeatDto(1, 2), SeatDto(1, 3),
                        SeatDto(3, 1), SeatDto(3, 2), SeatDto(3, 3),
                        SeatDto(5, 1), SeatDto(5, 2), SeatDto(5, 3),
                    ),
                )

                // when:
                val result = roomRestService.updateRoom(existentRoom.id, patchRoomDto)

                // then:
                expectThat(result).isA<Failure>() and {
                    get { errors }.containsExactlyInAnyOrder(
                        MissingRow(2),
                        MissingRow(4),
                    )
                }
            }

            scenario("Missing Seats") {
                // given:
                val existentRoom = Arb.room().next()
                    .also { roomRepository.save(it) }

                val patchRoomDto = PatchRoomDto(
                    seats = nonEmptyListOf(
                        SeatDto(1, 1), SeatDto(1, 2), SeatDto(1, 3),
                        SeatDto(2, 1), SeatDto(2, 2), SeatDto(2, 3),
                        SeatDto(3, 1), SeatDto(3, 3),
                        SeatDto(4, 1), SeatDto(4, 2), SeatDto(4, 4),
                    ),
                )

                // when:
                val result = roomRestService.updateRoom(existentRoom.id, patchRoomDto)

                // then:
                expectThat(result).isA<Failure>() and {
                    get { errors }.containsExactlyInAnyOrder(
                        MissingSeat(3, 2),
                        MissingSeat(4, 3),
                    )
                }
            }
        }
    }
}

private fun Room.toCreateRoomDto() =
    CreateRoomDto(
        number = number.value,
        seats = seats.map { it.toDto() },
    )

private fun Room.toPatchRoomDto() =
    PatchRoomDto(
        number = number.value,
        seats = seats.map { it.toDto() },
    )
