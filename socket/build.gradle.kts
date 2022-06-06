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

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    id("com.google.devtools.ksp") version "1.6.21-1.0.5"
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.java-websocket:Java-WebSocket:1.5.3")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    testImplementation(kotlin("test"))

    compileOnly(project(":socket:ksp"))
    ksp(project(":socket:ksp"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }

    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            groupId = "com.ldhdev"
            artifactId = "nameless-socket-client"
            version = "1.0.3"

            from(components["java"])

            val sourcesJar by tasks.kotlinSourcesJar
            artifact(sourcesJar)
        }
    }
}