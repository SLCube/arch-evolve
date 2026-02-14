plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.restdocs.mockmvc)
    implementation(libs.restdocs.api.spec.mockmvc)
    implementation(libs.kotest.runner)
    implementation(libs.kotest.spring)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.test)
    implementation(libs.spring.security.test)

    // Redis Mock (실제 Redis 서버 불필요)
    implementation(libs.jedis.mock)

    implementation(project(":module-common"))
    implementation(project(":module-auth-contract"))
}
