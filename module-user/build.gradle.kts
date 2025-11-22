plugins {
    id("spring-web-conventions")
    id("spring-data-conventions")
}

dependencies {
    implementation(project(":module-common"))

    implementation(libs.spring.boot.starter.security)
}