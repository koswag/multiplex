plugins {
    id("multiplex.common-conventions")
    id("multiplex.logging-conventions")
    id("multiplex.json-conventions")
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

    testImplementation("io.ktor:ktor-server-test-host:${RestVersions.KTOR}")
}
