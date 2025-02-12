plugins {
    id("multiplex.db-access-conventions")
    id("multiplex.json-conventions")
    id("multiplex.rest-conventions")
    kotlin("plugin.serialization") version "2.1.0"
}

dependencies {
    implementation(project(":multiplex-shared-kernel"))
}
