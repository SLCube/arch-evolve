pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "play-ground"

includeBuild("monolith")
includeBuild("payment-service")
includeBuild("api-gateway")

