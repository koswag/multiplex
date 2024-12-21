plugins {
    id("multiplex.db-access-conventions")
    id("multiplex.di-conventions")
    id("multiplex.json-conventions")
    id("multiplex.rest-conventions")
    id("multiplex.testing-conventions")
}

object Versions {
    const val KTOR = "3.0.2"
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-test"))
    implementation(project(":multiplex-commons:multiplex-common-infra"))
    implementation(project(":multiplex-movies:multiplex-movies-service"))
    implementation(project(":multiplex-movies:multiplex-movies-service"))
    implementation(project(":multiplex-rooms:multiplex-rooms-service"))
    implementation(project(":multiplex-screenings:multiplex-screenings-infrastructure"))
    implementation(project(":multiplex-shared-kernel"))

    testImplementation("io.ktor:ktor-client-content-negotiation:${Versions.KTOR}")
}
