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
    implementation("io.arrow-kt:arrow-core:${Versions.ARROW}")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}