import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false

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
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "jacoco")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.8")
        }
    }

    val libs = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        "implementation"(libs.findLibrary("spring-boot-starter").get())
        "implementation"(libs.findLibrary("jackson-module-kotlin").get())
        "implementation"(libs.findLibrary("kotlin-reflect").get())
        "testImplementation"(libs.findLibrary("spring-boot-starter-test").get())
    }

    // 5. Kotlin 컴파일 옵션
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        systemProperty(
            "org.springframework.restdocs.outputDir",
            layout.buildDirectory.dir("generated-snippets").get().asFile.path,
        )
    }

    plugins.withId("org.springframework.boot") {
        tasks.named("bootJar") {
            enabled = false
        }
        tasks.named("jar") {
            enabled = true
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    tasks.withType<ProcessResources> {
        filteringCharset = "UTF-8"
    }
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
