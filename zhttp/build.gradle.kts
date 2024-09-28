plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.zkrallah.zhttp"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.22")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                // Publishing AAR for Android consumers
                from(components["release"])
                artifactId = "zhttp"
                version = "1.0.0"
                groupId = "com.github.muhammadzkralla"

                // Define the AAR artifact
                artifact(tasks.getByName("bundleReleaseAar")) {
                    classifier = "aar" // Set classifier to avoid conflicts
                }

                // Define the JAR artifact for JVM consumers
                artifact(tasks.create<Jar>("jvmJar") {
                    archiveClassifier.set("jvm") // Use a different classifier
                    from(android.sourceSets.getByName("main").java.srcDirs)
                })
            }
        }
    }
}