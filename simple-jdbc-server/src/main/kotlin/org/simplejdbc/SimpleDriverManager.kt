package org.simplejdbc

import org.simplejdbc.api.JdbcDriver
import java.net.URL
import java.net.URLClassLoader
import java.sql.Connection
import java.sql.Driver
import java.util.*

class SimpleDriverManager(driverConfigurations: List<JdbcDriver>) {
    private val drivers: List<Driver> = driverConfigurations.map {
        val url = URL("jar:file:${it.path}!/")
        val classLoader = URLClassLoader(
                arrayOf(url),
                // This line is critical -- it isolates the class loader for this particular driver so that we don't
                // accidentally introduce any dependency conflicts.
                ClassLoader.getSystemClassLoader().parent
        )
        val loadedClass = classLoader.loadClass(it.className)
        loadedClass.getDeclaredConstructor().newInstance() as Driver
    }

    fun getConnection(url: String): Connection {
        val driver = drivers.find { it.acceptsURL(url) }
                ?: throw RuntimeException("No driver found for url")
        return driver.connect(url, Properties())
    }
}