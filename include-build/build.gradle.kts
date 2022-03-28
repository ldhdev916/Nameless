plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins.register("include-build") {
        id = "include-build"

        implementationClass = "com.happyandjust.build.BuildPlugin"
    }
}

dependencies {
    implementation(kotlin("gradle-plugin", "1.6.10"))
}