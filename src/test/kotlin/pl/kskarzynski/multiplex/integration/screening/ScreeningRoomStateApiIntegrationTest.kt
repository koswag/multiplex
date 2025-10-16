package pl.kskarzynski.multiplex.integration.screening

import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import pl.kskarzynski.multiplex.common.test.arbs.screeningId
import pl.kskarzynski.multiplex.integration.configureClient
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningRoomDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.toScreeningRoomDto
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

class ScreeningRoomStateApiIntegrationTest : ScreeningApiIntegrationTest() {

    init {
        xfeature("Streaming room state") {
            scenario("Room state does not change") {
                testApplication {
                    setupMultiplexApplication()
                    val client = configureClient()

                    // given:
                    val screening = createScreening()

                    // when:
                    val response = client.get("/sse/screenings/${screening.id}/room")

                    // then:
                    expectThat(response).get { status } isEqualTo HttpStatusCode.OK

                    val roomStates = withContext(Dispatchers.IO) {
                        val reader = response.bodyAsChannel().toInputStream().reader().buffered()
                        buildList {
                            repeat(5) {
                                val event = reader.readLine()
                                val decoded = Json.decodeFromString<ScreeningRoomDto>(event)
                                add(decoded)
                            }
                        }
                    }
                    expectThat(roomStates.toSet())
                        .containsExactly(screening.toScreeningRoomDto())
                }
            }

            scenario("Seat is booked in the meantime") {
                testApplication {
                    setupMultiplexApplication()
                    val client = configureClient()

                    val nonExistentScreeningId = Arb.screeningId().next()
                    client.sse("/sse/screenings/$nonExistentScreeningId/room") {
                        incoming.take(5)
                            .collect {
                                val eventData = it.event ?: error("No event data")
                                val decoded = Json.decodeFromString<ScreeningRoomDto>(eventData)
                                println(decoded)
                            }
                    }
                }
            }

            scenario("Screening does not exist") {
                testApplication {
                    setupMultiplexApplication()
                    val client = configureClient()

                    val nonExistentScreeningId = Arb.screeningId().next()
                    client.sse("/sse/screenings/$nonExistentScreeningId/room") {
                        incoming.take(5)
                            .collect {
                                val eventData = it.event ?: error("No event data")
                                val decoded = Json.decodeFromString<ScreeningRoomDto>(eventData)
                                println(decoded)
                            }
                    }
                }
            }
        }
    }
}
