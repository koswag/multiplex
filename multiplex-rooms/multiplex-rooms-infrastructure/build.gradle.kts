
plugins {
    id("multiplex.infra-conventions")
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-utils"))
    implementation(project(":multiplex-rooms:multiplex-rooms-api"))
    implementation(project(":multiplex-rooms:multiplex-rooms-domain"))
    implementation(project(":multiplex-shared-kernel"))

    testImplementation(project(":multiplex-commons:multiplex-common-test"))
}
