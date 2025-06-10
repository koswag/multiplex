package pl.kskarzynski.multiplex.integration

import arrow.core.nonEmptyListOf
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.koin.KoinExtension
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.negativeInt
import io.kotest.property.arbitrary.next
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.TestApplicationBuilder
import io.ktor.server.testing.testApplication
import org.koin.test.KoinTest
import org.koin.test.inject
import pl.kskarzynski.multiplex.common.test.arbs.room
import pl.kskarzynski.multiplex.common.test.arbs.roomId
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.installPlugins
import pl.kskarzynski.multiplex.integration.util.assertions.hasContentTypeJsonUtf8
import pl.kskarzynski.multiplex.rooms.service.config.RoomModule
import pl.kskarzynski.multiplex.rooms.service.data.RoomRepository
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomSeatTable
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomTable
import pl.kskarzynski.multiplex.rooms.service.rest.RoomRestModule.roomModule
import pl.kskarzynski.multiplex.rooms.service.rest.dto.CreateRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.PatchRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.InvalidSeatNumber
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.InvalidSeatRow
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.RoomNumberAlreadyExists
import pl.kskarzynski.multiplex.rooms.service.rest.dto.SeatDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.toDto
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.one

class RoomApiIntegrationTest : KoinTest, FeatureSpec() {

    val roomRepository by inject<RoomRepository>()

    init {
        isolationMode = IsolationMode.InstancePerLeaf
        extensions(KoinExtension(RoomModule))

        beforeSpec {
            val datasource = installPostgresContainer()
            initializeDatabase(datasource, RoomTable, RoomSeatTable)
        }

        feature("Getting a room") {
            scenario("Room does not exist") {
                testApplication {
                    // given:
                    setupMultiplexApplication()

                    // when:
                    val nonExistentRoomId = Arb.roomId().next()
                    val response = client.get("/api/rooms/$nonExistentRoomId")

                    // then:
                    expectThat(response.status) isEqualTo HttpStatusCode.NotFound
                }
            }

            scenario("Room exists") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()

                    val existentRoom = Arb.room().next()
                        .also { roomRepository.save(it) }

                    // when:
                    val response = client.get("/api/rooms/${existentRoom.id}")

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        hasContentTypeJsonUtf8()
                    }

                    expectThat(response.body<RoomDto>()) {
                        get { id } isEqualTo existentRoom.id.value
                        get { number } isEqualTo existentRoom.number.value
                        get { seats } isEqualTo existentRoom.seats.map { it.toDto() }
                    }
                }
            }
        }

        feature("Creating a Room") {
            scenario("Room is valid") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()

                    // when:
                    val createRoomDto = Arb.createRoomDto().next()
                    val response = client.post("/api/rooms") {
                        contentType(ContentType.Application.Json)
                        setBody(createRoomDto)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.Created
                        hasContentTypeJsonUtf8()
                    }

                    expectThat(response.body<RoomDto>()) {
                        get { number } isEqualTo createRoomDto.number
                        get { seats } isEqualTo createRoomDto.seats
                    }
                }
            }

            scenario("Room is not valid") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()

                    val existentRoom = Arb.room().next()
                        .also { roomRepository.save(it) }

                    // when:
                    val invalidSeatRow = Arb.negativeInt().next()
                    val invalidSeatNumber = Arb.negativeInt().next()
                    val createRoomDto = CreateRoomDto(
                        number = existentRoom.number.value,
                        seats = nonEmptyListOf(SeatDto(invalidSeatRow, invalidSeatNumber))
                    )
                    val response = client.post("/api/rooms") {
                        contentType(ContentType.Application.Json)
                        setBody(createRoomDto)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.BadRequest
                        hasContentTypeJsonUtf8()
                    }

                    expectThat(response.body<List<RoomValidationError>>()) {
                        hasSize(3)
                        one {
                            isA<RoomNumberAlreadyExists>() and {
                                get { number } isEqualTo existentRoom.number.value
                            }
                        }
                        one {
                            isA<InvalidSeatRow>() and {
                                get { seatRow } isEqualTo invalidSeatRow
                                get { seatNumber } isEqualTo invalidSeatNumber
                            }
                        }
                        one {
                            isA<InvalidSeatNumber>() and {
                                get { seatRow } isEqualTo invalidSeatRow
                                get { seatNumber } isEqualTo invalidSeatNumber
                            }
                        }
                    }
                }
            }
        }

        feature("Updating a Room") {
            scenario("Room is valid") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()

                    val existentRoom = Arb.room().next()
                        .also { roomRepository.save(it) }

                    // when:
                    val createRoomDto = Arb.patchRoomDto().next()
                    val response = client.patch("/api/rooms/${existentRoom.id}") {
                        contentType(ContentType.Application.Json)
                        setBody(createRoomDto)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        hasContentTypeJsonUtf8()
                    }

                    expectThat(response.body<RoomDto>()) {
                        get { number } isEqualTo createRoomDto.number
                        get { seats } isEqualTo createRoomDto.seats
                    }
                }
            }

            scenario("Room does not exist") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()

                    // when:
                    val nonExistentRoomId = Arb.roomId().next()
                    val createRoomDto = Arb.patchRoomDto().next()
                    val response = client.patch("/api/rooms/$nonExistentRoomId") {
                        contentType(ContentType.Application.Json)
                        setBody(createRoomDto)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.NotFound
                    }
                }
            }

            scenario("Room is not valid") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()

                    val existentRoom = Arb.room().next()
                        .also { roomRepository.save(it) }

                    val otherExistentRoom = Arb.room().next()
                        .also { roomRepository.save(it) }

                    // when:
                    val invalidSeatRow = Arb.negativeInt().next()
                    val invalidSeatNumber = Arb.negativeInt().next()
                    val createRoomDto = CreateRoomDto(
                        number = otherExistentRoom.number.value,
                        seats = nonEmptyListOf(SeatDto(invalidSeatRow, invalidSeatNumber))
                    )
                    val response = client.patch("/api/rooms/${existentRoom.id}") {
                        contentType(ContentType.Application.Json)
                        setBody(createRoomDto)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.BadRequest
                        hasContentTypeJsonUtf8()
                    }

                    expectThat(response.body<List<RoomValidationError>>()) {
                        hasSize(3)
                        one {
                            isA<RoomNumberAlreadyExists>() and {
                                get { number } isEqualTo otherExistentRoom.number.value
                            }
                        }
                        one {
                            isA<InvalidSeatRow>() and {
                                get { seatRow } isEqualTo invalidSeatRow
                                get { seatNumber } isEqualTo invalidSeatNumber
                            }
                        }
                        one {
                            isA<InvalidSeatNumber>() and {
                                get { seatRow } isEqualTo invalidSeatRow
                                get { seatNumber } isEqualTo invalidSeatNumber
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun TestApplicationBuilder.setupMultiplexApplication() {
    application {
        installPlugins()
        roomModule()
    }
}

private fun Arb.Companion.createRoomDto(): Arb<CreateRoomDto> =
    room().map {
        CreateRoomDto(
            number = it.number.value,
            seats = it.seats.map { it.toDto() },
        )
    }

private fun Arb.Companion.patchRoomDto(): Arb<PatchRoomDto> =
    room().map {
        PatchRoomDto(
            number = it.number.value,
            seats = it.seats.map { it.toDto() },
        )
    }
