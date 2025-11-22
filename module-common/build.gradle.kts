plugins {
    id("java-conventions")
    id("java-library")

    id("spring-web-conventions")
    id("spring-data-conventions")
}

dependencies {
    api(libs.kotlin.reflect)
}