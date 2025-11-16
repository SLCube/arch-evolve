pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val kotlinVersion: String by settings
    val asciidoctorVersion: String by settings
    val ktlintPluginVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.springframework.boot" -> useVersion(springBootVersion)
                "io.spring.dependency-management" -> useVersion(springDependencyManagementVersion)
                "org.jetbrains.kotlin.jvm" -> useVersion(kotlinVersion)
                "org.jetbrains.kotlin.plugin.spring" -> useVersion(kotlinVersion)
                "org.jetbrains.kotlin.plugin.jpa" -> useVersion(kotlinVersion)
                "org.asciidoctor.jvm.convert" -> useVersion(asciidoctorVersion)
                "org.jlleitschuh.gradle.ktlint" -> useVersion(ktlintPluginVersion)
            }
        }
    }
}

rootProject.name = "play-ground"

include("module-common")
include("module-product")
