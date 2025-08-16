plugins {
    id("multiplex.tooling-convention")
    id("multiplex.db-access-convention")
}

object Versions {
    const val KOTEST = "5.9.1"
    const val STRIKT = "0.34.0"
    const val TESTCONTAINERS = "1.20.2"
    const val KOTEST_TESTCONTAINERS = "2.0.2"
}

dependencies {
    implementation(project(":multiplex-shared-kernel"))
    implementation(project(":multiplex-commons:multiplex-common-utils"))

    implementation("io.kotest.extensions:kotest-extensions-testcontainers:${Versions.KOTEST_TESTCONTAINERS}")
    implementation("io.kotest:kotest-property:${Versions.KOTEST}")
    implementation("io.kotest:kotest-runner-junit5:${Versions.KOTEST}")
    implementation("io.strikt:strikt-core:${Versions.STRIKT}")
    implementation("org.testcontainers:postgresql:${Versions.TESTCONTAINERS}")
    implementation("org.testcontainers:testcontainers:${Versions.TESTCONTAINERS}")
}
