plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":module-common"))

    implementation(libs.spring.security.core)
}
