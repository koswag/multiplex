plugins {
    id("multiplex.db-access-conventions")
    id("multiplex.json-conventions")
    id("multiplex.rest-conventions")
}

dependencies {
    implementation(project(":multiplex-shared-kernel"))
}
