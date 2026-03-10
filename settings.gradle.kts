pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "play-ground"

includeBuild("monolith")
includeBuild("payment-service")
includeBuild("delivery-service")
includeBuild("api-gateway")
includeBuild("test-support")

