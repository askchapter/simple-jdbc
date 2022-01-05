import com.palantir.gradle.conjure.api.ConjureExtension
import com.palantir.gradle.conjure.api.GeneratorOptions
import groovy.lang.Closure

apply(plugin = "com.palantir.conjure")

tasks.named("publishTypeScript") {
    doFirst {
        file("simple-jdbc-api-typescript/src/.npmrc").writeText("//registry.npmjs.org/:_authToken=${System.getenv()["NPM_TOKEN"]}")
    }
}

configure<ConjureExtension> {
    typescript(delegateClosureOf<GeneratorOptions> {
        setProperty("nodeCompatibleModules", true)
    } as Closure<GeneratorOptions>)
}

