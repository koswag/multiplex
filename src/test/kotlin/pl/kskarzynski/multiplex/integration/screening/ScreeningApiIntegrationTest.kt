@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.integration.screening

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.koin.KoinExtension
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.ktor.client.request.*
import io.ktor.server.testing.*
import org.koin.test.KoinTest
import org.koin.test.inject
import pl.kskarzynski.multiplex.auth.AuthenticationModule.authModule
import pl.kskarzynski.multiplex.common.test.arbs.movie
import pl.kskarzynski.multiplex.common.test.arbs.room
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.installPlugins
import pl.kskarzynski.multiplex.integration.arbs.screening
import pl.kskarzynski.multiplex.integration.util.TestClockModule
import pl.kskarzynski.multiplex.movies.service.config.MovieModule
import pl.kskarzynski.multiplex.movies.service.data.MovieRepository
import pl.kskarzynski.multiplex.movies.service.data.table.MovieTable
import pl.kskarzynski.multiplex.rooms.service.config.RoomModule
import pl.kskarzynski.multiplex.rooms.service.data.RoomRepository
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomSeatTable
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomTable
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.BookingTable
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.ScreeningTable
import pl.kskarzynski.multiplex.screenings.infra.config.ScreeningModule
import pl.kskarzynski.multiplex.screenings.infra.rest.ScreeningRestModule.screeningModule
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.CreateScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningListItemDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningListItemRoomDto
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.room.Room
import java.time.Clock
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

abstract class ScreeningApiIntegrationTest : KoinTest, FeatureSpec() {

    protected val screeningRepository by inject<ScreeningRepository>()
    protected val roomRepository by inject<RoomRepository>()
    protected val movieRepository by inject<MovieRepository>()

    protected val fixedClock by inject<Clock>()

    protected val validStartTime: LocalDateTime
        get() = fixedClock.currentTime().plusDays(1)

    override fun isolationMode() = IsolationMode.InstancePerLeaf

    override fun extensions() = listOf(
        KoinExtension(
            listOf(
                ScreeningModule,
                MovieModule,
                RoomModule,
                TestClockModule,
            ),
        ),
    )

    override suspend fun beforeSpec(spec: Spec) {
        initializeDatabase(
            datasource = installPostgresContainer(),
            RoomTable,
            RoomSeatTable,
            MovieTable,
            ScreeningTable,
            BookingTable,
        )
    }

    protected suspend fun createScreening(
        movie: Movie? = null,
        room: Room? = null,
        startTime: LocalDateTime? = null,
    ): Screening =
        Arb.screening(movie ?: createMovie(), room ?: createRoom(), startTime).next()
            .also { screeningRepository.save(it) }

    protected suspend fun createMovie(): Movie =
        Arb.movie().next()
            .also { movieRepository.save(it) }

    protected suspend fun createRoom(): Room =
        Arb.room().next()
            .also { roomRepository.save(it) }
}

fun TestApplicationBuilder.setupMultiplexApplication() {
    application {
        installPlugins()
        authModule()
        screeningModule()
    }
}

fun Screening.toCreateScreeningDto() =
    CreateScreeningDto(
        movieId = movieId.value,
        roomId = room.id.value,
        startTime = startTime.value,
    )

fun Screening.toListItemDto() =
    ScreeningListItemDto(
        id = id.value,
        startTime = startTime.value,
        room = room.toScreeningListItemRoom(),
    )

fun Room.toScreeningListItemRoom() =
    ScreeningListItemRoomDto(
        id = id.value,
        number = number.value,
    )

fun HttpRequestBuilder.withQueryParams(vararg params: Pair<String, Any>) {
    for ((key, value) in params) {
        parameter(key, value)
    }
}
