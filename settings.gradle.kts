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
