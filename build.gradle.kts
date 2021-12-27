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

version = "0.0.1"

allprojects {
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}
