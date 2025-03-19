plugins {
    kotlin("jvm") version "2.1.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "online.aruka"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }

    maven("https://jitpack.io") {
        name = "jitpack"
    }

    maven("https://maven.enginehub.org/repo/") {
        name = "sk89q-repo"
    }

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.playpro.com")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("com.github.GriefPrevention:GriefPrevention:16.18.4")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.coreprotect:coreprotect:22.4")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
