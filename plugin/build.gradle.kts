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
    compileOnly("com.android.tools.build:gradle:8.2.2")

    testImplementation("org.ow2.asm:asm:9.2")
    testImplementation("org.ow2.asm:asm-test:9.2")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

group = "de.awenger"
version = "0.5.0"

gradlePlugin {
    website = "https://github.com/awenger/gretel"
    vcsUrl = "https://github.com/awenger/gretel"
    plugins {
        create("de.awenger.gretel") {
            id = "de.awenger.gretel"
            displayName = "Gretel"
            tags = listOf("trace", "android")
            description = "Plugin that adds system trace events to an Android app"
            implementationClass = "de.awenger.gretel.GretelPlugin"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}
