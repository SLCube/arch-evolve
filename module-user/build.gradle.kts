plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-auth-contract"))
    implementation(project(":module-user-contract"))


    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.security.core)
    implementation(libs.openfeign.querydsl.jpa)

    ksp(libs.openfeign.querydsl.ksp)

    runtimeOnly(libs.postgres)
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}