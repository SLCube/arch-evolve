import gradle.kotlin.dsl.accessors._2e0c6907f7abe40edf420d143f1f0690.implementation
import gradle.kotlin.dsl.accessors._2e0c6907f7abe40edf420d143f1f0690.runtimeOnly

plugins {
    id("java-conventions")
    id("org.jetbrains.kotlin.plugin.jpa")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.5.5")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:3.5.5")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.5.5")
}