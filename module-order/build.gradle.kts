dependencies {
    implementation(project(":module-common"))

    implementation(project(":module-order-contract"))
    implementation(project(":module-delivery-contract"))
    implementation(project(":module-product-contract"))
    implementation(project(":module-user-contract"))
    implementation(project(":module-auth-contract"))
    implementation(project(":module-payment-contract"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.security.core)

    runtimeOnly(libs.postgres)

    testImplementation(libs.restdocs.mockmvc)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.spring)

    testImplementation(project(":module-test-support"))
    testRuntimeOnly(libs.h2.database)
}