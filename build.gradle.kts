plugins {
    id("java")
}

group = "com.example"
version = "1.0.0"
description = "dynamodb-demo"

// Use Java 17 so `var` etc. work
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // --- AWS DynamoDB v2 SDK ---
    implementation("software.amazon.awssdk:dynamodb:2.39.5")
    implementation("software.amazon.awssdk:dynamodb-enhanced:2.39.5")

    // --- Logging ---
    implementation("org.slf4j:slf4j-simple:2.0.16")

    // --- JUnit 5 on Gradle 9 (from Gradle docs) ---

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("software.amazon.dynamodb:DynamoDBLocal:3.1.0")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}