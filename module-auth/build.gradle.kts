plugins {
    id("spring-web-conventions")
    id("spring-data-conventions")
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-user"))

    implementation(libs.spring.boot.starter.security)
    implementation(libs.bundles.jjwt)
}