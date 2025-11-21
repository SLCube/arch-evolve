pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "play-ground"

include("module-common")
include("module-product")
include("module-user")
include("module-auth")
include("module-order")
include("module-app")
