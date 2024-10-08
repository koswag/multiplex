import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    const val KOIN = "4.0.0"
    const val KOTEST = "5.9.1"
    const val KOTEST_KOIN = "1.3.0"
    const val POSTGRES = "42.7.4"
    const val STRIKT = "0.34.0"
    const val TESTCONTAINERS = "1.20.2"
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-test"))
    implementation(project(":multiplex-movies:multiplex-movies-api"))
    implementation(project(":multiplex-movies:multiplex-movies-domain"))
    implementation(project(":multiplex-shared-kernel"))

    implementation("org.jetbrains.exposed:exposed-jdbc:${Versions.EXPOSED}")
    implementation("org.postgresql:postgresql:${Versions.POSTGRES}")

    implementation("io.insert-koin:koin-core-jvm:${Versions.KOIN}")

    testImplementation("io.insert-koin:koin-test:${Versions.KOIN}")
    testImplementation("io.kotest.extensions:kotest-extensions-koin:${Versions.KOTEST_KOIN}")
    testImplementation("io.kotest:kotest-runner-junit5:${Versions.KOTEST}")
    testImplementation("io.kotest:kotest-property:${Versions.KOTEST}")
    testImplementation("io.strikt:strikt-core:${Versions.STRIKT}")
    testImplementation("org.testcontainers:postgresql:${Versions.TESTCONTAINERS}")
    testImplementation("org.testcontainers:testcontainers:${Versions.TESTCONTAINERS}")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xcontext-receivers",
        )
    }
}
