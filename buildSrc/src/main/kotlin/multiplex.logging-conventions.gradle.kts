plugins {
    id("multiplex.common-conventions")
}

object LoggingVersions {
    const val LOGBACK = "1.5.12"
}

dependencies {
    testImplementation("ch.qos.logback:logback-classic:${LoggingVersions.LOGBACK}")
}
