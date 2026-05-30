@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.integration.screening

import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import pl.kskarzynski.multiplex.common.test.arbs.screeningId
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.integration.configureClient
import pl.kskarzynski.multiplex.integration.util.assertions.hasContentTypeJsonUtf8
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.PatchScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto.PastScreeningTime
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.toDto
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.one
import kotlin.uuid.ExperimentalUuidApi

class ScreeningUpdateApiIntegrationTest : ScreeningApiIntegrationTest() {

    init {
        feature("Updating a Screening") {
            scenario("Screening does not exist") {
                testApplication {
                    setupMultiplexApplication()
                    val client = configureClient()

                    // given:
                    val nonExistentScreeningId = Arb.screeningId().next()
                    val emptyPatch = PatchScreeningDto(startTime = null)

                    // when:
                    val response = client.patch("/api/screenings/$nonExistentScreeningId") {
                        contentType(Json)
                        setBody(emptyPatch)
                    }

                    // then:
                    expectThat(response.status) isEqualTo NotFound
                }
            }

            scenario("Past Screening time") {
                testApplication {
                    setupMultiplexApplication()
                    val client = configureClient()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val screening = createScreening(movie, room)

                    // when:
                    val patch = PatchScreeningDto(startTime = fixedClock.currentTime().minusDays(1))
                    val response = client.patch("/api/screenings/${screening.id}") {
                        contentType(Json)
                        setBody(patch)
                    }

                    // then:
                    expect {
                        that(response) {
                            get { status } isEqualTo BadRequest
                            hasContentTypeJsonUtf8()
                        }

                        that(response.body<List<ScreeningValidationErrorDto>>()) {
                            hasSize(1)
                            one {
                                isA<PastScreeningTime>() and {
                                    get { screeningTime } isEqualTo patch.startTime
                                }
                            }
                        }
                    }
                }
            }

            scenario("Valid patch on an existent Screening") {
                testApplication {
                    setupMultiplexApplication()
                    val client = configureClient()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val screening = createScreening(movie, room)

                    // when:
                    val newStartTime = fixedClock.currentTime().plusDays(1)
                    val patch = PatchScreeningDto(startTime = newStartTime)
                    val response = client.patch("/api/screenings/${screening.id}") {
                        contentType(Json)
                        setBody(patch)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo OK
                        hasContentTypeJsonUtf8()
                    }

                    val expectedDto = screening.toDto(movie)
                        .copy(startTime = newStartTime)

                    expectThat(response.body<ScreeningDto>()) isEqualTo expectedDto

                    val existentScreening = screeningRepository.findScreening(screening.id)?.toDto(movie)
                    expectThat(existentScreening) isEqualTo expectedDto
                }
            }

            // FIXME: Doesn't work
            xscenario("Screening was updated in the meantime") {
                testApplication {
                    setupMultiplexApplication()
                    val client = configureClient()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val screening = createScreening(movie, room)

                    // when:
                    val newStartTime = fixedClock.currentTime().plusDays(1)
                    val concurrentPatches = List(2) { PatchScreeningDto(startTime = newStartTime) }
                    val responses = concurrentPatches.map { patch ->
                        async {
                            client.patch("/api/screenings/${screening.id}") {
                                contentType(Json)
                                setBody(patch)
                            }
                        }
                    }.awaitAll()

                    // then:
                    expectThat(responses) {
                        one { get { status } isEqualTo OK }
                        one { get { status } isEqualTo Conflict }
                    }
                }
            }
        }
    }
}