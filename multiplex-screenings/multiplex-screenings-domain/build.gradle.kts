plugins {
    id("multiplex.common-conventions")
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-utils"))
    implementation(project(":multiplex-shared-kernel"))

    testImplementation(project(":multiplex-commons:multiplex-common-test"))
}
