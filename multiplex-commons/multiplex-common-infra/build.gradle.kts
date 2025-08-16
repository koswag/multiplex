plugins {
    id("multiplex.db-access-convention")
    id("multiplex.json-convention")
    id("multiplex.rest-convention")
    kotlin("plugin.serialization") version "2.2.0"
}

dependencies {
    implementation(project(":multiplex-shared-kernel"))
}
