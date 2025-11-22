import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("io.spring.dependency-management") apply false
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.kotlin.plugin.spring") apply false
    id("org.jetbrains.kotlin.plugin.jpa") apply false

    id("org.asciidoctor.jvm.convert") version "3.3.2" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("jacoco")
    id("base")
}

group = "com.playground"
version = "0.0.1-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "jacoco")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
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

configure<JacocoPluginExtension> {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "Verification"
    description = "Generates an aggregate Jacoco report from all subprojects"

    dependsOn(subprojects.map { it.tasks.named("test") })

    val allSourceDirs = subprojects.map { it.layout.projectDirectory.dir("src/main/kotlin") }
    val allClassDirs = subprojects.map { it.layout.buildDirectory.dir("classes/kotlin/main") }
    val allExecData = subprojects.map { it.layout.buildDirectory.file("jacoco/test.exec") }

    sourceDirectories.setFrom(files(allSourceDirs))
    executionData.setFrom(files(allExecData))

    classDirectories.setFrom(
        files(allClassDirs).asFileTree.matching {
            exclude(
                "**/com/playground/PlayGroundApplication*",
                "**/com/playground/common/**",
                "**/com/playground/auth/config/**",
                "**/com/playground/auth/jwt/JwtProperties*",
                "**/com/playground/support/**",
                "**/com/playground/*/presentation/request/*",
                "**/com/playground/*/presentation/response/*",
                "**/com/playground/*/consumer/*",
                "**/com/playground/*/domain/exception/*",
            )
        },
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.named("check") {
    dependsOn("jacocoTestReport")
}
