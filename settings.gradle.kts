plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "multiplex"

include("multiplex-commons")
include("multiplex-commons:multiplex-common-utils")
include("multiplex-commons:multiplex-common-test")
include("multiplex-movies")
include("multiplex-movies:multiplex-movies-domain")
include("multiplex-movies:multiplex-movies-api")
include("multiplex-movies:multiplex-movies-infrastructure")
include("multiplex-shared-kernel")
include("multiplex-rooms")
include("multiplex-rooms:multiplex-rooms-domain")
findProject(":multiplex-rooms:multiplex-rooms-domain")?.name = "multiplex-rooms-domain"
include("multiplex-rooms:multiplex-rooms-infrastructure")
findProject(":multiplex-rooms:multiplex-rooms-infrastructure")?.name = "multiplex-rooms-infrastructure"
include("multiplex-rooms")
include("multiplex-rooms:multiplex-rooms-domain")
findProject(":multiplex-rooms:multiplex-rooms-domain")?.name = "multiplex-rooms-domain"
include("multiplex-rooms:multiplex-rooms-api")
findProject(":multiplex-rooms:multiplex-rooms-api")?.name = "multiplex-rooms-api"
include("multiplex-rooms:multiplex-rooms-infrastructure")
findProject(":multiplex-rooms:multiplex-rooms-infrastructure")?.name = "multiplex-rooms-infrastructure"
include("multiplex-screenings")
include("multiplex-screenings:multiplex-screenings-domain")
findProject(":multiplex-screenings:multiplex-screenings-domain")?.name = "multiplex-screenings-domain"
include("multiplex-screenings:multiplex-screenings-infrastructure")
findProject(":multiplex-screenings:multiplex-screenings-infrastructure")?.name = "multiplex-screenings-infrastructure"
include("multiplex-commons:multiplex-common-infra")
findProject(":multiplex-commons:multiplex-common-infra")?.name = "multiplex-common-infra"
