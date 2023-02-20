import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import cc.polyfrost.gradle.util.noServerRunConfigs

plugins {
    kotlin("jvm")
    id("cc.polyfrost.multi-version")
    id("cc.polyfrost.defaults.repo")
    id("cc.polyfrost.defaults.java")
    id("cc.polyfrost.defaults.loom")
    id("com.github.johnrengelman.shadow")
    id("net.kyori.blossom") version "1.3.0"
    id("signing")
    java
}

val mod_name: String by project
val mod_version: String by project
val mod_id: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

preprocess {
    vars.put("MODERN", if (project.platform.mcMinor >= 16) 1 else 0)
}

blossom {
    replaceToken("@VER@", mod_version)
    replaceToken("@NAME@", mod_name)
    replaceToken("@ID@", mod_id)
}

version = mod_version
group = "com.may2beez"
base {
    archivesName.set(mod_name)
}

loom {
    noServerRunConfigs()
    if (project.platform.isLegacyForge) {
        launchConfigs.named("client") {
            arg("--tweakClass", "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker")
            property(
                "mixin.debug.export",
                "true"
            )
        }
    }
    if (project.platform.isForge) {
        forge {
            mixinConfig("mixins.${mod_id}.json")
        }
    }
    mixin.defaultRefmapName.set("mixins.${mod_id}.refmap.json")
}

val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

sourceSets {
    main {
        output.setResourcesDir(java.classesDirectory)
    }
}

repositories {
    maven("https://repo.polyfrost.cc/releases")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    modCompileOnly("cc.polyfrost:oneconfig-$platform:0.2.0-alpha+") // Adds the OneConfig library, so we can develop with it.
    if (platform.isLegacyForge) {
        modRuntimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.1.0")
        compileOnly("org.spongepowered:mixin:0.7.11-SNAPSHOT")
        shade("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta+")
        modCompileOnly("org.projectlombok:lombok:1.18.26")
        annotationProcessor("org.projectlombok:lombok:1.18.26")
    }
}

tasks.processResources {
    inputs.property("id", mod_id)
    inputs.property("name", mod_name)
    val java = if (project.platform.mcMinor >= 18) {
        17 // If we are playing on version 1.18, set the java version to 17
    } else {
        if (project.platform.mcMinor == 17) 16 else 8 // For all previous versions, java version 8 is okay.
    }
    val compatLevel = "JAVA_${java}"
    inputs.property("java", java)
    inputs.property("java_level", compatLevel)
    inputs.property("version", mod_version)
    inputs.property("mcVersionStr", project.platform.mcVersionStr)
    filesMatching(listOf("mcmod.info", "mixins.${mod_id}.json", "mods.toml")) {
        expand(
            mapOf(
                "id" to mod_id,
                "name" to mod_name,
                "java" to java,
                "java_level" to compatLevel,
                "version" to mod_version,
                "mcVersionStr" to project.platform.mcVersionStr
            )
        )
    }
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "id" to mod_id,
                "name" to mod_name,
                "java" to java,
                "java_level" to compatLevel,
                "version" to mod_version,
                "mcVersionStr" to project.platform.mcVersionStr.substringBeforeLast(".") + ".x"
            )
        )
    }
}

tasks {
    withType(Jar::class.java) {
        if (project.platform.isFabric) {
            exclude("mcmod.info", "mods.toml")
        } else {
            exclude("fabric.mod.json")
            if (project.platform.isLegacyForge) {
                exclude("mods.toml")
            } else {
                exclude("mcmod.info")
            }
        }
    }
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        configurations = listOf(shade)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    remapJar {
        input.set(shadowJar.get().archiveFile)
        archiveClassifier.set("all")
    }
    jar {
        manifest {
            attributes(
                mapOf(
                    "ModSide" to "CLIENT", // We aren't developing a server-side mod, so this is fine.
                    "ForceLoadAsMod" to true, // We want to load this jar as a mod, so we force Forge to do so.
                    "TweakOrder" to "0", // Makes sure that the OneConfig launch wrapper is loaded as soon as possible.
                    "MixinConfigs" to "mixin.${mod_id}.json", // We want to use our mixin configuration, so we specify it here.
                    "TweakClass" to "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker" // Loads the OneConfig launch wrapper.
                )
            )
        }
        dependsOn(shadowJar)
        archiveClassifier.set("")
        enabled = false

        project.gradle.addBuildListener(object : BuildListener {
            override fun settingsEvaluated(settings: Settings) {
                println("settings evaluated!")
            }

            override fun projectsLoaded(gradle: Gradle) {
                println("loaded!")
            }

            override fun projectsEvaluated(gradle: Gradle) {
                println("projects evaluated!")
            }

            override fun buildFinished(result: BuildResult) {
                println(jar)
            }
        })
    }
}