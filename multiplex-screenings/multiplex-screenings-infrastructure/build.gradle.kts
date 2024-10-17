plugins {
    kotlin("jvm") version "2.0.20"
}

group = "pl.kskarzynski"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

object Versions {
    const val ARROW = "1.2.4"
    const val COROUTINES = "1.8.1"
    const val EXPOSED = "0.53.0"
    const val KOIN = "4.0.0"
    const val KOTEST = "5.9.1"
    const val KOTEST_KOIN = "1.3.0"
    const val KOTEST_EXTRA_ARBS = "2.1.2"
}

dependencies {
    implementation(project(":multiplex-rooms:multiplex-rooms-api"))
    implementation(project(":multiplex-commons:multiplex-common-infra"))
    implementation(project(":multiplex-commons:multiplex-common-utils"))
    implementation(project(":multiplex-screenings:multiplex-screenings-domain"))
    implementation(project(":multiplex-shared-kernel"))

    implementation("io.arrow-kt:arrow-core:${Versions.ARROW}")
    implementation("io.insert-koin:koin-core-jvm:${Versions.KOIN}")

    implementation("org.jetbrains.exposed:exposed-core:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-java-time:${Versions.EXPOSED}")

    testImplementation("io.insert-koin:koin-test:${Versions.KOIN}")
    testImplementation("io.kotest.extensions:kotest-extensions-koin:${Versions.KOTEST_KOIN}")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
