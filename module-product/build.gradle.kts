dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-order-contract"))
    implementation(project(":module-product-contract"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)

    runtimeOnly(libs.postgres)
}
