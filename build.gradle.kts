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

@file:Suppress("VulnerableLibrariesLocal")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("gg.essential.loom") version "0.10.0.4"
    id("gg.essential.defaults") version "0.1.11"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.github.ben-manes.versions") version "0.42.0"
}

version = "1.0.5-Pre"
group = "com.happyandjust"

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

loom {
    launchConfigs {
        named("client") {
            arg("--tweakClass", "gg.essential.loader.stage0.EssentialSetupTweaker")
            property("mixin.debug.export", "true")
            property("elementa.dev", "true")
        }
    }
    forge {
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
}

val include: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {

    implementation("org.spongepowered:mixin:0.8.5")

    implementation("gg.essential:essential-1.8.9-forge:3159")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    include("gg.essential:loader-launchwrapper:1.1.3")

    include("net.objecthunter:exp4j:0.4.8")

    annotationProcessor("org.spongepowered:mixin:0.8.5")
}

sourceSets.main {
    output.setResourcesDir(file("$buildDir/classes/kotlin/main"))
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("mcversion", project.property("minecraft.version"))

        filesMatching("mcmod.info") {
            expand("version" to project.version, "mcversion" to project.property("minecraft.version"))
        }
    }

    jar {
        manifest.attributes(
            "TweakClass" to "gg.essential.loader.stage0.EssentialSetupTweaker",
            "MixinConfigs" to "mixins.nameless.json",
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true
        )
        archiveBaseName.set("Nameless")

        dependsOn("shadowJar")
    }

    shadowJar {
        archiveClassifier.set("dev")
        configurations = listOf(include)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        exclude(
            "dummyThing",
            "LICENSE.txt",
            "META-INF/versions/",
            "fabric.mod.json",
            "README.md"
        )
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all"
            )
        }
    }

    remapJar {
        input.set(shadowJar.get().archiveFile)
    }
}
