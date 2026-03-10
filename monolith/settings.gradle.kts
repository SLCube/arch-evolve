pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "monolith"

include("module-common")

include("module-product")
include("module-product-contract")

include("module-user")
include("module-user-contract")

include("module-auth")
include("module-auth-contract")

include("module-order")
include("module-order-contract")

include("module-delivery-contract")

include("module-app")

include("module-test-support")

include("module-monitoring")
