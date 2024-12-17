import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "pl.kskarzynski"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

object CommonVersions {
    const val ARROW = "1.2.4"
    const val COROUTINES = "1.8.1"
    const val KOTEST = "5.9.1"
    const val KOTEST_EXTRA_ARBS = "2.1.2"
    const val STRIKT = "0.34.0"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${CommonVersions.COROUTINES}")
    implementation("io.arrow-kt:arrow-core:${CommonVersions.ARROW}")

    testImplementation("io.kotest:kotest-runner-junit5:${CommonVersions.KOTEST}")
    testImplementation("io.kotest:kotest-property:${CommonVersions.KOTEST}")
    testImplementation("io.kotest.extensions:kotest-property-arbs:${CommonVersions.KOTEST_EXTRA_ARBS}")
    testImplementation("io.strikt:strikt-core:${CommonVersions.STRIKT}")
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
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
