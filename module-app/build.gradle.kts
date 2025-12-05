plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")

    id("org.asciidoctor.jvm.convert")
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-product"))
    implementation(project(":module-user"))
    implementation(project(":module-auth"))
    implementation(project(":module-order"))
    implementation(project(":module-payment"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.aop)

    developmentOnly(libs.spring.boot.docker.compose)

    runtimeOnly(libs.postgres)

    testImplementation(libs.restdocs.mockmvc)
    testImplementation(libs.spring.security.test)

    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.spring)

    testRuntimeOnly(libs.h2.database)
}

//tasks.asciidoctor {
//    val snippetsDir = layout.buildDirectory.dir("generated-snippets")
//
//    sourceDir(file("src/docs/asciidoc"))
//    inputs.dir(snippetsDir)
//    dependsOn(tasks.named("test"))
//
//    attributes(
//        mapOf("snippets" to snippetsDir.get().asFile)
//    )
//}

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