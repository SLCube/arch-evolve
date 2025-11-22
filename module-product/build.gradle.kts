dependencies {
    implementation(project(":module-common"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)

    runtimeOnly(libs.postgres)
}
