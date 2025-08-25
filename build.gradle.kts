import org.gradle.internal.declarativedsl.parsing.main

plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "in.danny"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("io.undertow:undertow-servlet:2.3.15.Final")
    implementation ("org.slf4j:slf4j-api:2.0.13")
    runtimeOnly ("ch.qos.logback:logback-classic:1.5.13")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    // class chứa hàm main
    mainClass.set("in.danny.Main")
}

tasks.test {
    useJUnitPlatform()
}

