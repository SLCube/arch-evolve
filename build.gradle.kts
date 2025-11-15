plugins {
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    id("org.asciidoctor.jvm.convert") version "4.0.2"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

val kotestVersion = "5.8.0"
val kotestSpringExtensionVersion = "1.1.3"
val jjwtVersion = "0.12.5"
val springDataKotlinJdslVersion = "3.5.5"

group = "com.playground"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Kotlin & Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Kotlin JDSL
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:$springDataKotlinJdslVersion")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:$springDataKotlinJdslVersion")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:$springDataKotlinJdslVersion")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // Docker Compose Support (for local development)
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // Test Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:$kotestSpringExtensionVersion")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val snippetsDir = file("build/generated-snippets")

tasks.asciidoctor {
    sourceDir(file("src/docs/asciidoc"))
    inputs.dir(snippetsDir)
    dependsOn(tasks.test)
    attributes(
        mapOf("snippets" to snippetsDir),
    )
}

tasks.register("buildDocs") {
    group = "documentation"
    description = "Builds the API documentation."
    dependsOn(tasks.asciidoctor)
}

tasks.bootJar {
    from(tasks.asciidoctor.get().outputDir) {
        into("static/docs")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    filteringCharset = "UTF-8"
}

ktlint {
    version.set("1.3.1")
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}
