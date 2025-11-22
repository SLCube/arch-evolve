dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-user"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.bundles.jjwt)

    runtimeOnly(libs.postgres)
}