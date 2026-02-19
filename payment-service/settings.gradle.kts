pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "payment-service"

includeBuild("../test-support") {
    dependencySubstitution {
        substitute(module("com.playground:test-support")).using(project(":"))
    }
}
