plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("java-library")
    id("io.spring.dependency-management")
}

val jjwtVersion: String by rootProject
val kotlinJdslVersion: String by rootProject

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-validation")

    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.jetbrains.kotlin:kotlin-reflect")

    api("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    api("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    api("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    api("com.linecorp.kotlin-jdsl:jpql-dsl:$kotlinJdslVersion")
    api("com.linecorp.kotlin-jdsl:jpql-render:$kotlinJdslVersion")
    api("com.linecorp.kotlin-jdsl:spring-data-jpa-support:$kotlinJdslVersion")

    api("org.springframework.boot:spring-boot-starter-aop")
    api("org.postgresql:postgresql")
}
