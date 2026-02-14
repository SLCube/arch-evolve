plugins {
    base
}

tasks.register("buildAll") {
    group = "build"
    description = "Build all sub-projects"
    dependsOn(gradle.includedBuilds.map { it.task(":build") })
}

tasks.register("cleanAll") {
    group = "build"
    description = "Clean all sub-projects"
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
}
