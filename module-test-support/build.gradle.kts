plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

val kotestVersion: String by rootProject
val kotestSpringExtensionVersion: String by rootProject
val archunitVersion: String by rootProject

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")


    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    implementation("org.springframework.security:spring-security-test")

    implementation("io.kotest:kotest-runner-junit5-jvm:${kotestVersion}")
    implementation("io.kotest:kotest-assertions-core-jvm:${kotestVersion}")
    implementation("io.kotest.extensions:kotest-extensions-spring:${kotestSpringExtensionVersion}")

    implementation("com.tngtech.archunit:archunit-junit5:${archunitVersion}")

    implementation("com.h2database:h2")
}