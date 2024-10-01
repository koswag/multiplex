plugins {
    kotlin("jvm")
}

group = "pl.kskarzynski"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

object Versions {
    const val ARROW = "1.2.4"
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("io.arrow-kt:arrow-core:${Versions.ARROW}")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}