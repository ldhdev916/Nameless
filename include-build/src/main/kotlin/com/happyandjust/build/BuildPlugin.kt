package com.happyandjust.build

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {

    }
}

object Versions {
    object Mixin {
        const val Lib = "0.7.11-SNAPSHOT"
    }

    object Essential {
        const val Lib = "2117"
        const val LaunchWrapper = "1.1.3"
    }

    object Exp4j {
        const val Lib = "0.4.8"
    }

    object KotlinX {
        const val Serialization = "1.3.2"
    }

    object Processor {
        const val Mixin = "0.8.5"
        const val Gson = "2.2.4"
        const val Guava = "21.0"
        const val ASM = "6.2"
    }

    object Test {
        const val Junit = "5.8.2"
        const val Mockk = "1.12.3"
    }
}

object Deps {
    object Mixin {
        const val Lib = "org.spongepowered:mixin:${Versions.Mixin.Lib}"
    }

    object Essential {
        const val Lib = "gg.essential:essential-1.8.9-forge:${Versions.Essential.Lib}"
        const val LaunchWrapper = "gg.essential:loader-launchwrapper:${Versions.Essential.LaunchWrapper}"
    }

    object Exp4j {
        const val Lib = "net.objecthunter:exp4j:${Versions.Exp4j.Lib}"
    }

    object KotlinX {
        const val Serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KotlinX.Serialization}"
    }

    object Processor {
        const val Mixin = "org.spongepowered:mixin:${Versions.Processor.Mixin}"
        const val Gson = "com.google.code.gson:gson:${Versions.Processor.Gson}"
        const val Guava = "com.google.guava:guava:${Versions.Processor.Guava}"
        const val ASM = "org.ow2.asm:asm-tree:${Versions.Processor.ASM}"
    }

    object Test {
        const val Junit = "org.junit.jupiter:junit-jupiter-api:${Versions.Test.Junit}"
        const val JunitEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.Test.Junit}"
        const val Mockk = "io.mockk:mockk:${Versions.Test.Mockk}"
    }
}