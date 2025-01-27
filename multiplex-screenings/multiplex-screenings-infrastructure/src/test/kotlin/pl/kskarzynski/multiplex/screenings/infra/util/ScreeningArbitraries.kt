package pl.kskarzynski.multiplex.screenings.infra.util

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.localDateTime
import pl.kskarzynski.multiplex.common.test.arbs.movieId
import pl.kskarzynski.multiplex.common.test.arbs.room
import pl.kskarzynski.multiplex.common.test.arbs.roomId
import pl.kskarzynski.multiplex.common.test.arbs.screeningId
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.ScreeningData
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

internal fun Arb.Companion.screeningData(): Arb<ScreeningData> =
    arbitrary {
        val id = Arb.screeningId().bind()
        val movieId = Arb.movieId().bind()
        val roomId = Arb.roomId().bind()
        val startTime = Arb.localDateTime().bind()

        ScreeningData(id, movieId, roomId, ScreeningStartTime(startTime))
    }

fun Arb.Companion.screening(): Arb<Screening> =
    arbitrary {
        val id = Arb.screeningId().bind()
        val movieId = Arb.movieId().bind()
        val room = Arb.room().bind()
        val startTime = Arb.localDateTime().bind()
        val bookings = Arb.list(Arb.booking(), 0..5).bind()

        Screening(id, movieId, room, ScreeningStartTime(startTime), bookings)
    }
