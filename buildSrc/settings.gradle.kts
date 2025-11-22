rootProject.name = "buildSrc"

dependencyResolutionManagement {
    versionCatalogs {
        val libs = create("libs")
        libs.from(files("../gradle/libs.versions.toml"))
    }
}