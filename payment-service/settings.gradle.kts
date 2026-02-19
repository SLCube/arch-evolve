pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "payment-service"

includeBuild("../test-support") {
    dependencySubstitution {
        substitute(module("com.playground:test-support")).using(project(":"))
    }
}
