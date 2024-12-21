plugins {
    id("multiplex.common-conventions")
}

object JsonVersions {
    const val ARROW = "1.2.4"
    const val JACKSON = "2.18.2"
    const val KOTLINX_SERIALIZATION = "1.7.3"
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${JsonVersions.JACKSON}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${JsonVersions.JACKSON}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${JsonVersions.KOTLINX_SERIALIZATION}")
    implementation("io.arrow-kt:arrow-core-serialization:${JsonVersions.ARROW}")
}
