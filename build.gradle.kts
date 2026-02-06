import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.10"
    `maven-publish`
}

val detektVersion = "1.23.8"
group = "com.wire"
// Version is passed from CI via -Pversion=<tag>, or defaults to detekt version for local builds
version = findProperty("version")?.takeIf { it != "unspecified" } ?: detektVersion

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:$detektVersion")

    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:$detektVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Jar> {
    archiveFileName.set("${rootProject.name}-${archiveVersion.get()}.jar")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
