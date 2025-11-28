plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)

    implementation(libs.openfeign.querydsl.jpa)

    ksp(libs.openfeign.querydsl.ksp)
}