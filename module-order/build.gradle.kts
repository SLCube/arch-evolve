dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-user"))
    implementation(project(":module-product"))
    implementation(project(":module-auth"))

    implementation(project(":module-order-contract"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)

    runtimeOnly(libs.postgres)
}