plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-order-contract"))
    implementation(project(":module-product-contract"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.openfeign.querydsl.jpa)

    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.redisson.spring.boot.starter)

    ksp(libs.openfeign.querydsl.ksp)

    runtimeOnly(libs.postgres)

    testImplementation(libs.restdocs.mockmvc)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.spring)
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)

    testImplementation(project(":module-test-support"))
    testRuntimeOnly(libs.h2.database)
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}