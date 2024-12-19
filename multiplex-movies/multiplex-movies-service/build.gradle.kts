plugins {
    id("multiplex.db-access-conventions")
    id("multiplex.di-conventions")
    id("multiplex.rest-conventions")
    id("multiplex.testing-conventions")
    kotlin("plugin.serialization") version "2.1.0"
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-test"))
    implementation(project(":multiplex-commons:multiplex-common-infra"))
    implementation(project(":multiplex-movies:multiplex-movies-api"))
    implementation(project(":multiplex-shared-kernel"))
}
