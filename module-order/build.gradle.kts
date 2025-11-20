plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-user"))
    implementation(project(":module-product"))
    implementation(project(":module-auth"))
}