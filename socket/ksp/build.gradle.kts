import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

group = "com.ldhdev"
version = "1.0.5-Pre"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")

    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.21-1.0.5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
}