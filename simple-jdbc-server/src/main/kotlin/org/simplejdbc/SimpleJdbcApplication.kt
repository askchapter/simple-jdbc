package org.simplejdbc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.palantir.conjure.java.undertow.runtime.ConjureHandler
import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.HttpServerExchange
import org.simplejdbc.api.Configuration
import org.simplejdbc.api.JdbcDriver
import org.simplejdbc.api.SimpleJdbcServiceEndpoints
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths
import java.sql.Driver
import java.sql.DriverManager
import java.util.logging.Logger
import java.util.logging.Level
import kotlin.system.exitProcess


class SimpleJdbcApplication {
    companion object {
        val LOG = Logger.getLogger(SimpleJdbcApplication::class.java.name)
    }

    fun run(configuration: Configuration) {
        registerDrivers(configuration.drivers)

        val handler = Handlers.path().addPrefixPath(
                        "/",
                        ConjureHandler.builder()
                                .services(SimpleJdbcServiceEndpoints.of(SimpleJdbcResource()))
                                .build())

        val server: Undertow = Undertow.builder()
                .addHttpListener(3000, "0.0.0.0")
                .setHandler(handler)
                .build()
        server.start()
    }

    private fun registerDrivers(drivers: List<JdbcDriver>) {
        drivers.forEach {
            val url = URL("jar:file:${it.path}!/")
            val classLoader = URLClassLoader(arrayOf(url))
            val driver = Class.forName(it.className, true, classLoader)
                    .getDeclaredConstructor()
                    .newInstance() as Driver
            DriverManager.registerDriver(DriverShim(driver))
        }
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("""
            USAGE: simple-jdbc-server /path/to/configuration.yaml
        """.trimIndent())
        exitProcess(0)
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
