plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    application
}

dependencies {
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    }

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")

    implementation(project(":simple-jdbc-api:simple-jdbc-api-objects"))
    implementation(project(":simple-jdbc-api:simple-jdbc-api-undertow"))
    implementation("com.palantir.conjure.java:conjure-java-undertow-runtime")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

application {
    mainClass.set("org.simplejdbc.SimpleJdbcApplicationKt")
}