dependencies {
    implementation(project(":module-common"))

    implementation(project(":module-order-contract"))
    implementation(project(":module-product-contract"))
    implementation(project(":module-user-contract"))
    implementation(project(":module-auth-contract"))
    implementation(project(":module-payment-contract"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.security.core)

    testImplementation("io.mockk:mockk:1.13.10")

    runtimeOnly(libs.postgres)
}