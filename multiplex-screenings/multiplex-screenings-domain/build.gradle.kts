import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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
    const val ARROW = "1.2.4"
    const val KOTEST = "5.9.1"
    const val STRIKT = "0.34.0"
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-utils"))
    implementation(project(":multiplex-shared-kernel"))

    implementation("io.arrow-kt:arrow-core:${Versions.ARROW}")

    testImplementation(project(":multiplex-commons:multiplex-common-test"))
    testImplementation("io.kotest:kotest-runner-junit5:${Versions.KOTEST}")
    testImplementation("io.kotest:kotest-property:${Versions.KOTEST}")
    testImplementation("io.strikt:strikt-core:${Versions.STRIKT}")
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

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}