package pl.kskarzynski.multiplex.integration.movies

import io.kotest.core.spec.style.FeatureSpec
import org.koin.test.KoinTest
import org.koin.test.inject
import pl.kskarzynski.multiplex.rooms.service.data.RoomRepository

class RoomApiIntegrationTest : KoinTest, FeatureSpec() {

    val roomRepository by inject<RoomRepository>()

    init {
        feature("Getting a room") {
            scenario("Room exists") {

            }

            scenario("Room does not exist") {

            }
        }

        feature("Creating a Room") {
            scenario("Room is valid") {

            }

            scenario("Room is not valid") {

            }
        }

        feature("Updating a Room") {
            scenario("Room is valid") {

            }

            scenario("Room does not exist") {

            }

            scenario("Room is not valid") {

            }
        }
    }
}