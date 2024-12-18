plugins {
    id("multiplex.common-conventions")
}

object TestingVersions {
    const val KOTEST = "5.9.1"
    const val KOTEST_EXTRA_ARBS = "2.1.2"
    const val STRIKT = "0.34.0"
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:${TestingVersions.KOTEST}")
    testImplementation("io.kotest:kotest-property:${TestingVersions.KOTEST}")
    testImplementation("io.kotest.extensions:kotest-property-arbs:${TestingVersions.KOTEST_EXTRA_ARBS}")
    testImplementation("io.strikt:strikt-core:${TestingVersions.STRIKT}")
}

tasks.test {
    useJUnitPlatform()
}
