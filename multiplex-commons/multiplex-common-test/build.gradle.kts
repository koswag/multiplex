plugins {
    kotlin("jvm")
}

group = "pl.kskarzynski"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

object Versions {
    const val EXPOSED = "0.53.0"
    const val KOTEST = "5.9.1"
    const val TESTCONTAINERS = "1.20.2"
    const val KOTEST_TESTCONTAINERS = "2.0.2"
}

dependencies {
    implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")

    implementation("io.kotest.extensions:kotest-extensions-testcontainers:${Versions.KOTEST_TESTCONTAINERS}")
    implementation("io.kotest:kotest-runner-junit5:${Versions.KOTEST}")
    implementation("org.testcontainers:postgresql:${Versions.TESTCONTAINERS}")
    implementation("org.testcontainers:testcontainers:${Versions.TESTCONTAINERS}")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}