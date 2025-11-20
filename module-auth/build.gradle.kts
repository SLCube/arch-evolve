plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("io.spring.dependency-management")
}

val jjwtVersion: String by rootProject

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-user"))
}