plugins {
    id("multiplex.common-conventions")
}

object InfraVersions {
    const val EXPOSED = "0.53.0"
    const val TESTCONTAINERS = "1.20.2"
    const val JACKSON = "2.18.2"
    const val KOIN = "4.0.0"
    const val KOTEST_KOIN = "1.3.0"
    const val KOTEST_TESTCONTAINERS = "2.0.2"
    const val POSTGRES = "42.7.4"
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${InfraVersions.JACKSON}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${InfraVersions.JACKSON}")
    implementation("io.insert-koin:koin-core-jvm:${InfraVersions.KOIN}")
    implementation("org.jetbrains.exposed:exposed-java-time:${InfraVersions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${InfraVersions.EXPOSED}")
    implementation("org.postgresql:postgresql:${InfraVersions.POSTGRES}")

    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:${InfraVersions.KOTEST_TESTCONTAINERS}")
    testImplementation("io.insert-koin:koin-test:${InfraVersions.KOIN}")
    testImplementation("io.kotest.extensions:kotest-extensions-koin:${InfraVersions.KOTEST_KOIN}")
    testImplementation("org.testcontainers:postgresql:${InfraVersions.TESTCONTAINERS}")
    testImplementation("org.testcontainers:testcontainers:${InfraVersions.TESTCONTAINERS}")
}
