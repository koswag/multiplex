plugins {
    id("multiplex.db-access-conventions")
    id("multiplex.di-conventions")
    id("multiplex.rest-conventions")
    id("multiplex.testing-conventions")
}

dependencies {
    implementation(project(":multiplex-commons:multiplex-common-test"))
    implementation(project(":multiplex-commons:multiplex-common-infra"))
    implementation(project(":multiplex-movies:multiplex-movies-api"))
    implementation(project(":multiplex-movies:multiplex-movies-domain"))
    implementation(project(":multiplex-shared-kernel"))
}
