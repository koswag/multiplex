plugins {
    id("multiplex.infra-conventions")
}

dependencies {
    implementation(project(":multiplex-rooms:multiplex-rooms-api"))
    implementation(project(":multiplex-commons:multiplex-common-infra"))
    implementation(project(":multiplex-commons:multiplex-common-utils"))
    implementation(project(":multiplex-screenings:multiplex-screenings-domain"))
    implementation(project(":multiplex-shared-kernel"))
}
