plugins {
    id("multiplex.db-access-convention")
    id("multiplex.di-convention")
    id("multiplex.json-convention")
    id("multiplex.rest-convention")
    id("multiplex.testing-convention")
}

object Versions {
    const val KTOR = "3.0.2"
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-test"))
    implementation(project(":multiplex-commons:multiplex-common-infra"))
    implementation(project(":multiplex-commons:multiplex-common-utils"))
    implementation(project(":multiplex-movies:multiplex-movies-service"))
    implementation(project(":multiplex-movies:multiplex-movies-service"))
    implementation(project(":multiplex-rooms:multiplex-rooms-service"))
    implementation(project(":multiplex-screenings:multiplex-screenings-domain"))
    implementation(project(":multiplex-screenings:multiplex-screenings-infrastructure"))
    implementation(project(":multiplex-shared-kernel"))

    testImplementation("io.ktor:ktor-client-content-negotiation:${Versions.KTOR}")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
