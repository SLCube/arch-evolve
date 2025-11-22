plugins {
    id("spring-web-conventions")
    id("spring-data-conventions")
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-user"))
    implementation(project(":module-product"))
    implementation(project(":module-auth"))

    implementation(libs.spring.boot.starter.security)
}