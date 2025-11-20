val springBootVersion: String by project
val springDependencyManagementVersion: String by project
val kotlinVersion: String by project
val asciidoctorVersion: String by project
val ktlintPluginVersion: String by project
val ktlintEngineVersion: String by project
val kotestVersion: String by project
val kotestSpringExtensionVersion: String by project
val jjwtVersion: String by project
val kotlinJdslVersion: String by project
val archunitVersion: String by project

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.asciidoctor.jvm.convert")
    id("org.jlleitschuh.gradle.ktlint")
    id("jacoco")
}

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

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring")

    repositories {
        mavenCentral()
    }

    plugins.withId("org.springframework.boot") {
        tasks.getByName("bootJar") {
            enabled = false
        }

        tasks.getByName("jar") {
            enabled = true
        }
    }

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    dependencies {
        testImplementation("org.springframework.boot:spring-boot-starter-test")
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
    systemProperty(
        "org.springframework.restdocs.outputDir",
        layout.buildDirectory
            .dir("generated-snippets")
            .get()
            .asFile.path,
    )
}

tasks.bootJar {
    enabled = false
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    filteringCharset = "UTF-8"
}

ktlint {
    version.set(ktlintEngineVersion)
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

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(subprojects.map { it.tasks.named("test") })

    val allClassDirs = files(subprojects.map { it.layout.buildDirectory.dir("classes/kotlin/main") })
    val allSourceDirs = files(subprojects.map { it.file("src/main/kotlin") })
    val allExecData = files(subprojects.map { it.layout.buildDirectory.file("jacoco/test.exec") })

    sourceDirectories.setFrom(allSourceDirs)
    executionData.setFrom(allExecData)

    classDirectories.setFrom(
        fileTree(allClassDirs) {
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

tasks.check {
}

tasks.build {
    dependsOn(tasks.jacocoTestReport)
}
