import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false

    alias(libs.plugins.asciidoctor.convert) apply false
    alias(libs.plugins.ktlint)

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

    val libs = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")

    plugins.withId("java") {
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }

        dependencies {
            add("implementation", platform(libs.findLibrary("spring-boot-dependencies").get()))
            add("implementation", libs.findLibrary("jackson-module-kotlin").get())
            add("implementation", libs.findLibrary("kotlin-reflect").get())
            add("testImplementation", libs.findLibrary("spring-boot-starter-test").get())
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        tasks.withType<KotlinCompile> {
            compilerOptions {
                freeCompilerArgs.add("-Xjsr305=strict")
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }
    }

    plugins.withId("java") {
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
                "**/com/playground/*/infra/event/adapter/*",
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
