plugins {
    id("multiplex.common-convention")
}

object LoggingVersions {
    const val LOGBACK = "1.5.12"
    const val KOTLIN_LOGGING = "2.0.11"
}

dependencies {
    implementation("ch.qos.logback:logback-classic:${LoggingVersions.LOGBACK}")
    implementation("io.github.microutils:kotlin-logging-jvm:${LoggingVersions.KOTLIN_LOGGING}")
}
