import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.dmulloy2"
version = "2.0.0-SNAPSHOT"
description = "ShadowPerms"

var isSnapshot = version.toString().endsWith("-SNAPSHOT")

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        url = uri("https://repo.dmulloy2.net/content/groups/public/")
    }

    maven {
        url = uri("https://ci.ender.zone/plugin/repository/everything/")
    }

    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

    implementation("net.dmulloy2:swornapi:2.0.0-SNAPSHOT")
    implementation("com.github.MilkBowl:VaultAPI:1.7")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    named<ShadowJar>("shadowJar"){
        dependencies {
            include(dependency("net.dmulloy2:swornapi:.*"))
        }
        relocate("net.dmulloy2.swornapi", "net.dmulloy2.shadowperms.swornapi")
        archiveFileName.set("ShadowPerms.jar")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        var buildNumber = System.getenv("BUILD_NUMBER")
        var fullVersion = if (isSnapshot && buildNumber != null)
            "$version-$buildNumber"
        else
            version.toString()

        eachFile { expand("version" to fullVersion) }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
