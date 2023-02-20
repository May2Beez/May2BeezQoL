pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.polyfrost.cc/releases") // Adds the Polyfrost maven repository to get Polyfrost Gradle Toolkit
    }
    plugins {
        val pgtVersion = "0.1.27" // Sets the default versions for Polyfrost Gradle Toolkit
        id("cc.polyfrost.multi-version.root") version pgtVersion
    }
}

val mod_name: String by settings

rootProject.name = mod_name
rootProject.buildFileName = "root.gradle.kts"

listOf(
    "1.8.9-forge"
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}