plugins {
    kotlin("jvm") version "1.9.0" // Or your preferred Kotlin version
    id("maven-publish")
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.22")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
}

java {
    // Ensure compatibility with JVM 1.8
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    jvmToolchain(8) // JVM target 1.8
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"]) // Use the java component to publish JAR

            groupId = "com.github.muhammadzkralla"
            artifactId = "zhttp-jvm"
            version = "2.8.9"
        }
    }
}