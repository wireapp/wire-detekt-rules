import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

group = "com.wire"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Note: do not upgrade to >1.19.0, it breaks the build
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.19.0")

    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.19.0")
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Jar> {
    archiveName = "${rootProject.name}-${version}.jar"
}
