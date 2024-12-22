plugins {
    id("multiplex.db-access-conventions")
    id("multiplex.di-conventions")
    id("multiplex.json-conventions")
    id("multiplex.rest-conventions")
    id("multiplex.testing-conventions")
    id("multiplex.tooling-conventions")
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-infra"))
    implementation(project(":multiplex-commons:multiplex-common-utils"))
    implementation(project(":multiplex-rooms:multiplex-rooms-api"))
    implementation(project(":multiplex-shared-kernel"))

    testImplementation(project(":multiplex-commons:multiplex-common-test"))
}
