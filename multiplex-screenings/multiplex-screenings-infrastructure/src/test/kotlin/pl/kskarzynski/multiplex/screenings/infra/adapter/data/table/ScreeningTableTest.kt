package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import pl.kskarzynski.multiplex.common.test.arbs.screeningId
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.ScreeningData
import pl.kskarzynski.multiplex.screenings.infra.util.screening
import pl.kskarzynski.multiplex.screenings.infra.util.screeningData
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class ScreeningTableTest : FeatureSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    beforeSpec {
        val dataSource = installPostgresContainer()
        initializeDatabase(dataSource, ScreeningTable)
    }

    feature("Finding a Screening by ID") {
        scenario("Table is empty") {
            // given:
            val nonExistentScreeningId = Arb.screeningId().next()

            // when:
            val result = transaction { ScreeningTable.find(nonExistentScreeningId) }

            // then:
            expectThat(result).isNull()
        }

        scenario("Other screenings exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            transaction { ScreeningTable.insertAll(otherScreenings) }

            val nonExistentScreeningId = Arb.screeningId().next()

            // when:
            val result = transaction { ScreeningTable.find(nonExistentScreeningId) }

            // then:
            expectThat(result).isNull()
        }

        scenario("Screening exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            val screening = Arb.screeningData().next()
            transaction { ScreeningTable.insertAll(otherScreenings + screening) }

            // when:
            val result = transaction { ScreeningTable.find(screening.id) }

            // then:
            expectThat(result) isEqualTo screening
        }
    }

    feature("Finding Screenings by IDs") {
        scenario("Table is empty") {
            // given:
            val nonExistentScreeningIds = List(5) { Arb.screeningId().next() }

            // when:
            val result = transaction { ScreeningTable.findAll(nonExistentScreeningIds) }

            // then:
            expectThat(result).isEmpty()
        }

        scenario("Other screenings exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            transaction { ScreeningTable.insertAll(otherScreenings) }

            val nonExistentScreeningIds = List(5) { Arb.screeningId().next() }

            // when:
            val result = transaction { ScreeningTable.findAll(nonExistentScreeningIds) }

            // then:
            expectThat(result).isEmpty()
        }

        scenario("Screenings exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            transaction { ScreeningTable.insertAll(otherScreenings) }

            val screenings = List(5) { Arb.screeningData().next() }
            transaction { ScreeningTable.insertAll(screenings) }
            val screeningIds = screenings.map { it.id }

            // when:
            val result = transaction { ScreeningTable.findAll(screeningIds) }

            // then:
            expectThat(result) containsExactlyInAnyOrder screenings
        }

        scenario("Part of the screenings exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            transaction { ScreeningTable.insertAll(otherScreenings) }

            val screenings = List(5) { Arb.screeningData().next() }
            transaction { ScreeningTable.insertAll(screenings) }
            val screeningIds = screenings.map { it.id }

            val nonExistentScreeningIds = List(5) { Arb.screeningId().next() }

            // when:
            val result = transaction { ScreeningTable.findAll(screeningIds + nonExistentScreeningIds) }

            // then:
            expectThat(result) containsExactlyInAnyOrder screenings
        }
    }

    feature("Saving a screening") {
        scenario("Table is empty") {
            // given:
            val screening = Arb.screening().next()

            // when:
            transaction {
                ScreeningTable.save(screening)
            }

            // then:
            val saved = transaction { ScreeningTable.find(screening.id) }
            expectThat(saved).isNotNull() and {
                get { id } isEqualTo screening.id
                get { movieId } isEqualTo screening.movieId
                get { roomId } isEqualTo screening.room.id
                get { startTime } isEqualTo screening.startTime
            }
        }

        scenario("Other screenings exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            transaction { ScreeningTable.insertAll(otherScreenings) }

            val screening = Arb.screening().next()

            // when:
            transaction {
                ScreeningTable.save(screening)
            }

            // then:
            val saved = transaction { ScreeningTable.find(screening.id) }
            expectThat(saved).isNotNull() and {
                get { id } isEqualTo screening.id
                get { movieId } isEqualTo screening.movieId
                get { roomId } isEqualTo screening.room.id
                get { startTime } isEqualTo screening.startTime
            }
        }

        scenario("Screening already exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            transaction { ScreeningTable.insertAll(otherScreenings) }

            val screening = Arb.screening().next()
            transaction { ScreeningTable.insert(screening) }

            // when:
            transaction {
                ScreeningTable.save(screening)
            }

            // then:
            val saved = transaction { ScreeningTable.find(screening.id) }
            expectThat(saved).isNotNull() and {
                get { id } isEqualTo screening.id
                get { movieId } isEqualTo screening.movieId
                get { roomId } isEqualTo screening.room.id
                get { startTime } isEqualTo screening.startTime
            }
        }
    }

})

private fun ScreeningTable.insertAll(screenings: Collection<ScreeningData>) {
    for (screening in screenings) {
        insert(screening)
    }
}

private fun ScreeningTable.insert(screening: ScreeningData) {
    insert {
        it[id] = screening.id.value
        it[movieId] = screening.movieId.value
        it[roomId] = screening.roomId.value
        it[startTime] = screening.startTime.value
        it[version] = screening.version.value
    }
}

private fun ScreeningTable.insert(screening: Screening) {
    insert {
        it[id] = screening.id.value
        it[movieId] = screening.movieId.value
        it[roomId] = screening.room.id.value
        it[startTime] = screening.startTime.value
        it[version] = screening.version.value
    }
}
