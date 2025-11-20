plugins {
    id("org.springframework.boot")
    id("org.asciidoctor.jvm.convert")
    kotlin("jvm")
    kotlin("plugin.spring")
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

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:${kotestVersion}")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${kotestVersion}")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:${kotestSpringExtensionVersion}")
    testRuntimeOnly("com.h2database:h2")
}

tasks.asciidoctor {
    val snippetsDir = layout.buildDirectory.dir("generated-snippets")

    sourceDir(file("src/docs/asciidoc"))
    inputs.dir(snippetsDir)
    dependsOn(tasks.named("test"))

    attributes(
        mapOf("snippets" to snippetsDir.get().asFile)
    )
}

tasks.register("buildDocs") {
    group = "documentation"
    description = "Builds the API documentation."
    dependsOn(tasks.asciidoctor)
}

tasks.bootJar {
    enabled = true
    from(tasks.named("asciidoctor")) {
        into("static/docs")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty(
        "org.springframework.restdocs.outputDir",
        layout.buildDirectory
            .dir("generated-snippets")
            .get()
            .asFile.path,
    )
}