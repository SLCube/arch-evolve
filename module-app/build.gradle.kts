plugins {
    id("org.springframework.boot")
}

val kotestVersion: String by rootProject
val kotestSpringExtensionVersion: String by rootProject
val archunitVersion: String by rootProject

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-product"))
    implementation(project(":module-user"))
    implementation(project(":module-auth"))
    implementation(project(":module-order"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation(project(":module-test-support"))
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:${kotestVersion}")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${kotestVersion}")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:${kotestSpringExtensionVersion}")
    testRuntimeOnly("com.h2database:h2")
}

tasks.bootJar {
    enabled = true
    from(rootProject.tasks.named("asciidoctor")) {
        into("static/docs")
    }
}