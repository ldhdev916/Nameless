/*
 * Nameless - 1.8.9 Hypixel Quality Of Life Mod
 * Copyright (C) 2022 HappyAndJust
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.apache.commons.lang3.SystemUtils

plugins {
    java
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "1.0.5"
group = "com.happyandjust"

val mcVersion: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            arg("--tweakClass", "gg.essential.loader.stage0.EssentialSetupTweaker")
        }
    }
    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                // This argument causes a crash on macOS
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.nameless.json")
    }

    mixin {
        defaultRefmapName.set("mixins.nameless.refmap.json")
    }
}

repositories {
    mavenCentral()

    maven("https://jitpack.io")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.sk1er.club/repository/maven-public")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.hypixel.net/repository/Hypixel/")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {

    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    implementation("gg.essential:essential-1.8.9-forge:17141+gd6f4cfd3a8")

    shadowImpl("gg.essential:loader-launchwrapper:1.2.3")
    shadowImpl("net.objecthunter:exp4j:0.4.8")
    shadowImpl("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2") {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-stdlib-common")
    }

    runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.0")

    annotationProcessor("org.spongepowered:mixin:0.8.5")
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
    java.srcDir(layout.projectDirectory.dir("src/main/kotlin"))
    kotlin.destinationDirectory.set(java.destinationDirectory)
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("mcversion", mcVersion)

        filesMatching("mcmod.info") {
            expand(mapOf("version" to project.version, "mcversion" to mcVersion))
        }
    }

    assemble {
        dependsOn(remapJar)
    }

    jar {
        manifest.attributes(
            mapOf(
                "TweakClass" to "gg.essential.loader.stage0.EssentialSetupTweaker",
                "MixinConfigs" to "mixins.nameless.json",
                "FMLCorePluginContainsFMLMod" to true,
                "ForceLoadAsMod" to true
            )
        )
        archiveBaseName.set("Nameless")

        archiveClassifier.set("without-deps")
        destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    }

    shadowJar {
        destinationDirectory.set(layout.buildDirectory.dir("badjars"))
        archiveClassifier.set("all-dev")
        configurations = listOf(shadowImpl)
        doLast {
            configurations.forEach {
                println("Copying jars into mod: ${it.files}")
            }
        }

        fun relocate(name: String) = relocate(name, "com.ldhdev.deps.$name")
    }

    compileJava {
        options.encoding = "UTF-8"

        dependsOn(processResources)
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
