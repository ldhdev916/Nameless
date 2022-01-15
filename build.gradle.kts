/*
 * Nameless - 1.8.9 Hypixel Quality Of Life Mod
 * Copyright (C) 2021 HappyAndJust
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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.user.IReobfuscator
import net.minecraftforge.gradle.user.ReobfMappingType
import net.minecraftforge.gradle.user.TaskSingleReobf
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("net.minecraftforge.gradle.forge") version "6f53277"
    id("org.spongepowered.mixin") version "d75e32e"
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
    implementation("gg.essential:essential-1.8.9-forge:1778")
    include("gg.essential:loader-launchwrapper:1.1.3")
    include("net.objecthunter:exp4j:0.4.8")

    annotationProcessor("org.spongepowered:mixin:0.7.11-SNAPSHOT")
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
    processResources {
        inputs.property("version", project.version)
        inputs.property("mcversion", project.minecraft.version)

        filesMatching("mcmod.info") {
            expand(mapOf("version" to project.version, "mcversion" to project.minecraft.version))
        }
    }

    build.get().dependsOn("shadowJar")

    named<Jar>("jar") {
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

    named<ShadowJar>("shadowJar") {
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
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xjvm-default=enable")
        }
    }

    named<TaskSingleReobf>("reobfJar") {
        enabled = false
    }
}
