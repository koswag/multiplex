plugins {
    id("multiplex.common-convention")
}

object ToolingVersions {
    const val ARROW = "1.2.4"
    const val COROUTINES = "1.8.1"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${ToolingVersions.COROUTINES}")
    implementation("io.arrow-kt:arrow-core:${ToolingVersions.ARROW}")
}
