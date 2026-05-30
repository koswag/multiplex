plugins {
    id("multiplex.common-convention")
}

object DbAccessVersions {
    const val EXPOSED = "1.3.0"
    const val KOTEST_TESTCONTAINERS = "2.0.2"
    const val POSTGRES = "42.7.4"
    const val TESTCONTAINERS = "1.20.2"
}

dependencies {
    implementation("org.jetbrains.exposed:exposed-java-time:${DbAccessVersions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-r2dbc:${DbAccessVersions.EXPOSED}")
    implementation("org.postgresql:postgresql:${DbAccessVersions.POSTGRES}")

    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:${DbAccessVersions.KOTEST_TESTCONTAINERS}")
    testImplementation("org.testcontainers:postgresql:${DbAccessVersions.TESTCONTAINERS}")
    testImplementation("org.testcontainers:testcontainers:${DbAccessVersions.TESTCONTAINERS}")
}
