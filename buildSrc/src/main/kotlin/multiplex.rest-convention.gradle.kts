plugins {
    id("multiplex.common-convention")
    id("multiplex.logging-convention")
    id("multiplex.json-convention")
}

object RestVersions {
    const val KTOR = "3.0.2"
}

dependencies {
    implementation("io.ktor:ktor-server-auth:${RestVersions.KTOR}")
    implementation("io.ktor:ktor-server-auth-jwt:${RestVersions.KTOR}")
    implementation("io.ktor:ktor-server-content-negotiation:${RestVersions.KTOR}")
    implementation("io.ktor:ktor-server-core:${RestVersions.KTOR}")
    implementation("io.ktor:ktor-server-netty:${RestVersions.KTOR}")
    implementation("io.ktor:ktor-server-resources:${RestVersions.KTOR}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${RestVersions.KTOR}")
    implementation("io.ktor:ktor-server-sse:${RestVersions.KTOR}")

    testImplementation("io.ktor:ktor-server-test-host:${RestVersions.KTOR}")
}
