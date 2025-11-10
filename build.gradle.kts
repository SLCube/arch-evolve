plugins {
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
    id("org.asciidoctor.jvm.convert") version "4.0.2"
}

val kotestVersion = "5.8.0"
val kotestSpringExtensionVersion = "1.1.3"
val jjwtVersion = "0.12.5"
val queryDslVersion = "7.1"

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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // QueryDSL
    implementation("io.github.openfeign.querydsl:querydsl-jpa:$queryDslVersion")
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:$queryDslVersion")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

    // KoTest
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:$kotestSpringExtensionVersion")
    testImplementation("org.springframework.security:spring-security-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets {
    main {
        kotlin.srcDirs("$buildDir/generated/ksp/main/kotlin")
    }
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
        mapOf("snippets" to snippetsDir)
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