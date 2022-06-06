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

import net.minecraftforge.gradle.user.IReobfuscator
import net.minecraftforge.gradle.user.ReobfMappingType
import net.minecraftforge.gradle.user.TaskSingleReobf
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("net.minecraftforge.gradle.forge") version "6f53277"
    id("org.spongepowered.mixin") version "d75e32e"
    id("com.github.ben-manes.versions") version "0.42.0"
}

version = "1.0.5-Pre"
group = "com.happyandjust"

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

minecraft {
    version = "1.8.9-11.15.1.2318-1.8.9"
    runDir = "run"
    mappings = "stable_22"
    makeObfSourceJar = false

    clientRunArgs += arrayOf("--tweakClass gg.essential.loader.stage0.EssentialSetupTweaker")
}

mixin {
    add(sourceSets.main.get(), "mixins.nameless.refmap.json")
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

    implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT")

    implementation("gg.essential:essential-1.8.9-forge:2666")
    include("gg.essential:loader-launchwrapper:1.1.3")

    include("net.objecthunter:exp4j:0.4.8")

    include(project(":socket")) {
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-stdlib-common")
    }

    annotationProcessor("org.spongepowered:mixin:0.8.5")
    annotationProcessor("com.google.code.gson:gson:2.2.4")
    annotationProcessor("com.google.guava:guava:21.0")
    annotationProcessor("org.ow2.asm:asm-tree:6.2")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.4")

    val junit = "5.8.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit")
}

sourceSets.main {
    ext["refmap"] = "mixins.nameless.refmap.json"
    output.setResourcesDir(file("$buildDir/classes/kotlin/main"))
}

configure<NamedDomainObjectContainer<IReobfuscator>> {
    clear()
    create("shadowJar") {
        mappingType = ReobfMappingType.SEARGE
        classpath = sourceSets.main.get().compileClasspath
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    processResources {
        inputs.property("version", project.version)
        inputs.property("mcversion", project.minecraft.version)

        filesMatching("mcmod.info") {
            expand(mapOf("version" to project.version, "mcversion" to project.minecraft.version))
        }
    }

    build {
        dependsOn("shadowJar")
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

        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
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

    named<TaskSingleReobf>("reobfJar") {
        enabled = false
    }
}
