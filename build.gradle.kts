buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.palantir.gradle.conjure:gradle-conjure:5.19.0")
    }
}

plugins {
    id("com.palantir.consistent-versions") version "2.5.0"
}

// TODO: derive from git version
version = "0.0.1"

allprojects {
    group = "org.simplejdbc"
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}
