package org.simplejdbc

import java.sql.Connection
import java.sql.Driver
import java.sql.DriverPropertyInfo
import java.util.*
import java.util.logging.Logger

// https://www.kfu.com/~nsayer/Java/dyn-jdbc.html

class DriverShim(private val driver: Driver): Driver {

    override fun connect(url: String, info: Properties): Connection {
        return driver.connect(url, info)
    }

    override fun acceptsURL(url: String): Boolean {
        return driver.acceptsURL(url)
    }

    override fun getPropertyInfo(url: String, info: Properties): Array<DriverPropertyInfo> {
        return driver.getPropertyInfo(url, info)
    }

    override fun getMajorVersion(): Int {
        return driver.majorVersion
    }

    override fun getMinorVersion(): Int {
        return driver.minorVersion
    }

    override fun jdbcCompliant(): Boolean {
        return driver.jdbcCompliant()
    }

    override fun getParentLogger(): Logger {
        return driver.parentLogger
    }
}