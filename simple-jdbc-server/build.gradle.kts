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

    // Jackson
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.1")
    implementation("com.palantir.conjure.java.runtime:conjure-java-jackson-serialization")

    implementation(project(":simple-jdbc-api:simple-jdbc-api-objects"))
    implementation(project(":simple-jdbc-api:simple-jdbc-api-undertow"))
    implementation("com.palantir.conjure.java:conjure-java-undertow-runtime")

    api("org.slf4j:slf4j-simple")
    implementation("org.slf4j:slf4j-api")
    implementation("io.github.microutils:kotlin-logging:1.12.5")

    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("org.apache.avro:avro:1.11.0")
    implementation("org.xerial.snappy:snappy-java:1.1.8.4")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

application {
    mainClass.set("org.simplejdbc.SimpleJdbcApplicationKt")
}
