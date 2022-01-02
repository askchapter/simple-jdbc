package org.simplejdbc

import java.io.Closeable
import java.sql.ResultSet
import java.sql.Statement

// TODO: fix concurrency issues
class ResultSetIterator<T>(private val statement: Statement, private val resultSet: ResultSet, private val mapRows: (ResultSet) -> T): Iterator<T>, Closeable {
    private var hasNextValue: Boolean? = null
    private var nextValue: T? = null

    override fun hasNext(): Boolean {
        if (hasNextValue == null) {
            hasNextValue = resultSet.next()
            if (hasNextValue as Boolean) {
                nextValue = mapRows(resultSet)
            }
        }
        return hasNextValue as Boolean
    }

    override fun next(): T {
        var toReturn: T
        if (nextValue != null) {
            toReturn = nextValue as T
            hasNextValue = null
            nextValue = null
        } else {
            val more = resultSet.next()
            if (!more) {
                throw RuntimeException("No rows remaining")
            }
            toReturn = mapRows(resultSet)
        }
        return toReturn
    }

    override fun close() {
        resultSet.close()
        statement.close()
    }

}