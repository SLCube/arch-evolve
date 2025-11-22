plugins {
    id("spring-web-conventions")
    id("spring-data-conventions")
    alias(libs.plugins.asciidoctor.convert)
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-product"))
    implementation(project(":module-user"))
    implementation(project(":module-auth"))
    implementation(project(":module-order"))

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation(libs.spring.security.test)

    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.spring)

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