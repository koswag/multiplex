plugins {
    id("multiplex.common-conventions")
    id("multiplex.logging-conventions")
}

object RestVersions {
    const val KTOR = "3.0.2"
}

dependencies {
    implementation("io.ktor:ktor-server-core:${RestVersions.KTOR}")
    implementation("io.ktor:ktor-server-netty:${RestVersions.KTOR}")
}
