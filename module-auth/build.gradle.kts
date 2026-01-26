plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-auth-contract"))
    implementation(project(":module-user-contract"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.bundles.jjwt)

    runtimeOnly(libs.postgres)

    testImplementation(libs.restdocs.mockmvc)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(project(":module-test-support"))
    testRuntimeOnly(libs.h2.database)
}
