import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.20"
}

group = "pl.kskarzynski"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

object Versions {
    const val ARROW = "1.2.4"
    const val COROUTINES = "1.8.1"
    const val EXPOSED = "0.53.0"
    const val KOIN = "4.0.0"
    const val KOTEST = "5.9.1"
    const val KOTEST_EXTRA_ARBS = "2.1.2"
}

dependencies {
    implementation(project("multiplex-movies:multiplex-movies-infrastructure"))
    implementation(project("multiplex-rooms:multiplex-rooms-infrastructure"))
    implementation(project("multiplex-screenings:multiplex-screenings-infrastructure"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.COROUTINES}")

    implementation("io.arrow-kt:arrow-core:${Versions.ARROW}")

    implementation("io.insert-koin:koin-core-jvm:${Versions.KOIN}")

    testImplementation("io.kotest:kotest-runner-junit5:${Versions.KOTEST}")
    testImplementation("io.kotest:kotest-property:${Versions.KOTEST}")
    testImplementation("io.kotest.extensions:kotest-property-arbs:${Versions.KOTEST_EXTRA_ARBS}")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xcontext-receivers",
        )
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
