plugins {
    id("multiplex.common-conventions")
}

object JsonVersions {
    const val JACKSON = "2.18.2"
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:${JsonVersions.JACKSON}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${JsonVersions.JACKSON}")
}
