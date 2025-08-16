plugins {
    id("multiplex.common-convention")
}

object DiVersions {
    const val KOIN = "4.0.0"
    const val KOTEST_KOIN = "1.3.0"
}

dependencies {
    implementation("io.insert-koin:koin-core-jvm:${DiVersions.KOIN}")
    implementation("io.insert-koin:koin-ktor:${DiVersions.KOIN}")

    testImplementation("io.insert-koin:koin-test:${DiVersions.KOIN}")
    testImplementation("io.kotest.extensions:kotest-extensions-koin:${DiVersions.KOTEST_KOIN}")
}
