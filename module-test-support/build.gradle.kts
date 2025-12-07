dependencies {
    implementation(libs.restdocs.mockmvc)
    implementation(libs.kotest.runner)
    implementation(libs.kotest.spring)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.test)
    implementation(libs.spring.security.test)

    implementation(project(":module-common"))
    implementation(project(":module-auth-contract"))
}