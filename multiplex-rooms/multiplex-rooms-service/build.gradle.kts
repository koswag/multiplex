plugins {
    id("multiplex.db-access-convention")
    id("multiplex.di-convention")
    id("multiplex.json-convention")
    id("multiplex.rest-convention")
    id("multiplex.testing-convention")
    id("multiplex.tooling-convention")
    kotlin("plugin.serialization") version "2.2.0"
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-infra"))
    implementation(project(":multiplex-commons:multiplex-common-utils"))
    implementation(project(":multiplex-rooms:multiplex-rooms-api"))
    implementation(project(":multiplex-shared-kernel"))

    testImplementation(project(":multiplex-commons:multiplex-common-test"))
}
