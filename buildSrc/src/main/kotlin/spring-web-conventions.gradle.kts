import gradle.kotlin.dsl.accessors._18c6aa86d5cf3c0ead48d227d07700ca.dependencyManagement
import gradle.kotlin.dsl.accessors._18c6aa86d5cf3c0ead48d227d07700ca.implementation
import gradle.kotlin.dsl.accessors._18c6aa86d5cf3c0ead48d227d07700ca.testImplementation

plugins {
    id("java-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.plugin.spring")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.1")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.named("bootJar") {
    enabled = false
}

tasks.named("jar") {
    enabled = true
}