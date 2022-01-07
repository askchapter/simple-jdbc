package org.askchapter.simplejdbc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.palantir.conjure.java.undertow.runtime.ConjureHandler
import io.undertow.Handlers
import io.undertow.Undertow
import org.askchapter.simplejdbc.api.Configuration
import org.askchapter.simplejdbc.api.JdbcDriver
import org.askchapter.simplejdbc.api.SimpleJdbcServiceEndpoints
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess


class SimpleJdbcApplication {
    fun run(configuration: Configuration) {
        val driverManager = SimpleDriverManager(configuration.drivers)

        val handler = Handlers.path().addPrefixPath(
                        "/",
                        ConjureHandler.builder()
                                .services(SimpleJdbcServiceEndpoints.of(SimpleJdbcResource(driverManager)))
                                .build())

        val server: Undertow = Undertow.builder()
                .addHttpListener(configuration.port, configuration.host)
                .setHandler(handler)
                .build()
        server.start()
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("""
            usage: simple-jdbc-server /path/to/configuration.yaml
        """.trimIndent())
        exitProcess(2)
    }

    // Load configuration
    val rawConfigurationPath = args[0]
    val objectMapper = ObjectMapper(YAMLFactory())
    val configuration = objectMapper.readValue(File(rawConfigurationPath), Configuration::class.java)

    // Resolve any paths in the configuration file relative to the parent directory of the file
    val configurationParentPath = Paths.get(rawConfigurationPath).parent
    val resolvedConfiguration = Configuration.builder()
            .from(configuration)
            .drivers(configuration.drivers.map {
                val driverPath = Paths.get(it.path)
                JdbcDriver.builder()
                        .from(it)
                        .path(configurationParentPath.resolve(driverPath).toString())
                        .build()
            })
            .build()

    SimpleJdbcApplication().run(resolvedConfiguration)
}
