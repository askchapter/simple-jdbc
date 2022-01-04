package org.simplejdbc

import com.palantir.conjure.java.undertow.lib.BinaryResponseBody
import java.io.OutputStream
import java.sql.Connection

class BinaryResponseBodyWrapper(private val connection: Connection, private val delegate: BinaryResponseBody): BinaryResponseBody {
    override fun write(outputStream: OutputStream) {
        delegate.write(outputStream)
        connection.close()
    }
}