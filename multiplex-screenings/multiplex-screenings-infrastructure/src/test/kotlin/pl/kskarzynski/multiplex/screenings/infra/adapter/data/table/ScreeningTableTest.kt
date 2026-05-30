@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import pl.kskarzynski.multiplex.common.test.arbs.screeningId
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.ScreeningData
import pl.kskarzynski.multiplex.screenings.infra.util.screening
import pl.kskarzynski.multiplex.screenings.infra.util.screeningData
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.uuid.ExperimentalUuidApi

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
            val result = suspendTransaction { ScreeningTable.find(nonExistentScreeningId) }

            // then:
            expectThat(result).isNull()
        }

        scenario("Other screenings exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            suspendTransaction { ScreeningTable.insertAll(otherScreenings) }

            val nonExistentScreeningId = Arb.screeningId().next()

            // when:
            val result = suspendTransaction { ScreeningTable.find(nonExistentScreeningId) }

            // then:
            expectThat(result).isNull()
        }

        scenario("Screening exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            val screening = Arb.screeningData().next()
            suspendTransaction { ScreeningTable.insertAll(otherScreenings + screening) }

            // when:
            val result = suspendTransaction { ScreeningTable.find(screening.id) }

            // then:
            expectThat(result) isEqualTo screening
        }
    }

    feature("Finding Screenings by IDs") {
        scenario("Table is empty") {
            // given:
            val nonExistentScreeningIds = List(5) { Arb.screeningId().next() }

            // when:
            val result = suspendTransaction { ScreeningTable.findAll(nonExistentScreeningIds).toList() }

            // then:
            expectThat(result).isEmpty()
        }

        scenario("Other screenings exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            suspendTransaction { ScreeningTable.insertAll(otherScreenings) }

            val nonExistentScreeningIds = List(5) { Arb.screeningId().next() }

            // when:
            val result = suspendTransaction { ScreeningTable.findAll(nonExistentScreeningIds).toList() }

            // then:
            expectThat(result).isEmpty()
        }

        scenario("Screenings exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            suspendTransaction { ScreeningTable.insertAll(otherScreenings) }

            val screenings = List(5) { Arb.screeningData().next() }
            suspendTransaction { ScreeningTable.insertAll(screenings) }
            val screeningIds = screenings.map { it.id }

            // when:
            val result = suspendTransaction { ScreeningTable.findAll(screeningIds).toList() }

            // then:
            expectThat(result) containsExactlyInAnyOrder screenings
        }

        scenario("Part of the screenings exists") {
            // given:
            val otherScreenings = List(5) { Arb.screeningData().next() }
            suspendTransaction { ScreeningTable.insertAll(otherScreenings) }

            val screenings = List(5) { Arb.screeningData().next() }
            suspendTransaction { ScreeningTable.insertAll(screenings) }
            val screeningIds = screenings.map { it.id }

            val nonExistentScreeningIds = List(5) { Arb.screeningId().next() }

            // when:
            val result = suspendTransaction { ScreeningTable.findAll(screeningIds + nonExistentScreeningIds).toList() }

            // then:
            expectThat(result) containsExactlyInAnyOrder screenings
        }
    }

    feature("Saving a screening") {
        scenario("Table is empty") {
            // given:
            val screening = Arb.screening().next()

            // when:
            suspendTransaction {
                ScreeningTable.save(screening)
            }

            // then:
            val saved = suspendTransaction { ScreeningTable.find(screening.id) }
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
            suspendTransaction { ScreeningTable.insertAll(otherScreenings) }

            val screening = Arb.screening().next()

            // when:
            suspendTransaction {
                ScreeningTable.save(screening)
            }

            // then:
            val saved = suspendTransaction { ScreeningTable.find(screening.id) }
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
            suspendTransaction { ScreeningTable.insertAll(otherScreenings) }

            val screening = Arb.screening().next()
            suspendTransaction { ScreeningTable.insert(screening) }

            // when:
            suspendTransaction {
                ScreeningTable.save(screening)
            }

            // then:
            val saved = suspendTransaction { ScreeningTable.find(screening.id) }
            expectThat(saved).isNotNull() and {
                get { id } isEqualTo screening.id
                get { movieId } isEqualTo screening.movieId
                get { roomId } isEqualTo screening.room.id
                get { startTime } isEqualTo screening.startTime
            }
        }
    }

})

private suspend fun ScreeningTable.insertAll(screenings: Collection<ScreeningData>) {
    for (screening in screenings) {
        insert(screening)
    }
}

private suspend fun ScreeningTable.insert(screening: ScreeningData) {
    insert {
        it[id] = screening.id.value
        it[movieId] = screening.movieId.value
        it[roomId] = screening.roomId.value
        it[startTime] = screening.startTime.value
        it[version] = screening.version.value
    }
}

private suspend fun ScreeningTable.insert(screening: Screening) {
    insert {
        it[id] = screening.id.value
        it[movieId] = screening.movieId.value
        it[roomId] = screening.room.id.value
        it[startTime] = screening.startTime.value
        it[version] = screening.version.value
    }
}
