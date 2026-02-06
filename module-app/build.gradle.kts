plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")

    id("org.asciidoctor.jvm.convert")
    alias(libs.plugins.restdocs.api.spec)
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-product"))
    implementation(project(":module-user"))
    implementation(project(":module-auth"))
    implementation(project(":module-order"))
    implementation(project(":module-delivery"))
    implementation(project(":module-payment"))
    implementation(project(":module-monitoring"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.aop)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.loki.logback.appender)

    // Swagger UI (개발 환경 전용)
    developmentOnly(libs.springdoc.openapi.starter.webmvc.ui)
    developmentOnly(libs.spring.boot.docker.compose)

    runtimeOnly(libs.postgres)

    testImplementation(project(":module-test-support"))
    testImplementation(libs.restdocs.mockmvc)
    testImplementation(libs.restdocs.api.spec.mockmvc)
    testImplementation(libs.spring.security.test)

    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.spring)

    testRuntimeOnly(libs.h2.database)
}

val copySnippets = tasks.register<Copy>("copySnippets") {
    group = "documentation"
    description = "Copy REST Docs snippets from all modules to module-app"

    val modulesWithTests = listOf(
        "module-user",
        "module-product",
        "module-order",
        "module-payment",
        "module-auth"
    )

    modulesWithTests.forEach { moduleName ->
        from("${rootProject.projectDir}/${moduleName}/build/generated-snippets") {
            include("**/*")
        }
    }

    into(layout.buildDirectory.dir("generated-snippets"))

    dependsOn(
        ":module-user:test",
        ":module-product:test",
        ":module-order:test",
        ":module-payment:test",
        ":module-auth:test"
    )
}

tasks.asciidoctor {
    val snippetsDir = layout.buildDirectory.dir("generated-snippets")

    sourceDir(file("src/docs/asciidoc"))
    inputs.dir(snippetsDir)
    dependsOn(copySnippets)

    attributes(
        mapOf("snippets" to snippetsDir.get().asFile)
    )
}

openapi3 {
    setServer("http://localhost:8080")
    title = "PlayGround API Documentation"
    description = "PlayGround 프로젝트의 REST API 문서"
    version = "v2.5"
    format = "yaml"
}

tasks.withType<com.epages.restdocs.apispec.gradle.OpenApi3Task> {
    dependsOn(copySnippets)
}

tasks.register("resolveAndCopyApi") {
    group = "documentation"
    description = "Resolve and copy OpenAPI spec to static resources"

    dependsOn("openapi3")

    doLast {
        val sourceFile = file("build/api-spec/openapi3.yaml")
        val targetDir = file("src/main/resources/static/docs")
        val targetFile = file("$targetDir/openapi3.yaml")

        targetDir.mkdirs()

        if (sourceFile.exists()) {
            copy {
                from(sourceFile)
                into(targetDir)
                rename { "openapi3.yaml" }
            }

            val content = targetFile.readText()
            val updatedContent = content.replace(
                "tags: []\npaths:",
                """tags: []
security:
- bearerAuth: []
paths:"""
            )

            val finalContent = updatedContent.replace(
                "components:\n  schemas:",
                """components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT Access Token
  schemas:"""
            )

            val withPublicApis = finalContent
                .replace(
                    "  /users/login:\n    post:",
                    "  /users/login:\n    post:\n      security: []"
                )
                .replace(
                    "  /users/sign-up:\n    post:",
                    "  /users/sign-up:\n    post:\n      security: []"
                )
                .replace(
                    "  /auth/refresh:\n    post:",
                    "  /auth/refresh:\n    post:\n      security: []"
                )

            targetFile.writeText(withPublicApis)
        }
    }
}

tasks.register("buildDocs") {
    group = "documentation"
    description = "Builds the API documentation."
    dependsOn(tasks.asciidoctor)
}

tasks.bootJar {
    enabled = true
    dependsOn("resolveAndCopyApi")

    from(tasks.named("asciidoctor")) {
        into("static/docs")
    }

    from("build/api-spec") {
        into("static/docs")
        include("openapi3.yaml")
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
