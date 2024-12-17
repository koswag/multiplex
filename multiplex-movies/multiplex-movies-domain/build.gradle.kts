plugins {
    id("multiplex.common-conventions")
}

dependencies {
    implementation(project(":multiplex-movies:multiplex-movies-api"))
    implementation(project(":multiplex-shared-kernel"))
}
