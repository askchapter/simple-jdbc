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
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()

allprojects {
    group = "org.askchapter.simplejdbc"
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("OSSRH_USER") ?: return@sonatype)
            password.set(System.getenv("OSSRH_PASSWORD") ?: return@sonatype)
        }
    }
}


