plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "multiplex"

include("multiplex-shared-kernel")
include("multiplex-common-utils")
