dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-delivery-contract"))
    implementation(project(":module-user-contract"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.security.core)

    runtimeOnly(libs.postgres)
}