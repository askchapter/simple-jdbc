plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    application
    `maven-publish`
    signing
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
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Jackson
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.palantir.conjure.java.runtime:conjure-java-jackson-serialization")

    implementation(project(":simple-jdbc-api:simple-jdbc-api-objects"))
    implementation(project(":simple-jdbc-api:simple-jdbc-api-undertow"))
    implementation("com.palantir.conjure.java:conjure-java-undertow-runtime")

    api("org.slf4j:slf4j-simple")
    implementation("org.slf4j:slf4j-api")
    implementation("io.github.microutils:kotlin-logging")

    implementation("org.apache.commons:commons-csv")
    implementation("org.apache.avro:avro")
    implementation("org.xerial.snappy:snappy-java")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

application {
    mainClass.set("org.askchapter.simplejdbc.SimpleJdbcApplicationKt")
}

publishing {
    publications {
        create<MavenPublication>("distribution") {
            artifact(tasks.distTar)

            pom {
                name.set(project.name)
                description.set("An interface over JDBC providing interoperability with non-JVM languages and a simplified programming model targeting data transformation use-cases")
                url.set("https://github.com/askchapter/simple-jdbc")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }

                developers {
                    developer {
                        id.set("robertf224")
                        name.set("Robert Fidler")
                        email.set("rob@askchapter.org")
                    }
                }

                scm {
                    url.set("https://github.com/askchapter/simple-jdbc/tree/${project.version}")
                }
            }
        }
    }
}

signing {
    val key = System.getenv("SIGNING_KEY") ?: return@signing
    val password = System.getenv("SIGNING_PASSWORD") ?: return@signing

    useInMemoryPgpKeys(key, password)
    sign(publishing.publications)
}

