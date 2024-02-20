plugins {
    id("com.gradle.plugin-publish") version "1.2.1"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.2.2")
}

group = "de.awenger"
version = "0.0.1"

gradlePlugin {
    website = "https://github.com/awenger/gretel"
    vcsUrl = "https://github.com/awenger/gretel"
    plugins {
        create("de.awenger.gretel") {
            id = "de.awenger.gretel"
            displayName = "Gretel"
            tags = listOf("trace", "android")
            description = "Adds trace events to an Android app"
            implementationClass = "de.awenger.gretel.GretelPlugin"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}