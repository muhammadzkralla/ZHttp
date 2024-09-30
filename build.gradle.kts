// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral() // Only mavenCentral needed, remove google()
    }
}

plugins {
    kotlin("jvm") version "1.9.0" apply false // Apply Kotlin JVM plugin globally for subprojects
}