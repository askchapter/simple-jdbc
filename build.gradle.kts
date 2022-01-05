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
    id("com.palantir.git-version") version "0.12.3"
}

val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()

allprojects {
    group = "org.simplejdbc"
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}
