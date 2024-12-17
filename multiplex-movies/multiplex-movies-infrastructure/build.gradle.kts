plugins {
    id("multiplex.infra-conventions")
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-test"))
    implementation(project(":multiplex-movies:multiplex-movies-api"))
    implementation(project(":multiplex-movies:multiplex-movies-domain"))
    implementation(project(":multiplex-shared-kernel"))
}
